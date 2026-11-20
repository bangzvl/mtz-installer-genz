package com.mtzinstaller.app

import android.content.Intent
import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mtzinstaller.app.data.ThemeInstaller
import com.mtzinstaller.app.ui.components.FloatingNavBar
import com.mtzinstaller.app.ui.components.DownloadOverlay
import com.mtzinstaller.app.ui.components.InstallOverlay
import com.mtzinstaller.app.ui.components.InstalledSuccessCard
import com.mtzinstaller.app.ui.components.SoftBusyOverlay
import com.mtzinstaller.app.ui.components.NavTab
import com.mtzinstaller.app.ui.components.PillButton
import com.mtzinstaller.app.ui.screens.BrowseScreen
import com.mtzinstaller.app.ui.screens.CollectionScreen
import com.mtzinstaller.app.ui.screens.HomeScreen
import com.mtzinstaller.app.ui.screens.InfoScreen
import com.mtzinstaller.app.ui.screens.OnboardingScreen
import com.mtzinstaller.app.ui.screens.ThemeDetailSheet
import com.mtzinstaller.app.ui.theme.MtzAppTheme
import com.mtzinstaller.app.ui.theme.MtzColors
import com.mtzinstaller.app.ui.theme.MtzType

class MainActivity : ComponentActivity() {

    private val vm: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT),
        )
        if (savedInstanceState == null) handleIntent(intent)
        setContent { MtzAppTheme { MtzApp(vm) } }
    }

    override fun onResume() {
        super.onResume()
        // Cek ulang status aplikasi Tema & tampilkan popup "tema terpasang" bila baru kembali dari Tema
        vm.onAppResumed()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    /** File .mtz dibuka dari file manager → langsung impor. */
    private fun handleIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_VIEW) {
            intent.data?.let {
                vm.finishOnboarding()
                vm.importFromUri(it)
            }
        }
    }
}

