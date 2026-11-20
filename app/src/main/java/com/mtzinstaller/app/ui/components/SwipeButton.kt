package com.mtzinstaller.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mtzinstaller.app.ui.theme.MtzColors
import com.mtzinstaller.app.ui.theme.MtzType
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Tombol "geser ke kanan": lingkaran putih berpanah diseret ke ujung kanan untuk melanjutkan.
 * Jika dilepas sebelum 70% lintasan, lingkaran memantul kembali. Mengetuk jalur akan
 * memberi animasi petunjuk (lingkaran bergeser sedikit lalu kembali).
 */
@Composable
fun SwipeButton(text: String, onComplete: () -> Unit, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    val offset = remember { Animatable(0f) }
    // Selalu pakai aksi terbaru (mis. "Lanjut" di halaman 1–2, "Masuk" di halaman 3).
    // Tanpa ini, gesture yang dibuat sekali akan terus memanggil aksi dari halaman pertama.
    val latestOnComplete by rememberUpdatedState(onComplete)
    val hint = rememberInfiniteTransition(label = "hint")
    val shimmer by hint.animateFloat(0f, 1f, infiniteRepeatable(tween(1600), RepeatMode.Restart), label = "shimmer")
    val thumb = 52.dp
    val pad = 6.dp

    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            .height(thumb + pad * 2)
            .neo(CircleShape)
            .clip(CircleShape)
            .background(MtzColors.Ink),
    ) {
        val density = LocalDensity.current
        val maxPx = with(density) { (maxWidth - thumb - pad * 2).toPx() }
        val progress = if (maxPx > 0f) (offset.value / maxPx).coerceIn(0f, 1f) else 0f

        // Isian ungu mengikuti lingkaran
        Box(
            Modifier
                .fillMaxHeight()
                .width(with(density) { (offset.value).toDp() } + thumb + pad * 2)
                .clip(CircleShape)
                .background(Brush.horizontalGradient(listOf(MtzColors.BlueDeep, MtzColors.Blue))),
        )
        // Teks + panah petunjuk berkilau
        Box(Modifier.fillMaxWidth().fillMaxHeight().padding(start = thumb + pad), contentAlignment = Alignment.Center) {
            val arrows = "›››"
            Text(
                "$text  $arrows",
                color = Color.White.copy(alpha = (0.55f + 0.45f * (1f - shimmer)) * (1f - progress)),
                style = MtzType.Label.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                modifier = Modifier.alpha(1f - progress * 0.9f),
            )
        }
        // Lingkaran yang digeser
        Box(
            Modifier
                .padding(pad)
                .offset { IntOffset(offset.value.roundToInt(), 0) }
                .size(thumb)
                .clip(CircleShape)
                .background(Color.White)
                .pointerInput(maxPx) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            scope.launch {
                                if (offset.value > maxPx * 0.7f) {
                                    offset.animateTo(maxPx, tween(120))
                                    latestOnComplete()
                                    offset.snapTo(0f)
                                } else {
                                    offset.animateTo(0f, spring(dampingRatio = 0.55f))
                                }
                            }
                        },
                        onDragCancel = { scope.launch { offset.animateTo(0f, spring()) } },
                    ) { change, drag ->
                        change.consume()
                        scope.launch { offset.snapTo((offset.value + drag).coerceIn(0f, maxPx)) }
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, "Geser ke kanan", tint = MtzColors.Ink, modifier = Modifier.size(22.dp))
        }
        // Ketuk jalur = animasi petunjuk geser
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .padding(start = thumb + pad * 2)
                .pointerInput(maxPx) {
                    detectTapGestures {
                        scope.launch {
                            offset.animateTo(maxPx * 0.28f, tween(220))
                            offset.animateTo(0f, spring(dampingRatio = 0.45f))
                        }
                    }
                },
        )
    }
}
