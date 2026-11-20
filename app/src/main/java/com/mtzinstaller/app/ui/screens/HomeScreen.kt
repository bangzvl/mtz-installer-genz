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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mtzinstaller.app.data.MtzTheme
import com.mtzinstaller.app.ui.components.CircleIconButton
import com.mtzinstaller.app.ui.components.CountChip
import com.mtzinstaller.app.ui.components.NavTab
import com.mtzinstaller.app.ui.components.PillButton
import com.mtzinstaller.app.ui.components.QuickAction
import com.mtzinstaller.app.ui.components.SectionTitle
import com.mtzinstaller.app.ui.components.Sticker
import com.mtzinstaller.app.ui.components.ThemePreview
import com.mtzinstaller.app.ui.components.Ticker
import com.mtzinstaller.app.ui.components.glass
import com.mtzinstaller.app.ui.components.neo
import com.mtzinstaller.app.ui.components.relativeTime
import com.mtzinstaller.app.ui.theme.MtzColors
import com.mtzinstaller.app.ui.theme.MtzType
import com.mtzinstaller.app.ui.theme.accentFor
import java.util.Calendar

@Composable
fun HomeScreen(
    themes: List<MtzTheme>,
    setupNeeded: Boolean,
    onSetup: () -> Unit,
    onImport: () -> Unit,
    onNavigate: (NavTab) -> Unit,
    onOpen: (MtzTheme) -> Unit,
    onInstall: (MtzTheme) -> Unit,
    onToggleFavorite: (MtzTheme) -> Unit,
) {
    val recentInstalled = themes.filter { it.lastInstalledAt != null }.sortedByDescending { it.lastInstalledAt }

    LazyColumn(
        Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 130.dp),
    ) {
        // Sapaan
        item {
            Row(
                Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier.size(46.dp).neo(CircleShape).clip(CircleShape).background(MtzColors.Orb),
                    contentAlignment = Alignment.Center,
                ) { Text("🎨", fontSize = 22.sp) }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("${greeting()} 👋", style = MtzType.Caption, color = MtzColors.Muted)
                    Text("MTZ Installer gen Z", style = MtzType.Heading, maxLines = 1)
                }
                CountChip("📦", themes.size)
            }
        }

        // Ubin aksi cepat
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                item { QuickAction("➕", "Impor", MtzColors.Blue, onImport) }
                item { QuickAction("🔍", "Jelajah", MtzColors.Lilac) { onNavigate(NavTab.Browse) } }
                item { QuickAction("🗂️", "Koleksi", MtzColors.Mint) { onNavigate(NavTab.Collection) } }
                item { QuickAction("❤️", "Favorit", MtzColors.Pink) { onNavigate(NavTab.Collection) } }
                item { QuickAction("💡", "Bantuan", MtzColors.Sun) { onNavigate(NavTab.Info) } }
            }
        }

        if (setupNeeded) {
            item {
                Row(
                    Modifier
                        .padding(start = 20.dp, end = 20.dp, bottom = 12.dp)
                        .fillMaxWidth()
                        .neo(RoundedCornerShape(22.dp), shadow = MtzColors.Sun)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFFFFF4D6), Color(0xFFFFE3A3))))
                        .clickable(onClick = onSetup)
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("🔒", fontSize = 24.sp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Setup sekali: buka kunci pemasangan", style = MtzType.Label)
                        Text(
                            "Aplikasi Tema versi ini mengunci pasang otomatis. Ketuk untuk panduan 1 menit.",
                            style = MtzType.Caption,
                            color = MtzColors.Ink.copy(alpha = 0.7f),
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    PillButton("Mulai", onSetup, small = true)
                }
            }
        }

        item {
            Ticker("✦ tema baru tiap hari   ✦ pasang cuma 1 klik   ✦ no ribet club   ✦ glow up HP kamu   ")
        }

        item {
            Row(
                Modifier.padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SectionTitle("Koleksi Terbaru")
                Spacer(Modifier.width(8.dp))
                Sticker("baru", color = MtzColors.Blue, content = Color.White, fontSize = 11)
                Spacer(Modifier.weight(1f))
                Text(
                    "Lihat semua",
                    style = MtzType.Label.copy(fontSize = 13.sp),
                    color = MtzColors.Blue,
                    modifier = Modifier.clip(CircleShape).clickable { onNavigate(NavTab.Collection) }.padding(6.dp),
                )
            }
        }

        item {
            if (themes.isEmpty()) {
                EmptyFeatureCard(onImport, Modifier.padding(horizontal = 20.dp))
            } else {
                LazyRow(
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    items(themes.take(10), key = { it.id }) { theme ->
                        FeatureCard(theme, onOpen = { onOpen(theme) }, onToggleFavorite = { onToggleFavorite(theme) })
                    }
                }
            }
        }

        item { SectionTitle("Terakhir Dipasang", modifier = Modifier.padding(start = 20.dp, top = 12.dp, bottom = 10.dp)) }

        item {
            Column(Modifier.padding(horizontal = 20.dp).fillMaxWidth().glass(RoundedCornerShape(24.dp))) {
                if (recentInstalled.isEmpty()) {
                    Text(
                        "Belum ada tema yang dipasang. Pilih tema di atas lalu ketuk \"Pasang\".",
                        style = MtzType.Body,
                        color = MtzColors.Muted,
                        modifier = Modifier.padding(18.dp),
                    )
                } else {
                    recentInstalled.take(5).forEach { theme ->
                        RecentRow(theme, onOpen = { onOpen(theme) }, onInstall = { onInstall(theme) })
                    }
                }
            }
        }
    }
}

