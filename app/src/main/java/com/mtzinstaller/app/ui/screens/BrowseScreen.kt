package com.mtzinstaller.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mtzinstaller.app.ui.components.Sticker
import com.mtzinstaller.app.ui.theme.MtzColors
import com.mtzinstaller.app.ui.theme.MtzType

/** Tab Jelajah: langsung membuka MiuiThemez, tanpa pengaturan apa pun. */
@Composable
fun BrowseScreen(
    downloads: Map<String, Float>,
    onWebDownload: (url: String, fileName: String, userAgent: String?, cookie: String?, referer: String?) -> Unit,
) {
    MiuiThemezBrowser(
        downloads = downloads,
        onDownload = onWebDownload,
        header = {
            Column(Modifier.statusBarsPadding()) {
                Row(Modifier.padding(top = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Jelajah", style = MtzType.Display)
                    Text("🔍", fontSize = 30.sp, modifier = Modifier.padding(start = 6.dp))
                    Spacer(Modifier.width(10.dp))
                    Sticker("MiuiThemez", color = MtzColors.Pink, content = androidx.compose.ui.graphics.Color.White, angle = 5f, fontSize = 12)
                }
                Text(
                    "Cari tema, tekan Download, dan tema otomatis masuk ke Koleksi.",
                    style = MtzType.Body,
                    color = MtzColors.Muted,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        },
    )
}
