package com.mtzinstaller.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mtzinstaller.app.data.MtzTheme
import com.mtzinstaller.app.ui.components.CircleIconButton
import com.mtzinstaller.app.ui.components.CountChip
import com.mtzinstaller.app.ui.components.PillButton
import com.mtzinstaller.app.ui.components.SectionTitle
import com.mtzinstaller.app.ui.components.Sticker
import com.mtzinstaller.app.ui.components.Ticker
import com.mtzinstaller.app.ui.components.neo
import com.mtzinstaller.app.ui.components.ThemePreview
import com.mtzinstaller.app.ui.components.relativeTime
import com.mtzinstaller.app.ui.theme.MtzColors
import com.mtzinstaller.app.ui.theme.MtzType
import com.mtzinstaller.app.ui.theme.accentFor

@Composable
fun HomeScreen(
    themes: List<MtzTheme>,
    setupNeeded: Boolean,
    onSetup: () -> Unit,
    onImport: () -> Unit,
    onOpen: (MtzTheme) -> Unit,
    onInstall: (MtzTheme) -> Unit,
    onToggleFavorite: (MtzTheme) -> Unit,
) {
    val recentInstalled = themes.filter { it.lastInstalledAt != null }.sortedByDescending { it.lastInstalledAt }

    LazyColumn(
        Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 120.dp),
    ) {
        item {
          Column {
            Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Tema", style = MtzType.Display)
                Text("🎨", fontSize = 34.sp, modifier = Modifier.padding(start = 6.dp))
                Spacer(Modifier.weight(1f))
                CountChip("📦", themes.size)
            }
            Text(
                "Kumpulkan tema .mtz favoritmu, lihat pratinjaunya, dan pasang ke HP Xiaomi kamu tanpa ribet.",
                style = MtzType.Body,
                color = MtzColors.Ink.copy(alpha = 0.75f),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
            )
            Ticker(
                "✦ tema baru tiap hari ✦ pasang cuma 1 klik ✦ no ribet club ✦ glow up HP kamu ✦ aesthetic only ",
                Modifier.padding(top = 12.dp, bottom = 6.dp),
            )
          }
        }

        if (setupNeeded) {
            item {
                Row(
                    Modifier
                        .padding(start = 20.dp, end = 20.dp, top = 12.dp)
                        .fillMaxWidth()
                        .neo(RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(MtzColors.Sun)
                        .clickable(onClick = onSetup)
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("🔒", fontSize = 24.sp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Setup sekali: buka kunci pemasangan", style = MtzType.Label)
                        Text("Aplikasi Tema versi ini mengunci pasang otomatis. Ketuk untuk panduan 1 menit.", style = MtzType.Caption, color = MtzColors.Ink.copy(alpha = 0.75f))
                    }
                    Spacer(Modifier.width(8.dp))
                    PillButton("Mulai", onSetup, small = true)
                }
            }
        }

        item { ImportCard(onImport, Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) }

        item {
            Row(Modifier.padding(start = 20.dp, top = 18.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                SectionTitle("Koleksi Terbaru", "🔥")
                Spacer(Modifier.width(10.dp))
                Sticker("baru!", color = MtzColors.Pink, content = Color.White, angle = 5f, fontSize = 11)
            }
        }

        item {
            if (themes.isEmpty()) {
                EmptyFeatureCard(onImport, Modifier.padding(horizontal = 20.dp))
            } else {
                LazyRow(contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    items(themes.take(10), key = { it.id }) { theme ->
                        FeatureCard(theme, onOpen = { onOpen(theme) }, onToggleFavorite = { onToggleFavorite(theme) })
                    }
                }
            }
        }

        item { SectionTitle("Terakhir Dipasang", modifier = Modifier.padding(start = 20.dp, top = 24.dp, bottom = 6.dp)) }

        if (recentInstalled.isEmpty()) {
            item {
                Text(
                    "Belum ada tema yang dipasang. Pilih tema di atas lalu ketuk \"Pasang\".",
                    style = MtzType.Body,
                    color = MtzColors.Muted,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                )
            }
        } else {
            items(recentInstalled.take(5), key = { "recent-${it.id}" }) { theme ->
                RecentRow(theme, onOpen = { onOpen(theme) }, onInstall = { onInstall(theme) })
            }
        }
    }
}

