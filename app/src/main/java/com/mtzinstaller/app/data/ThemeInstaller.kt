package com.mtzinstaller.app.data

import android.content.ClipData
import android.content.ComponentName
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File

/**
 * Memasang tema lewat aplikasi Tema bawaan Xiaomi (com.android.thememanager).
 *
 * Xiaomi sering mengubah/menghapus "pintu masuk" untuk tema dari luar, jadi beberapa rute
 * dicoba berurutan dan setiap rute DICEK dulu lewat PackageManager sebelum dipanggil:
 *
 *  1. Tester lama  : ApplyThemeForScreenshot + action "support3.0" (Tema Global 2.15.x–3.0.5.x).
 *  2. Buka file    : ACTION_VIEW file .mtz langsung ke aplikasi Tema (jika versi itu mendukung).
 *  3. File Manager : serahkan file ke File Manager Xiaomi, yang meneruskannya ke importer Tema.
 *  4. Manual       : buka layar tema lokal / aplikasi Tema + panduan.
 */
object ThemeInstaller {

    const val THEME_MANAGER = "com.android.thememanager"
    private const val TESTER_ACTIVITY = "com.android.thememanager.ApplyThemeForScreenshot"
    private const val TESTER_ACTION = "com.android.thememanager.support3.0"
    private const val RESOURCE_TAB_ACTIVITY = "com.android.thememanager.ThemeResourceTabActivity"
    private const val EXPORT_FOLDER = "MtzInstaller"
    private val MTZ_MIME_TYPES = listOf(
        "application/vnd.miui.mtz",
        "application/x-mtz",
        "application/zip",
        "application/octet-stream",
    )

    data class Exported(val path: String, val uri: Uri)

    sealed interface LaunchResult {
        /** Aplikasi Tema langsung dibuka dengan tema ini. */
        data object Applied : LaunchResult
        /** File diserahkan ke aplikasi lain (Tema/File Manager); pengguna tinggal konfirmasi. */
        data class HandedOff(val via: String) : LaunchResult
        /** Tidak ada rute otomatis di versi Tema ini. */
        data class ManualImportNeeded(
            val path: String,
            val themeAppVersion: String?,
            val testerState: TesterState,
            val canRevertThemeApp: Boolean,
        ) : LaunchResult
    }

    fun isThemeManagerAvailable(context: Context): Boolean = themeManagerVersion(context) != null

    fun themeManagerVersion(context: Context): String? = try {
        context.packageManager.getPackageInfo(THEME_MANAGER, 0).versionName ?: "?"
    } catch (e: PackageManager.NameNotFoundException) {
        null
    }

    enum class TesterState { USABLE, LOCKED, MISSING }

    /**
     * Status pintu pemasangan otomatis (ApplyThemeForScreenshot):
     * USABLE  = ada & terbuka untuk aplikasi lain,
     * LOCKED  = masih ada tapi dikunci Xiaomi (exported=false, mis. Tema 3.0.6.8),
     * MISSING = sudah dihapus.
     */
    fun testerState(context: Context): TesterState {
        val pm = context.packageManager
        val infos = legacyTesterIntents(context, "/x.mtz").mapNotNull { pm.resolveActivity(it, 0)?.activityInfo }
        return when {
            infos.any { it.exported } -> TesterState.USABLE
            infos.isNotEmpty() -> TesterState.LOCKED
            else -> TesterState.MISSING
        }
    }

    /** True jika versi Tema yang terpasang masih punya jalur pemasangan otomatis (tester lama). */
    fun hasAutoApplyRoute(context: Context): Boolean = testerState(context) == TesterState.USABLE

