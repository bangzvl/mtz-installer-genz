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
import com.mtzinstaller.app.ui.components.NavTab
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

        Box(Modifier.fillMaxSize().background(MtzColors.Cream)) {
            when (tab) {
                NavTab.Home -> HomeScreen(
                    themes = themes,
                    onImport = openPicker,
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
                    onOpenThemeApp = { ThemeInstaller.openThemeManager(context) },
                )
            }

            FloatingNavBar(
                current = tab,
                onSelect = { tab = it },
                modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 16.dp),
            )
            SnackbarHost(
                snackbar,
                Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(start = 16.dp, end = 16.dp, bottom = 92.dp),
            ) { data ->
                Snackbar(data, containerColor = MtzColors.Ink, contentColor = Color.White, shape = RoundedCornerShape(18.dp))
            }
        }
    }

    themes.firstOrNull { it.id == vm.selectedId }?.let { theme ->
        ThemeDetailSheet(
            theme = theme,
            busy = vm.busy != null,
            onDismiss = { vm.selectedId = null },
            onInstall = { vm.install(theme) },
            onToggleFavorite = { vm.toggleFavorite(theme) },
            onDelete = { vm.delete(theme) },
        )
    }

    vm.manualHelpPath?.let { path ->
        AlertDialog(
            onDismissRequest = { vm.manualHelpPath = null },
            title = { Text("Pasang manual") },
            text = {
                Text(
                    "Aplikasi Tema tidak mengizinkan pemasangan otomatis di perangkat ini.\n\n" +
                        "File sudah disalin ke:\n$path\n\n" +
                        "Buka aplikasi Tema → Profil → Tema → Import, lalu pilih file tersebut.",
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.manualHelpPath = null
                    ThemeInstaller.openThemeManager(context)
                }) { Text("Buka Tema", color = MtzColors.Coral) }
            },
            dismissButton = { TextButton(onClick = { vm.manualHelpPath = null }) { Text("Tutup", color = MtzColors.Ink) } },
            containerColor = MtzColors.Cream,
        )
    }

    vm.busy?.let { BusyOverlay(it) }
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
