package com.mtzinstaller.app.ui.screens

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
import com.mtzinstaller.app.ui.theme.MtzColors
import com.mtzinstaller.app.ui.theme.MtzType

@Composable
fun InfoScreen(themeManagerAvailable: Boolean, onOpenThemeApp: () -> Unit) {
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
        val statusColor = if (themeManagerAvailable) MtzColors.Mint else MtzColors.Coral
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(statusColor).padding(20.dp)) {
            Text(if (themeManagerAvailable) "Aplikasi Tema terdeteksi ✅" else "Aplikasi Tema tidak ditemukan", style = MtzType.Title.copy(fontSize = 22.sp), color = MtzColors.Ink)
            Spacer(Modifier.height(6.dp))
            Text(
                if (themeManagerAvailable) "Ponsel ini memiliki com.android.thememanager, jadi tema bisa diterapkan langsung."
                else "Fitur pasang otomatis hanya berfungsi di HP Xiaomi/Redmi/POCO dengan MIUI atau HyperOS.",
                style = MtzType.Body,
                color = MtzColors.Ink.copy(alpha = 0.8f),
            )
            if (themeManagerAvailable) {
                Spacer(Modifier.height(14.dp))
                PillButton("Buka Aplikasi Tema", onOpenThemeApp, small = true)
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
private fun InfoCard(title: String, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Color.White).padding(20.dp)) {
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