@Composable
fun MtzApp(vm: MainViewModel) {
    val themes by vm.themes.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current
    LaunchedEffect(Unit) { vm.messages.collect { snackbar.showSnackbar(it) } }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) vm.importFromUri(uri)
    }
    // .mtz tidak punya MIME baku, jadi tampilkan semua file lalu validasi isinya.
    val openPicker: () -> Unit = { picker.launch(arrayOf("*/*")) }

    if (!vm.onboardingDone) {
        OnboardingScreen(onStart = vm::finishOnboarding)
    } else {
        var tab by rememberSaveable { mutableStateOf(NavTab.Home) }

        Box(Modifier.fillMaxSize().background(MtzColors.Background)) {
            when (tab) {
                NavTab.Home -> HomeScreen(
                    themes = themes,
                    setupNeeded = vm.needsRevertSetup,
                    onSetup = { vm.showRevertGuide = true },
                    onImport = openPicker,
                    onNavigate = { tab = it },
                    onOpen = { vm.selectedId = it.id },
                    onInstall = vm::install,
                    onToggleFavorite = vm::toggleFavorite,
                )
                NavTab.Browse -> BrowseScreen(
                    downloads = vm.downloads,
                    onWebDownload = vm::downloadFromWeb,
                )
                NavTab.Collection -> CollectionScreen(
                    themes = themes,
                    onOpen = { vm.selectedId = it.id },
                    onImport = openPicker,
                )
                NavTab.Info -> InfoScreen(
                    themeManagerAvailable = vm.themeManagerAvailable,
                    themeManagerVersion = vm.themeManagerVersion,
                    autoApply = vm.themeAutoApply,
                    locked = vm.themeTester == ThemeInstaller.TesterState.LOCKED,
                    needsRevert = vm.needsRevertSetup,
                    onRevertGuide = { vm.showRevertGuide = true },
                    isFactoryVersion = vm.themeManagerAvailable && !vm.themeUpdated,
                    onRestoreLatest = { vm.showUpdateGuide = true },
                    helpers = vm.installedHelpers(),
                    report = vm::diagnosticsReport,
                    onOpenThemeApp = { ThemeInstaller.openThemeManager(context) },
                )
            }

            FloatingNavBar(
                current = tab,
                onSelect = { tab = it },
                onCenter = openPicker,
                modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 14.dp),
            )
            SnackbarHost(
                snackbar,
                Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(start = 16.dp, end = 16.dp, bottom = 104.dp),
            ) { data ->
                Snackbar(data, containerColor = MtzColors.Ink, contentColor = Color.White, shape = RoundedCornerShape(18.dp))
            }

            // Popup saat mengunduh dari Jelajah
            vm.downloads.entries.firstOrNull()?.let { (id, progress) ->
                DownloadOverlay(
                    title = vm.downloadTitles[id] ?: "Tema",
                    progress = progress,
                    verifying = vm.downloadVerifying[id] == true,
                    onCancel = { vm.cancelDownload(id) },
                )
            }

            // Popup kartu "tema terpasang"
            vm.successTheme?.let { t ->
                InstalledSuccessCard(
                    title = t.title,
                    previewPath = t.previewPath,
                    colorKey = t.id,
                    onClose = { vm.successTheme = null },
                    onOpenCollection = { vm.successTheme = null; tab = NavTab.Collection },
                )
            }
        }
    }

    themes.firstOrNull { it.id == vm.selectedId }?.let { theme ->
        ThemeDetailSheet(
            theme = theme,
            busy = vm.busy != null,
            onDismiss = { vm.selectedId = null },
            onInstall = { vm.install(theme) },
            onInstallOther = { vm.installWithOtherApp(theme) },
            onToggleFavorite = { vm.toggleFavorite(theme) },
            onDelete = { vm.delete(theme) },
        )
    }

    vm.manualHelp?.let { help ->
        val close = { vm.manualHelp = null }
        AlertDialog(
            onDismissRequest = close,
            title = { Text("Pasang dengan 1 langkah lagi", style = MtzType.Heading) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        "Versi aplikasi Tema di HP ini (${help.themeAppVersion ?: "tidak diketahui"}) sudah menutup " +
                            "pemasangan otomatis dari aplikasi lain. Filenya sudah siap di folder Download/MtzInstaller. " +
                            "Pilih salah satu cara:",
                        style = MtzType.Body,
                    )
                    Spacer(Modifier.height(14.dp))
                    if (help.canRevertThemeApp && help.testerState != ThemeInstaller.TesterState.USABLE) {
                        HelpOption(
                            "⭐ Disarankan: kembalikan Tema ke versi pabrik (sekali saja)",
                            "Versi update ${help.themeAppVersion ?: ""} mengunci pemasangan otomatis. " +
                                "Versi pabrik biasanya masih membukanya, jadi setelah itu tombol Pasang langsung berfungsi.",
                            "Lihat panduan",
                        ) { close(); vm.showRevertGuide = true }
                    }
                    HelpOption(
                        "1. Lewat File Manager (temp.mtz)",
                        "Aplikasi Tema di sebagian HP meminta file bernama temp.mtz di penyimpanan utama. " +
                            "Ketuk tombol di bawah: filenya kami siapkan otomatis (sekali izinkan \"Akses semua file\"), " +
                            "lalu di File Manager ketuk temp.mtz → Tema → Terapkan.",
                        "Siapkan temp.mtz",
                    ) { close(); vm.manualHelpTheme?.let(vm::prepareRootTemp) }
                    HelpOption(
                        "2. Lewat aplikasi Tema",
                        "Buka Tema → Saya/Profil → Tema → Impor, lalu pilih file dari Download/MtzInstaller.",
                        "Buka Tema",
                    ) { close(); ThemeInstaller.openThemeManager(context) }
                    HelpOption(
                        "3. Kembalikan Tema ke versi bawaan",
                        "Di info aplikasi Tema, ketuk ⋮ → Copot pemasangan pembaruan. Versi bawaan sering masih " +
                            "mendukung pasang otomatis. Lalu coba Pasang lagi.",
                        "Info aplikasi",
                    ) { close(); ThemeInstaller.openThemeManagerAppInfo(context) }
                    HelpOption(
                        "4. Pakai aplikasi pemasang lain",
                        "Kalau kamu memasang aplikasi pembantu (mis. HyperOS MTZ Studio + Shizuku), " +
                            "tema bisa langsung diserahkan ke sana. Panduannya ada di tab Info.",
                        "Pilih aplikasi",
                    ) { close(); vm.manualHelpTheme?.let(vm::installWithOtherApp) }
                }
            },
            confirmButton = { TextButton(onClick = close) { Text("Tutup", color = MtzColors.Ink) } },
            containerColor = MtzColors.Cream,
        )
    }

    if (vm.showRevertGuide) {
        RevertGuideDialog(
            version = vm.themeManagerVersion,
            onOpenAppInfo = { ThemeInstaller.openThemeManagerAppInfo(context) },
            onOpenGetApps = { ThemeInstaller.openGetApps(context) },
            onDismiss = { vm.showRevertGuide = false; vm.refreshThemeStatus() },
        )
    }

    if (vm.showUpdateGuide) {
        UpdateGuideDialog(
            version = vm.themeManagerVersion,
            onOpenStore = { ThemeInstaller.openThemeManagerStorePage(context) },
            onOpenGetApps = { ThemeInstaller.openGetApps(context) },
            onDismiss = { vm.showUpdateGuide = false; vm.refreshThemeStatus() },
        )
    }

    vm.installProgress?.let { p ->
        InstallOverlay(
            title = p.theme.title,
            previewPath = p.theme.previewPath,
            colorKey = p.theme.id,
            step = p.step,
            done = p.done,
            failed = p.failed,
            hint = if (vm.themeAutoApply) "Sebentar lagi Xiaomi menampilkan layar hitam \"mode debugging\". Itu normal, tema sedang diterapkan. Tekan kembali untuk lanjut." else null,
        )
    }
    vm.busy?.let { SoftBusyOverlay(it) }
}