private fun greeting(): String = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
    in 4..10 -> "Selamat pagi"
    in 11..14 -> "Selamat siang"
    in 15..17 -> "Selamat sore"
    else -> "Selamat malam"
}

@Composable
private fun FeatureCard(theme: MtzTheme, onOpen: () -> Unit, onToggleFavorite: () -> Unit) {
    val shape = RoundedCornerShape(26.dp)
    Box(
        Modifier
            .width(184.dp)
            .height(250.dp)
            .neo(shape, shadow = accentFor(theme.id))
            .clip(shape)
            .clickable(onClick = onOpen),
    ) {
        ThemePreview(theme.previewPath, theme.title, accentFor(theme.id), Modifier.fillMaxSize(), RoundedCornerShape(0.dp))
        // Scrim bawah agar teks putih terbaca
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.45f to Color.Transparent,
                        1f to Color(0xCC0E1B3D),
                    ),
                ),
        )
        Row(Modifier.padding(10.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.clip(CircleShape).background(Color.White.copy(alpha = 0.85f)).padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(if (theme.version == "-") "MTZ" else "v${theme.version}", style = MtzType.Caption.copy(fontSize = 11.sp), color = MtzColors.Ink)
            }
            Spacer(Modifier.weight(1f))
            CircleIconButton(
                if (theme.favorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                "Favorit",
                onToggleFavorite,
                container = Color.White.copy(alpha = 0.9f),
                tint = MtzColors.Pink,
                size = 34.dp,
            )
        }
        Column(Modifier.align(Alignment.BottomStart).padding(14.dp)) {
            Text(
                theme.title,
                style = MtzType.Title.copy(fontSize = 19.sp, lineHeight = 22.sp),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                "oleh ${theme.author}",
                style = MtzType.Caption,
                color = Color.White.copy(alpha = 0.85f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun EmptyFeatureCard(onImport: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(28.dp)
    Column(
        modifier
            .fillMaxWidth()
            .neo(shape)
            .clip(shape)
            .background(Brush.linearGradient(listOf(MtzColors.Sky, MtzColors.Blue)))
            .padding(22.dp),
    ) {
        Sticker("Mulai", color = Color.White.copy(alpha = 0.9f), content = MtzColors.Blue, fontSize = 12)
        Spacer(Modifier.height(14.dp))
        Text("Belum ada tema", style = MtzType.Title, color = Color.White)
        Spacer(Modifier.height(6.dp))
        Text(
            "Cari tema di tab Jelajah atau impor file .mtz dari HP untuk melihat pratinjau dan memasangnya.",
            style = MtzType.Body,
            color = Color.White.copy(alpha = 0.92f),
        )
        Spacer(Modifier.height(18.dp))
        PillButton("Impor .mtz", onImport, small = true, container = Color.White, content = MtzColors.Ink)
    }
}

@Composable
private fun RecentRow(theme: MtzTheme, onOpen: () -> Unit, onInstall: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onOpen).padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ThemePreview(
            theme.previewPath, theme.title, accentFor(theme.id),
            Modifier.size(48.dp).border(2.dp, Color.White, RoundedCornerShape(15.dp)),
            RoundedCornerShape(15.dp),
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(theme.title, style = MtzType.Label, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(relativeTime(theme.lastInstalledAt ?: theme.addedAt), style = MtzType.Caption, color = MtzColors.Muted)
        }
        PillButton("Pasang lagi", onInstall, small = true, container = MtzColors.Lime, content = MtzColors.Blue)
    }
}