    /** True jika aplikasi Tema adalah versi update yang bisa dikembalikan ke versi pabrik. */
    fun isThemeManagerUpdated(context: Context): Boolean = runCatching {
        val ai = context.packageManager.getApplicationInfo(THEME_MANAGER, 0)
        (ai.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
    }.getOrDefault(false)

    /**
     * Aplikasi Tema tidak bisa membaca folder privat aplikasi ini, jadi file disalin ke
     * Download/MtzInstaller lewat MediaStore (tanpa izin tambahan di Android 10+).
     */
    fun exportToPublic(context: Context, theme: MtzTheme): Exported {
        val resolver = context.contentResolver
        val relativePath = "${Environment.DIRECTORY_DOWNLOADS}/$EXPORT_FOLDER/"
        val name = "${safeName(theme.title)}_${theme.id.take(6)}.mtz"
        val collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI

        runCatching {
            resolver.delete(
                collection,
                "${MediaStore.MediaColumns.DISPLAY_NAME}=? AND ${MediaStore.MediaColumns.RELATIVE_PATH}=?",
                arrayOf(name, relativePath),
            )
        }

        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/octet-stream")
            put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
        val uri = resolver.insert(collection, values)
            ?: throw IllegalStateException("Tidak bisa menulis ke folder Download")

        resolver.openOutputStream(uri)?.use { out ->
            File(theme.filePath).inputStream().use { it.copyTo(out) }
        } ?: throw IllegalStateException("Tidak bisa menulis file tema")

        values.clear()
        values.put(MediaStore.MediaColumns.IS_PENDING, 0)
        resolver.update(uri, values, null, null)

        val finalName = resolver.query(uri, arrayOf(MediaStore.MediaColumns.DISPLAY_NAME), null, null, null)
            ?.use { c -> if (c.moveToFirst()) c.getString(0) else null } ?: name

        @Suppress("DEPRECATION")
        val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        return Exported(File(File(downloads, EXPORT_FOLDER), finalName).absolutePath, uri)
    }

    fun launchApply(context: Context, exported: Exported): LaunchResult {
        val pm = context.packageManager

        // 1) Tester lama — kontrak persis yang terverifikasi di Tema Global 2.15.x / 3.0.x
        legacyTesterIntents(context, exported.path)
            .firstOrNull { pm.resolveActivity(it, 0)?.activityInfo?.exported == true }
            ?.let { if (start(context, it)) return LaunchResult.Applied }

        // 2) Versi Tema yang masih menerima "buka file .mtz" secara langsung
        MTZ_MIME_TYPES.forEach { mime ->
            val open = viewIntent(exported.uri, mime).setPackage(THEME_MANAGER)
            if (open.resolveActivity(pm) != null && start(context, open)) {
                return LaunchResult.HandedOff("Tema")
            }
        }

        // (Rute File Manager tidak lagi dijalankan otomatis: di beberapa HP ia membuka pemilih
        //  aplikasi acak / meminta "temp.mtz" di root. Sekarang jadi pilihan manual yang dipandu.)

        // 4) Tidak ada rute otomatis
        return LaunchResult.ManualImportNeeded(
            path = exported.path,
            themeAppVersion = themeManagerVersion(context),
            testerState = testerState(context),
            canRevertThemeApp = isThemeManagerUpdated(context),
        )
    }

    /** Buka layar "tema saya" (lokal) jika ada, kalau tidak buka aplikasi Tema biasa. */
    fun openThemeManager(context: Context): Boolean {
        val local = Intent().apply {
            component = ComponentName(THEME_MANAGER, RESOURCE_TAB_ACTIVITY)
            putExtra("extra_outer_tab_id", "mine")
            putExtra("EXTRA_TAB_ID", "theme")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (local.resolveActivity(context.packageManager) != null && start(context, local)) return true
        val launch = context.packageManager.getLaunchIntentForPackage(THEME_MANAGER) ?: return false
        return start(context, launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    /** Membuka halaman info aplikasi Tema (untuk "Copot pemasangan pembaruan"). */
    fun openThemeManagerAppInfo(context: Context): Boolean = start(
        context,
        Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$THEME_MANAGER"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
    )

    /**
     * Membuka halaman aplikasi Tema di toko aplikasi (GetApps Xiaomi, lalu Play Store) supaya
     * pengguna bisa memperbarui kembali ke versi terbaru setelah sebelumnya dikembalikan ke versi pabrik.
     */
    fun openThemeManagerStorePage(context: Context): Boolean {
        val candidates = listOf(
            Intent(Intent.ACTION_VIEW, Uri.parse("mimarket://details?id=$THEME_MANAGER")).setPackage("com.xiaomi.mipicks"),
            Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$THEME_MANAGER")).setPackage("com.xiaomi.mipicks"),
            Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$THEME_MANAGER")).setPackage("com.android.vending"),
            Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$THEME_MANAGER")),
            Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$THEME_MANAGER")),
        )
        return candidates.any { start(context, it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    }

    /** Membuka aplikasi GetApps (untuk mengatur pembaruan otomatis). */
    fun openGetApps(context: Context): Boolean {
        val i = context.packageManager.getLaunchIntentForPackage("com.xiaomi.mipicks")
            ?: context.packageManager.getLaunchIntentForPackage("com.android.vending")
            ?: return false
        return start(context, i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    // ---------- temp.mtz di root (diminta oleh importer Tema di sebagian versi) ----------

    /** True jika aplikasi punya izin "Akses semua file" (dibutuhkan untuk menulis ke root penyimpanan). */
    fun hasAllFilesAccess(): Boolean = Environment.isExternalStorageManager()

    /** Membuka halaman izin "Akses semua file" untuk aplikasi ini. */
    fun requestAllFilesAccess(context: Context): Boolean =
        start(
            context,
            Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:${context.packageName}"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        ) || start(context, Intent(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))

    /** Menyalin tema ke /sdcard/temp.mtz (butuh izin Akses semua file). */
    fun writeRootTemp(theme: MtzTheme): String {
        @Suppress("DEPRECATION")
        val target = File(Environment.getExternalStorageDirectory(), "temp.mtz")
        File(theme.filePath).copyTo(target, overwrite = true)
        return target.absolutePath
    }

    /** Membuka File Manager Xiaomi di halaman utama penyimpanan (tempat temp.mtz). */
    fun openFileManager(context: Context): Boolean {
        val pm = context.packageManager
        val fm = pm.getLaunchIntentForPackage("com.mi.android.globalFileexplorer")
            ?: pm.getLaunchIntentForPackage("com.android.fileexplorer")
            ?: pm.getLaunchIntentForPackage("com.google.android.apps.nbu.files")
            ?: return false
        return start(context, fm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    /** Membuka File Manager di folder Download/MtzInstaller (best effort). */
    fun openExportFolder(context: Context): Boolean {
        val folder = Uri.parse("content://com.android.externalstorage.documents/document/primary%3ADownload%2F$EXPORT_FOLDER")
        val viaFm = Intent(Intent.ACTION_VIEW).setDataAndType(folder, "vnd.android.document/directory")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        if (start(context, viaFm)) return true
        val miFm = context.packageManager.getLaunchIntentForPackage("com.mi.android.globalFileexplorer")
            ?: context.packageManager.getLaunchIntentForPackage("com.android.fileexplorer")
            ?: return false
        return start(context, miFm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    /**
     * Serahkan file .mtz ke aplikasi pemasang lain yang terpasang di HP (mis. HyperOS MTZ Studio,
     * HyperTheme, MTZ Installer Pro). Memakai "Buka dengan" + "Bagikan" sekaligus.
     */
    fun openWithOtherApp(context: Context, exported: Exported): Boolean {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_STREAM, exported.uri)
            clipData = ClipData.newRawUri("MTZ", exported.uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val view = viewIntent(exported.uri, "application/octet-stream")
        val chooser = Intent.createChooser(send, "Pasang dengan aplikasi lain").apply {
            putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(view))
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return start(context, chooser)
    }

    /** Laporan kompatibilitas yang bisa disalin pengguna untuk dianalisis. */
    fun diagnose(context: Context): String {
        val pm = context.packageManager
        val sb = StringBuilder()
        sb.appendLine("== MTZ Installer gen Z — laporan HP ==")
        sb.appendLine("HP        : ${Build.MANUFACTURER} ${Build.MODEL} (${Build.DEVICE})")
        sb.appendLine("Android   : ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
        sb.appendLine("ROM       : ${Build.VERSION.INCREMENTAL}")
        val info = runCatching {
            @Suppress("DEPRECATION")
            pm.getPackageInfo(THEME_MANAGER, PackageManager.GET_ACTIVITIES)
        }.getOrNull()
        if (info == null) {
            sb.appendLine("Tema      : TIDAK DITEMUKAN")
            return sb.toString()
        }
        val ai = info.applicationInfo
        val updated = ai != null && (ai.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
        @Suppress("DEPRECATION")
        val code = if (Build.VERSION.SDK_INT >= 28) info.longVersionCode else info.versionCode.toLong()
        sb.appendLine("Tema      : ${info.versionName} (code $code)${if (updated) " • versi update" else " • versi pabrik"}")
        sb.appendLine()
        sb.appendLine("-- Rute pemasangan --")
        val tester = legacyTesterIntents(context, "/x.mtz")
        fun state(i: Intent): String {
            val a = pm.resolveActivity(i, 0)?.activityInfo ?: return "tidak ada ❌"
            return if (a.exported) "ADA, terbuka ✅" else "ada tapi DIKUNCI 🔒"
        }
        sb.appendLine("Tester support3.0 : ${state(tester[0])}")
        sb.appendLine("Tester lama       : ${state(tester[1])}")
        val probe = Uri.parse("content://media/external/downloads/1")
        MTZ_MIME_TYPES.forEach { mime ->
            val handlers = pm.queryIntentActivities(viewIntent(probe, mime), 0).map { it.activityInfo.packageName }.distinct()
            sb.appendLine("Buka $mime : ${handlers.ifEmpty { listOf("-") }.joinToString()}")
        }
        val tab = Intent().setComponent(ComponentName(THEME_MANAGER, RESOURCE_TAB_ACTIVITY))
        sb.appendLine("Layar tema lokal  : ${yes(tab.resolveActivity(pm) != null)}")
        sb.appendLine()
        sb.appendLine("-- Activity Tema yang terbuka (exported) --")
        info.activities.orEmpty()
            .filter { it.exported }
            .map { it.name.removePrefix("$THEME_MANAGER.") }
            .sorted()
            .take(60)
            .forEach { sb.appendLine("• $it") }
        sb.appendLine()
        sb.appendLine("-- Aplikasi pembantu terpasang --")
        HELPER_APPS.forEach { (pkg, label) ->
            val v = runCatching { pm.getPackageInfo(pkg, 0).versionName }.getOrNull()
            if (v != null) sb.appendLine("• $label $v")
        }
        return sb.toString()
    }

    /** Aplikasi pembantu yang dikenal (untuk deteksi & tombol "Pasang dengan aplikasi lain"). */
    val HELPER_APPS = listOf(
        "moe.shizuku.privileged.api" to "Shizuku",
        "dev.glorioustr.mtzstudio" to "HyperOS MTZ Studio",
        "com.guffyapps.mtzinstallerpro" to "MTZ Installer Pro",
    )

    fun installedHelpers(context: Context): List<String> = HELPER_APPS.mapNotNull { (pkg, label) ->
        runCatching { context.packageManager.getPackageInfo(pkg, 0); label }.getOrNull()
    }

    private fun yes(b: Boolean) = if (b) "ADA ✅" else "tidak ada ❌"

    private fun legacyTesterIntents(context: Context, path: String): List<Intent> = listOf(
        // Kontrak lengkap (action support3.0 + 4 extras)
        Intent(TESTER_ACTION).apply {
            component = ComponentName(THEME_MANAGER, TESTER_ACTIVITY)
            putExtra("theme_file_path", path)
            putExtra("api_called_from", context.packageName)
            putExtra("theme_apply_flags", -1L)
            putExtra("theme_remove_flags", -1L)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        },
        // Varian lama tanpa action (MIUI 12–14)
        Intent(Intent.ACTION_MAIN).apply {
            component = ComponentName(THEME_MANAGER, TESTER_ACTIVITY)
            putExtra("theme_file_path", path)
            putExtra("api_called_from", "ThemeEditor")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        },
    )

    private fun viewIntent(uri: Uri, mime: String) = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, mime)
        clipData = ClipData.newRawUri("MTZ", uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    private fun start(context: Context, intent: Intent): Boolean = runCatching {
        context.startActivity(intent)
    }.isSuccess

    private fun safeName(s: String): String =
        s.replace(Regex("[^A-Za-z0-9._-]+"), "_").trim('_').take(40).ifEmpty { "theme" }
}
