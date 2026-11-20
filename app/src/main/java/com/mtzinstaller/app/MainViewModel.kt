package com.mtzinstaller.app

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mtzinstaller.app.data.CatalogItem
import com.mtzinstaller.app.data.CatalogRepository
import com.mtzinstaller.app.data.MtzParser
import com.mtzinstaller.app.data.MtzTheme
import com.mtzinstaller.app.data.ThemeInstaller
import com.mtzinstaller.app.data.ThemeStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val store = ThemeStore(app)

    private val _themes = MutableStateFlow(store.load().sortedByDescending { it.addedAt })
    val themes: StateFlow<List<MtzTheme>> = _themes.asStateFlow()

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    var onboardingDone by mutableStateOf(store.onboardingDone)
        private set

    /** Pesan loading; null jika tidak sedang memproses. */
    var busy by mutableStateOf<String?>(null)
        private set

    var selectedId by mutableStateOf<String?>(null)

    /** Diisi jika aplikasi Tema tidak bisa dipanggil otomatis. */
    var manualHelp by mutableStateOf<ThemeInstaller.LaunchResult.ManualImportNeeded?>(null)
    var manualHelpTheme: MtzTheme? = null
        private set

    // Status aplikasi Tema — dihitung ulang setiap aplikasi dibuka kembali (onResume)
    var themeManagerAvailable by mutableStateOf(false)
        private set
    var themeManagerVersion by mutableStateOf<String?>(null)
        private set
    var themeTester by mutableStateOf(ThemeInstaller.TesterState.MISSING)
        private set
    var themeUpdated by mutableStateOf(false)
        private set
    val themeAutoApply: Boolean get() = themeTester == ThemeInstaller.TesterState.USABLE
    /** Perlu setup sekali: Tema versi update yang menguncinya → kembalikan ke versi pabrik. */
    val needsRevertSetup: Boolean get() = themeManagerAvailable && !themeAutoApply && themeUpdated
    var showRevertGuide by mutableStateOf(false)

    fun refreshThemeStatus() {
        val app = getApplication<Application>()
        themeManagerVersion = ThemeInstaller.themeManagerVersion(app)
        themeManagerAvailable = themeManagerVersion != null
        themeTester = ThemeInstaller.testerState(app)
        themeUpdated = ThemeInstaller.isThemeManagerUpdated(app)
    }

    // ---------- Unduhan dari tab Jelajah (MiuiThemez) ----------
    /** id unduhan → progres (0..1, atau -1 jika ukuran tidak diketahui). */
    val downloads = mutableStateMapOf<String, Float>()

    /** Dipanggil saat browser MiuiThemez memulai unduhan file .mtz. */
    fun downloadFromWeb(url: String, fileName: String, userAgent: String?, cookie: String?, referer: String?) {
        val title = fileName.substringBeforeLast('.').replace('_', ' ').replace('-', ' ').trim().ifEmpty { "Tema" }
        val item = CatalogItem(
            id = "web:${url.hashCode()}", title = title, author = "MiuiThemez", version = "-",
            category = "Web", description = "", sizeBytes = 0, previewUrl = null, downloadUrl = url,
        )
        val headers = buildMap {
            userAgent?.let { put("User-Agent", it) }
            cookie?.let { put("Cookie", it) }
            referer?.let { put("Referer", it) }
        }
        runDownload(item, headers)
    }

    private fun runDownload(item: CatalogItem, headers: Map<String, String>) {
        if (item.id in downloads) return
        downloads[item.id] = 0f
        viewModelScope.launch {
            val ctx = getApplication<Application>()
            val tmp = File(ctx.cacheDir, "dl_${item.id.hashCode()}.mtz")
            try {
                val theme = withContext(Dispatchers.IO) {
                    CatalogRepository.download(item.downloadUrl, tmp, headers) { p -> downloads[item.id] = p }
                    importFile(tmp, "${item.title}.mtz", item)
                }
                _themes.value.filter { it.sourceId == item.id }.forEach { old ->
                    File(old.filePath).delete()
                    old.previewPath?.let { File(it).delete() }
                }
                update { listOf(theme) + it.filterNot { t -> t.sourceId == item.id } }
                selectedId = theme.id
                _messages.tryEmit("\"${theme.title}\" berhasil diunduh")
            } catch (e: MtzParser.InvalidMtzException) {
                _messages.tryEmit(e.message ?: "File tidak valid")
            } catch (e: Exception) {
                _messages.tryEmit("Gagal mengunduh: ${e.localizedMessage ?: e.javaClass.simpleName}")
            } finally {
                tmp.delete()
                downloads.remove(item.id)
            }
        }
    }

    init {
        refreshThemeStatus()
    }

    fun finishOnboarding() {
        store.onboardingDone = true
        onboardingDone = true
    }

    fun importFromUri(uri: Uri) {
        if (busy != null) return
        viewModelScope.launch {
            busy = "Membaca file tema…"
            try {
                val theme = withContext(Dispatchers.IO) { importInternal(uri) }
                update { listOf(theme) + it }
                selectedId = theme.id
                _messages.tryEmit("Tema \"${theme.title}\" ditambahkan ke koleksi")
            } catch (e: MtzParser.InvalidMtzException) {
                _messages.tryEmit(e.message ?: "File tidak valid")
            } catch (e: Exception) {
                _messages.tryEmit("Gagal mengimpor: ${e.localizedMessage ?: e.javaClass.simpleName}")
            } finally {
                busy = null
            }
        }
    }

    fun install(theme: MtzTheme) {
        if (busy != null) return
        viewModelScope.launch {
            busy = "Menyiapkan tema…"
            try {
                val ctx = getApplication<Application>()
                val exported = withContext(Dispatchers.IO) { ThemeInstaller.exportToPublic(ctx, theme) }
                when (val r = ThemeInstaller.launchApply(ctx, exported)) {
                    ThemeInstaller.LaunchResult.Applied ->
                        _messages.tryEmit("Membuka Tema untuk menerapkan \"${theme.title}\"")
                    is ThemeInstaller.LaunchResult.HandedOff ->
                        _messages.tryEmit("File diteruskan ke ${r.via}. Pilih aplikasi Tema lalu ketuk Terapkan")
                    is ThemeInstaller.LaunchResult.ManualImportNeeded -> {
                        manualHelpTheme = theme
                        manualHelp = r
                    }
                }
                val now = System.currentTimeMillis()
                update { list ->
                    list.map {
                        if (it.id == theme.id) it.copy(lastInstalledAt = now, installCount = it.installCount + 1) else it
                    }
                }
            } catch (e: Exception) {
                _messages.tryEmit("Gagal memasang: ${e.localizedMessage ?: e.javaClass.simpleName}")
            } finally {
                busy = null
            }
        }
    }

    /** Serahkan tema ke aplikasi pemasang lain (MTZ Studio, HyperTheme, dll). */
    fun installWithOtherApp(theme: MtzTheme) {
        if (busy != null) return
        viewModelScope.launch {
            busy = "Menyiapkan file…"
            try {
                val ctx = getApplication<Application>()
                val exported = withContext(Dispatchers.IO) { ThemeInstaller.exportToPublic(ctx, theme) }
                if (!ThemeInstaller.openWithOtherApp(ctx, exported)) {
                    _messages.tryEmit("Belum ada aplikasi pemasang lain di HP ini")
                }
            } catch (e: Exception) {
                _messages.tryEmit("Gagal menyiapkan file: ${e.localizedMessage ?: e.javaClass.simpleName}")
            } finally {
                busy = null
            }
        }
    }

    fun diagnosticsReport(): String = ThemeInstaller.diagnose(getApplication())

    fun installedHelpers(): List<String> = ThemeInstaller.installedHelpers(getApplication())

    fun toggleFavorite(theme: MtzTheme) {
        update { list -> list.map { if (it.id == theme.id) it.copy(favorite = !it.favorite) else it } }
    }

    fun delete(theme: MtzTheme) {
        File(theme.filePath).delete()
        theme.previewPath?.let { File(it).delete() }
        if (selectedId == theme.id) selectedId = null
        update { list -> list.filterNot { it.id == theme.id } }
        _messages.tryEmit("\"${theme.title}\" dihapus dari koleksi")
    }

    private fun update(transform: (List<MtzTheme>) -> List<MtzTheme>) {
        val next = transform(_themes.value)
        _themes.value = next
        store.save(next)
    }

    private fun importInternal(uri: Uri): MtzTheme {
        val ctx = getApplication<Application>()
        val tmp = File(ctx.cacheDir, "import_${System.nanoTime()}.mtz")
        try {
            val input = ctx.contentResolver.openInputStream(uri)
                ?: throw IllegalStateException("Tidak bisa membuka file")
            input.use { src -> tmp.outputStream().use { src.copyTo(it) } }
            return importFile(tmp, queryDisplayName(ctx, uri), null)
        } finally {
            tmp.delete()
        }
    }

    /** Validasi + simpan file .mtz ke koleksi. Data katalog dipakai jika description.xml kosong. */
    private fun importFile(source: File, displayName: String?, item: CatalogItem?): MtzTheme {
        val ctx = getApplication<Application>()
        val id = UUID.randomUUID().toString()
        val dir = File(ctx.filesDir, "themes").apply { mkdirs() }
        val dest = File(dir, "$id.mtz")
        source.copyTo(dest, overwrite = true)

        val meta = try {
            MtzParser.parse(dest)
        } catch (e: Exception) {
            dest.delete()
            throw e
        }
        val previewBytes = meta.preview ?: item?.previewUrl?.let { CatalogRepository.downloadBytes(it) }
        val previewPath = previewBytes?.let { savePreview(ctx, id, it) }

        return MtzTheme(
            id = id,
            title = meta.title ?: item?.title ?: displayName?.substringBeforeLast('.') ?: "Tema tanpa nama",
            author = meta.author ?: item?.author ?: "Tidak diketahui",
            version = meta.version ?: item?.version ?: "-",
            description = meta.description ?: item?.description.orEmpty(),
            sizeBytes = dest.length(),
            filePath = dest.absolutePath,
            previewPath = previewPath,
            addedAt = System.currentTimeMillis(),
            sourceId = item?.id,
        )
    }

    private fun queryDisplayName(ctx: Context, uri: Uri): String? {
        if (uri.scheme == "file") return uri.lastPathSegment
        return runCatching {
            ctx.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { c -> if (c.moveToFirst()) c.getString(0) else null }
        }.getOrNull()
    }

    /** Simpan pratinjau yang sudah diperkecil agar daftar tetap ringan. */
    private fun savePreview(ctx: Context, id: String, bytes: ByteArray): String? = runCatching {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        var sample = 1
        while (bounds.outHeight / sample > 1200 || bounds.outWidth / sample > 1200) sample *= 2
        val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, BitmapFactory.Options().apply { inSampleSize = sample })
            ?: return@runCatching null
        val dir = File(ctx.filesDir, "previews").apply { mkdirs() }
        val out = File(dir, "$id.jpg")
        out.outputStream().use { bmp.compress(Bitmap.CompressFormat.JPEG, 85, it) }
        bmp.recycle()
        out.absolutePath
    }.getOrNull()
}
