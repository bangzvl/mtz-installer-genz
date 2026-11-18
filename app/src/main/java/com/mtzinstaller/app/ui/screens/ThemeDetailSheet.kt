package com.mtzinstaller.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mtzinstaller.app.data.MtzTheme
import com.mtzinstaller.app.ui.components.CircleIconButton
import com.mtzinstaller.app.ui.components.PillButton
import com.mtzinstaller.app.ui.components.ThemePreview
import com.mtzinstaller.app.ui.components.formatSize
import com.mtzinstaller.app.ui.components.neo
import com.mtzinstaller.app.ui.theme.MtzColors
import com.mtzinstaller.app.ui.theme.MtzType
import com.mtzinstaller.app.ui.theme.accentFor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeDetailSheet(
    theme: MtzTheme,
    busy: Boolean,
    onDismiss: () -> Unit,
    onInstall: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit,
) {
    var confirmDelete by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = MtzColors.Cream) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 20.dp)
                .navigationBarsPadding(),
        ) {
            ThemePreview(
                theme.previewPath, theme.title, accentFor(theme.id),
                Modifier.fillMaxWidth().height(300.dp).neo(RoundedCornerShape(24.dp), offset = 4.dp),
                RoundedCornerShape(24.dp),
            )
            Spacer(Modifier.height(18.dp))
            Text(theme.title, style = MtzType.Title)
            Text("oleh ${theme.author}", style = MtzType.Body, color = MtzColors.Muted)
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatChip("Versi", theme.version, MtzColors.Sun, Modifier.weight(1f))
                StatChip("Ukuran", formatSize(theme.sizeBytes), MtzColors.Sky, Modifier.weight(1f))
                StatChip("Dipasang", "${theme.installCount}x", MtzColors.Mint, Modifier.weight(1f))
            }
            if (theme.description.isNotBlank()) {
                Spacer(Modifier.height(14.dp))
                Text(theme.description, style = MtzType.Body, color = MtzColors.Ink.copy(alpha = 0.8f))
            }
            Spacer(Modifier.height(22.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                PillButton(
                    if (busy) "Memproses…" else "Pasang Sekarang ⚡",
                    onInstall,
                    Modifier.weight(1f),
                    container = MtzColors.Lime,
                    content = MtzColors.Ink,
                    enabled = !busy,
                )
                Spacer(Modifier.width(10.dp))
                CircleIconButton(
                    if (theme.favorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    "Favorit", onToggleFavorite, tint = MtzColors.Coral, size = 54.dp,
                )
                Spacer(Modifier.width(8.dp))
                CircleIconButton(Icons.Filled.Delete, "Hapus", { confirmDelete = true }, tint = MtzColors.Muted, size = 54.dp)
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Hapus tema?") },
            text = { Text("\"${theme.title}\" akan dihapus dari koleksi. Tema yang sedang aktif di HP tidak terpengaruh.") },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; onDelete() }) { Text("Hapus", color = MtzColors.Coral) }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Batal", color = MtzColors.Ink) } },
            containerColor = MtzColors.Cream,
        )
    }
}

@Composable
private fun StatChip(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(modifier.neo(RoundedCornerShape(16.dp)).clip(RoundedCornerShape(16.dp)).background(color).padding(horizontal = 12.dp, vertical = 10.dp)) {
        Text(label, style = MtzType.Caption, color = MtzColors.Ink.copy(alpha = 0.7f))
        Text(value, style = MtzType.Label, maxLines = 1)
    }
}
