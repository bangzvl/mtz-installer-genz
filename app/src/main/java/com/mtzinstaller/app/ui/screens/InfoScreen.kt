package com.mtzinstaller.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mtzinstaller.app.ui.components.PillButton
import com.mtzinstaller.app.ui.components.glass
import com.mtzinstaller.app.ui.components.neo
import androidx.compose.ui.graphics.Brush
import com.mtzinstaller.app.ui.theme.MtzColors
import com.mtzinstaller.app.ui.theme.MtzType

@Composable
fun InfoScreen(
    themeManagerAvailable: Boolean,
    themeManagerVersion: String?,
    autoApply: Boolean,
    locked: Boolean,
    needsRevert: Boolean,
    onRevertGuide: () -> Unit,
    isFactoryVersion: Boolean,
    onRestoreLatest: () -> Unit,
    helpers: List<String>,
    report: () -> String,
    onOpenThemeApp: () -> Unit,
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    fun openUrl(url: String) = runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 120.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Info", style = MtzType.Display)
            Text("💡", fontSize = 34.sp, modifier = Modifier.padding(start = 6.dp))
        }
        Spacer(Modifier.height(14.dp))

        // Status aplikasi Tema
        val statusColor = if (themeManagerAvailable && autoApply) MtzColors.Mint else if (themeManagerAvailable) MtzColors.Sun else MtzColors.Coral
        Column(Modifier.fillMaxWidth().neo(RoundedCornerShape(24.dp), shadow = statusColor).clip(RoundedCornerShape(24.dp)).background(Brush.linearGradient(listOf(statusColor.copy(alpha = 0.55f), statusColor))).padding(20.dp)) {
            Text(if (themeManagerAvailable) "Aplikasi Tema terdeteksi ✅" else "Aplikasi Tema tidak ditemukan", style = MtzType.Title.copy(fontSize = 22.sp), color = MtzColors.Ink)
            Spacer(Modifier.height(6.dp))
            Text(
                if (themeManagerAvailable) {
                    "Versi aplikasi Tema: ${themeManagerVersion ?: "?"}" +
                        (if (isFactoryVersion) " (versi pabrik)\n" else " (versi update)\n") +
                        when {
                            autoApply -> "Pasang otomatis: didukung ✅"
                            locked -> "Pasang otomatis: DIKUNCI 🔒 di versi ini."
                            else -> "Pasang otomatis: tidak tersedia di versi ini."
                        }
                }
                else "Fitur pasang otomatis hanya berfungsi di HP Xiaomi/Redmi/POCO dengan MIUI atau HyperOS.",
                style = MtzType.Body,
                color = MtzColors.Ink.copy(alpha = 0.8f),
            )
            if (themeManagerAvailable) {
                Spacer(Modifier.height(14.dp))
                Row {
                    if (needsRevert) {
                        PillButton("Buka kunci 🔓", onRevertGuide, small = true, container = MtzColors.Blue, content = Color.White)
                        Spacer(Modifier.width(10.dp))
                    }
                    if (isFactoryVersion) {
                        PillButton("Ke versi terbaru ⬆️", onRestoreLatest, small = true, container = MtzColors.Blue, content = Color.White)
                        Spacer(Modifier.width(10.dp))
                    }
                    PillButton("Buka Tema", onOpenThemeApp, small = true, container = Color.White, content = MtzColors.Ink)
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        // Cek kompatibilitas — hanya daftar centang (tahan lama kartu untuk menyalin laporan teknis)
        Column(
            Modifier
                .fillMaxWidth()
                .glass(RoundedCornerShape(24.dp))
                .pointerInput(Unit) {
                    detectTapGestures(onLongPress = {
                        clipboard.setText(AnnotatedString(report()))
                        Toast.makeText(context, "Laporan teknis disalin", Toast.LENGTH_SHORT).show()
                    })
                }
                .padding(18.dp),
        ) {
            Text("Cek kompatibilitas", style = MtzType.Heading)
            Spacer(Modifier.height(10.dp))
            CheckItem(themeManagerAvailable, "Aplikasi Tema terdeteksi", themeManagerVersion?.let { "Versi $it" } ?: "Tidak ditemukan")
            CheckItem(autoApply, if (autoApply) "Pasang otomatis didukung" else "Pasang otomatis terkunci", if (autoApply) "Siap memasang tema" else "Ketuk \"Buka kunci\" di atas, cukup sekali")
            CheckItem(true, "Penyimpanan siap", "Tema disalin ke Download/MtzInstaller")
            CheckItem(true, "Android ${android.os.Build.VERSION.RELEASE} didukung", "MTZ Installer gen Z kompatibel")
        }

        Spacer(Modifier.height(16.dp))
        InfoCard("Aplikasi pembantu (opsional) 🧩") {
            Text(
                "Di HyperOS versi baru, Xiaomi menutup pemasangan tema dari aplikasi lain. Kalau tema tetap " +
                    "tidak bisa dipasang, pasang aplikasi pembantu berikut. Setelah itu, di detail tema ketuk " +
                    "\"Pasang dengan aplikasi lain\" dan pilih aplikasinya.",
                style = MtzType.Body,
                color = MtzColors.Ink.copy(alpha = 0.8f),
            )
            Spacer(Modifier.height(8.dp))
            Step(1, MtzColors.Coral, "Shizuku: memberi akses lanjutan tanpa root, dinyalakan lewat Wireless debugging (tanpa PC).")
            Step(2, MtzColors.Lilac, "HyperOS MTZ Studio (open source): memasang .mtz memakai Shizuku atau root.")
            Spacer(Modifier.height(8.dp))
            Text(
                if (helpers.isEmpty()) "Terpasang: belum ada" else "Terpasang: ${helpers.joinToString()}",
                style = MtzType.Label,
            )
            Spacer(Modifier.height(12.dp))
            Row {
                PillButton("Shizuku", { openUrl("https://shizuku.rikka.app/") }, small = true)
                Spacer(Modifier.width(10.dp))
                PillButton("MTZ Studio", { openUrl("https://github.com/GloriousApps/HyperOS-MTZ-Studio/releases") }, small = true)
            }
        }

        Spacer(Modifier.height(16.dp))
        InfoCard("Cara kerja") {
            Step(1, MtzColors.Coral, "Ambil tema dari tab Jelajah (MiuiThemez) atau impor file .mtz dari penyimpanan.")
            Step(2, MtzColors.Sun, "Aplikasi membaca description.xml & gambar pratinjau di dalam file.")
            Step(3, MtzColors.Sky, "Saat \"Pasang\", file disalin ke Download/MtzInstaller.")
            Step(4, MtzColors.Lilac, "Aplikasi Tema MIUI dibuka untuk menerapkan tema tersebut.")
        }

        Spacer(Modifier.height(16.dp))
        InfoCard("Catatan penting") {
            Text(
                "• Di beberapa versi HyperOS terbaru, Xiaomi membatasi tema pihak ketiga. Jika tema gagal diterapkan, " +
                    "biasanya perlu versi aplikasi Tema yang lebih lama atau mengatur wilayah ke negara yang mendukung tema.\n\n" +
                    "• Jika pemasangan otomatis gagal, buka aplikasi Tema → Profil → Tema → Import, lalu pilih file di folder Download/MtzInstaller.\n\n" +
                    "• Pasang hanya tema dari sumber yang kamu percaya.",
                style = MtzType.Body,
                color = MtzColors.Ink.copy(alpha = 0.8f),
            )
        }
    }
}

@Composable
private fun CheckItem(ok: Boolean, title: String, subtitle: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(30.dp)
                .neo(RoundedCornerShape(10.dp), offset = 2.dp, shadow = if (ok) MtzColors.Mint else MtzColors.Sun)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (ok) Brush.linearGradient(listOf(Color(0xFF7BE6C2), MtzColors.Mint))
                    else Brush.linearGradient(listOf(Color(0xFFFFDD85), MtzColors.Sun)),
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (ok) Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(19.dp))
            else Text("!", style = MtzType.Label.copy(fontWeight = FontWeight.Black), color = Color.White)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MtzType.Label)
            Text(subtitle, style = MtzType.Caption, color = MtzColors.Muted)
        }
    }
}

@Composable
private fun InfoCard(title: String, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth().glass(RoundedCornerShape(24.dp)).padding(20.dp)) {
        Text(title, style = MtzType.Heading)
        Spacer(Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun Step(n: Int, color: Color, text: String) {
    Row(Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(32.dp).clip(CircleShape).background(color), contentAlignment = Alignment.Center) {
            Text("$n", style = MtzType.Label, color = Color.White)
        }
        Spacer(Modifier.width(12.dp))
        Text(text, style = MtzType.Body, modifier = Modifier.weight(1f))
    }
}