@Composable
private fun UpdateGuideDialog(
    version: String?,
    onOpenStore: () -> Unit,
    onOpenGetApps: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Kembalikan Tema ke versi terbaru ⬆️", style = MtzType.Heading) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    "Saat ini aplikasi Tema memakai versi pabrik${version?.let { " ($it)" } ?: ""}. " +
                        "Kamu bisa memperbaruinya lagi ke versi terbaru kapan saja.",
                    style = MtzType.Body,
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    "Catatan: tema yang sudah terpasang tetap aktif, tapi versi terbaru mengunci pemasangan " +
                        "otomatis lagi. Untuk memasang tema baru nanti, cukup ulangi \"Buka kunci\".",
                    style = MtzType.Caption,
                    color = MtzColors.Muted,
                )
                Spacer(Modifier.height(14.dp))
                HelpOption(
                    "Langkah 1 — Perbarui aplikasi Tema",
                    "Ketuk tombol di bawah untuk membuka halaman Tema di GetApps/Play Store, lalu ketuk \"Perbarui\".",
                    "Buka halaman update",
                    onOpenStore,
                )
                HelpOption(
                    "Langkah 2 (opsional) — Nyalakan lagi update otomatis",
                    "Di GetApps → Saya → Pengaturan, nyalakan kembali \"Pembaruan otomatis\" jika kamu ingin semua aplikasi selalu terbaru.",
                    "Buka GetApps",
                    onOpenGetApps,
                )
                Text(
                    "Jika halaman update tidak menampilkan tombol Perbarui, buka Pengaturan → Tentang ponsel → " +
                        "Pembaruan sistem/aplikasi, atau tunggu update otomatis dari Xiaomi.",
                    style = MtzType.Caption,
                    color = MtzColors.Muted,
                )
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Selesai", color = MtzColors.Blue) } },
        containerColor = MtzColors.Cream,
        shape = RoundedCornerShape(28.dp),
    )
}

@Composable
private fun RevertGuideDialog(
    version: String?,
    onOpenAppInfo: () -> Unit,
    onOpenGetApps: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Buka kunci pemasangan 🔓", style = MtzType.Heading) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    "Aplikasi Tema ${version ?: ""} di HP kamu adalah versi update yang mengunci pemasangan tema " +
                        "dari aplikasi lain. Kembalikan ke versi pabrik sekali saja:",
                    style = MtzType.Body,
                )
                Spacer(Modifier.height(14.dp))
                HelpOption(
                    "Langkah 1 — Copot pembaruan Tema",
                    "Ketuk tombol di bawah → di halaman Info aplikasi Tema ketuk ⋮ (pojok kanan atas) → " +
                        "\"Copot pemasangan pembaruan\" → OK. Tema yang sedang dipakai mungkin kembali ke bawaan, itu normal.",
                    "Buka info aplikasi Tema",
                    onOpenAppInfo,
                )
                HelpOption(
                    "Langkah 2 — Matikan update otomatis",
                    "Supaya Tema tidak diperbarui lagi: buka GetApps → Saya → Pengaturan → matikan " +
                        "\"Pembaruan otomatis\". Jika ada di Play Store, matikan juga update otomatis untuk Tema.",
                    "Buka GetApps",
                    onOpenGetApps,
                )
                HelpOption(
                    "Langkah 3 — Kembali & pasang",
                    "Kembali ke MTZ Installer gen Z. Tab Info akan menampilkan \"Pasang otomatis: didukung ✅\". " +
                        "Buka tema lalu ketuk Pasang Sekarang ⚡ → Terapkan.",
                    "Selesai",
                    onDismiss,
                )
                Text(
                    "Jika setelah langkah 1 status masih terkunci, versi pabrik HP ini juga sudah menguncinya. " +
                        "Gunakan \"Pasang dengan aplikasi lain\" (Shizuku + HyperOS MTZ Studio) di tab Info.",
                    style = MtzType.Caption,
                    color = MtzColors.Muted,
                )
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Tutup", color = MtzColors.Ink) } },
        containerColor = MtzColors.Cream,
    )
}

@Composable
private fun HelpOption(title: String, body: String, action: String, onClick: () -> Unit) {
    Column(
        Modifier
            .padding(bottom = 12.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(14.dp),
    ) {
        Text(title, style = MtzType.Label)
        Text(body, style = MtzType.Caption, color = MtzColors.Muted, modifier = Modifier.padding(top = 2.dp, bottom = 10.dp))
        PillButton(action, onClick, small = true, container = MtzColors.Blue, content = Color.White)
    }
}

@Composable
private fun BusyOverlay(message: String) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.35f))
            .pointerInput(Unit) { detectTapGestures { } },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier.clip(RoundedCornerShape(24.dp)).background(Color.White).padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CircularProgressIndicator(color = MtzColors.Coral)
            Spacer(Modifier.height(14.dp))
            Text(message, style = MtzType.Label)
        }
    }
}