@Composable
private fun ImportCard(onImport: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth().neo(RoundedCornerShape(20.dp)).clip(RoundedCornerShape(20.dp)).background(Color.White).clickable(onClick = onImport).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("👑", fontSize = 24.sp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("Import file .mtz", style = MtzType.Label)
            Text("Pilih tema dari penyimpanan & pasang instan", style = MtzType.Caption, color = MtzColors.Muted)
        }
        Spacer(Modifier.width(8.dp))
        PillButton("Pilih", onImport, small = true, container = MtzColors.Lime, content = MtzColors.Ink)
    }
}

@Composable
private fun FeatureCard(theme: MtzTheme, onOpen: () -> Unit, onToggleFavorite: () -> Unit) {
    val accent = accentFor(theme.id)
    val onAccent = if (accent.luminance() < 0.5f) Color.White else MtzColors.Ink
    Column(
        Modifier
            .width(172.dp)
            .height(238.dp)
            .neo(RoundedCornerShape(22.dp))
            .clip(RoundedCornerShape(22.dp))
            .background(accent)
            .clickable(onClick = onOpen)
            .padding(11.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.clip(CircleShape).background(MtzColors.Ink).padding(horizontal = 10.dp, vertical = 5.dp)) {
                Text(if (theme.version == "-") "MTZ" else "v${theme.version}", color = Color.White, style = MtzType.Label.copy(fontSize = 11.sp))
            }
            Spacer(Modifier.weight(1f))
            CircleIconButton(
                if (theme.favorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                "Favorit",
                onToggleFavorite,
                tint = MtzColors.Coral,
                size = 32.dp,
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(theme.title, style = MtzType.Title.copy(fontSize = 20.sp, lineHeight = 22.sp), color = onAccent, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text("oleh ${theme.author}", style = MtzType.Caption, color = onAccent.copy(alpha = 0.85f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(8.dp))
        ThemePreview(
            theme.previewPath, theme.title, Color.White.copy(alpha = 0.35f),
            Modifier.fillMaxWidth().weight(1f).border(2.dp, MtzColors.Ink, RoundedCornerShape(14.dp)),
            RoundedCornerShape(14.dp),
        )
    }
}

@Composable
private fun EmptyFeatureCard(onImport: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxWidth().neo(RoundedCornerShape(28.dp)).clip(RoundedCornerShape(28.dp)).background(MtzColors.Coral).padding(22.dp),
    ) {
        Box(Modifier.clip(CircleShape).background(MtzColors.Ink).padding(horizontal = 16.dp, vertical = 9.dp)) {
            Text("Mulai", color = Color.White, style = MtzType.Label.copy(fontSize = 13.sp))
        }
        Spacer(Modifier.height(14.dp))
        Text("Belum Ada Tema", style = MtzType.Title, color = Color.White)
        Spacer(Modifier.height(6.dp))
        Text(
            "Unduh file .mtz dari situs atau grup tema favoritmu, lalu impor di sini untuk melihat pratinjau dan memasangnya.",
            style = MtzType.Body,
            color = Color.White.copy(alpha = 0.9f),
        )
        Spacer(Modifier.height(18.dp))
        PillButton("Import .mtz", onImport, small = true)
    }
}

@Composable
private fun RecentRow(theme: MtzTheme, onOpen: () -> Unit, onInstall: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onOpen).padding(horizontal = 20.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ThemePreview(theme.previewPath, theme.title, accentFor(theme.id), Modifier.size(46.dp).border(2.dp, MtzColors.Ink, RoundedCornerShape(14.dp)), RoundedCornerShape(14.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(theme.title, style = MtzType.Label, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(relativeTime(theme.lastInstalledAt ?: theme.addedAt), style = MtzType.Caption, color = MtzColors.Muted)
        }
        PillButton("Pasang lagi", onInstall, container = MtzColors.Coral, small = true)
    }
}
