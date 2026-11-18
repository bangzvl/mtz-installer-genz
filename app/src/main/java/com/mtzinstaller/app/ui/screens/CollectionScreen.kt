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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mtzinstaller.app.data.MtzTheme
import com.mtzinstaller.app.ui.components.CountChip
import com.mtzinstaller.app.ui.components.PillButton
import com.mtzinstaller.app.ui.components.ThemePreview
import com.mtzinstaller.app.ui.components.formatSize
import com.mtzinstaller.app.ui.components.neo
import com.mtzinstaller.app.ui.theme.MtzColors
import com.mtzinstaller.app.ui.theme.MtzType
import com.mtzinstaller.app.ui.theme.accentFor

@Composable
fun CollectionScreen(
    themes: List<MtzTheme>,
    onOpen: (MtzTheme) -> Unit,
    onImport: () -> Unit,
) {
    val favCount = themes.count { it.favorite }
    val lastInstalledId = themes.filter { it.lastInstalledAt != null }.maxByOrNull { it.lastInstalledAt!! }?.id

    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp)) {
            Text("Koleksi 🗂️", style = MtzType.Heading, modifier = Modifier.align(Alignment.Center))
            CountChip("❤️", favCount, Modifier.align(Alignment.CenterEnd))
        }

        if (themes.isEmpty()) {
            Column(
                Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("🗃️", fontSize = 56.sp)
                Spacer(Modifier.height(12.dp))
                Text("Koleksimu masih kosong", style = MtzType.Heading)
                Spacer(Modifier.height(6.dp))
                Text("Tema yang kamu impor akan muncul di sini.", style = MtzType.Body, color = MtzColors.Muted, textAlign = TextAlign.Center)
                Spacer(Modifier.height(20.dp))
                PillButton("Import .mtz", onImport)
            }
        } else {
            CollectionList(themes, lastInstalledId, onOpen)
        }
    }
}

@Composable
private fun CollectionList(themes: List<MtzTheme>, lastInstalledId: String?, onOpen: (MtzTheme) -> Unit) {
        val podium = themes.sortedWith(
            compareByDescending<MtzTheme> { it.favorite }
                .thenByDescending { it.installCount }
                .thenByDescending { it.addedAt },
        ).take(3)

        LazyColumn(contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 120.dp)) {
            item {
                Row(
                    Modifier.fillMaxWidth().padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    podium.getOrNull(1)?.let { PodiumItem(it, 2, 72.dp, MtzColors.Pink) { onOpen(it) } }
                    podium.getOrNull(0)?.let { PodiumItem(it, 1, 96.dp, MtzColors.Lime, Modifier.offset(y = (-14).dp)) { onOpen(it) } }
                    podium.getOrNull(2)?.let { PodiumItem(it, 3, 72.dp, MtzColors.Sky) { onOpen(it) } }
                }
            }
            item {
                Column(Modifier.padding(end = 3.dp, bottom = 3.dp).fillMaxWidth().neo(RoundedCornerShape(24.dp)).clip(RoundedCornerShape(24.dp)).background(Color.White)) {
                    themes.forEachIndexed { index, theme ->
                        CollectionRow(theme, isActive = theme.id == lastInstalledId) { onOpen(theme) }
                        if (index < themes.lastIndex) {
                            HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = MtzColors.Line)
                        }
                    }
                }
            }
        }
}

@Composable
private fun PodiumItem(
    theme: MtzTheme,
    rank: Int,
    size: Dp,
    medal: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(modifier.width(104.dp).clickable(onClick = onClick), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.BottomCenter) {
            ThemePreview(
                theme.previewPath, theme.title, accentFor(theme.id),
                Modifier.padding(bottom = 10.dp).size(size).neo(CircleShape, stroke = 3.dp, shadow = if (rank == 1) MtzColors.Lime else MtzColors.Ink),
                CircleShape,
            )
            Box(
                Modifier.size(26.dp).clip(CircleShape).background(medal).border(2.dp, MtzColors.Ink, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text("$rank", style = MtzType.Label.copy(fontSize = 13.sp))
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(theme.title, style = MtzType.Label, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
        Text("${theme.installCount}x dipasang", style = MtzType.Caption, color = MtzColors.Muted)
    }
}

@Composable
private fun CollectionRow(theme: MtzTheme, isActive: Boolean, onClick: () -> Unit) {
    val accent = accentFor(theme.id)
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ThemePreview(theme.previewPath, theme.title, accent, Modifier.size(46.dp).border(2.dp, MtzColors.Ink, CircleShape), CircleShape)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(theme.title, style = MtzType.Label, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                "${if (theme.version == "-") "" else "v${theme.version} · "}${formatSize(theme.sizeBytes)}",
                style = MtzType.Caption,
                color = MtzColors.Muted,
            )
        }
        if (isActive) {
            Box(Modifier.clip(CircleShape).background(MtzColors.Ink).padding(horizontal = 14.dp, vertical = 7.dp)) {
                Text("Aktif", color = Color.White, style = MtzType.Label.copy(fontSize = 12.sp))
            }
            Spacer(Modifier.width(8.dp))
        }
        Box(Modifier.size(36.dp).clip(CircleShape).background(accent).border(2.dp, MtzColors.Ink, CircleShape), contentAlignment = Alignment.Center) {
            Text(if (theme.favorite) "♥" else "${theme.installCount}", color = Color.White, style = MtzType.Label.copy(fontSize = 13.sp))
        }
    }
}
