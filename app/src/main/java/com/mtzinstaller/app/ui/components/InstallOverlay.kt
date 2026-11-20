package com.mtzinstaller.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import com.mtzinstaller.app.ui.theme.MtzColors
import com.mtzinstaller.app.ui.theme.MtzType
import com.mtzinstaller.app.ui.theme.accentFor

private val STEPS = listOf(
    "Memeriksa file tema",
    "Menyalin ke folder Download",
    "Membuka aplikasi Tema",
)

/**
 * Overlay pemasangan: pratinjau tema dalam cincin bercahaya yang berputar, progress bar
 * bergradien, dan daftar langkah yang tercentang satu per satu.
 */
@Composable
fun InstallOverlay(
    title: String,
    previewPath: String?,
    colorKey: String,
    step: Int,
    done: Boolean,
    failed: Boolean,
) {
    val infinite = rememberInfiniteTransition(label = "install")
    val angle by infinite.animateFloat(0f, 360f, infiniteRepeatable(tween(1400, easing = LinearEasing)), label = "ring")
    val breathe by infinite.animateFloat(0.96f, 1.04f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "breathe")
    val progress by animateFloatAsState(
        targetValue = if (done || failed) 1f else (step + 1) / (STEPS.size + 1f),
        animationSpec = tween(450),
        label = "progress",
    )
    val badge by animateFloatAsState(
        targetValue = if (done || failed) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "badge",
    )
    val accent = accentFor(colorKey)
    val visible = remember { androidx.compose.animation.core.MutableTransitionState(false).apply { targetState = true } }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0x660E1B3D))
            .pointerInput(Unit) { detectTapGestures { } },
        contentAlignment = Alignment.Center,
    ) {
        AnimatedVisibility(visibleState = visible, enter = fadeIn(tween(200)) + scaleIn(initialScale = 0.9f)) {
            Column(
                Modifier
                    .padding(horizontal = 28.dp)
                    .fillMaxWidth()
                    .glass(RoundedCornerShape(32.dp))
                    .background(Color.White.copy(alpha = 0.6f))
                    .padding(horizontal = 24.dp, vertical = 26.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Pratinjau + cincin bercahaya
                Box(Modifier.size(136.dp), contentAlignment = Alignment.Center) {
                    Canvas(Modifier.fillMaxSize().rotate(if (done || failed) 0f else angle)) {
                        val stroke = 6.dp.toPx()
                        val brush = when {
                            done -> Brush.sweepGradient(listOf(MtzColors.Mint, MtzColors.Mint))
                            failed -> Brush.sweepGradient(listOf(MtzColors.Sun, MtzColors.Sun))
                            else -> Brush.sweepGradient(listOf(Color.Transparent, MtzColors.Sky, MtzColors.Blue, MtzColors.Lilac))
                        }
                        drawArc(
                            brush = brush,
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = stroke, cap = StrokeCap.Round),
                            topLeft = androidx.compose.ui.geometry.Offset(stroke / 2, stroke / 2),
                            size = androidx.compose.ui.geometry.Size(size.width - stroke, size.height - stroke),
                        )
                    }
                    ThemePreview(
                        previewPath, title, accent,
                        Modifier.size(112.dp).scale(if (done || failed) 1f else breathe).border(3.dp, Color.White, CircleShape),
                        CircleShape,
                    )
                    Box(
                        Modifier
                            .align(Alignment.BottomEnd)
                            .padding(6.dp)
                            .size(36.dp)
                            .scale(badge)
                            .clip(CircleShape)
                            .background(if (failed) MtzColors.Sun else MtzColors.Mint)
                            .border(3.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (failed) Text("!", style = MtzType.Label, color = Color.White)
                        else Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }

                Spacer(Modifier.height(18.dp))
                Text(
                    when {
                        done -> "Siap diterapkan ✨"
                        failed -> "Butuh 1 langkah lagi"
                        else -> "Memasang tema…"
                    },
                    style = MtzType.Caption,
                    color = if (failed) Color(0xFFB7791F) else MtzColors.Blue,
                )
                Text(
                    title,
                    style = MtzType.Title.copy(textAlign = TextAlign.Center),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(Modifier.height(16.dp))
                // Progress bar bergradien
                Box(Modifier.fillMaxWidth().height(10.dp).clip(CircleShape).background(MtzColors.Line)) {
                    Box(
                        Modifier
                            .fillMaxWidth(progress)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(
                                if (failed) Brush.horizontalGradient(listOf(MtzColors.Sun, MtzColors.Peach))
                                else Brush.horizontalGradient(listOf(MtzColors.Sky, MtzColors.Blue, MtzColors.Lilac)),
                            ),
                    )
                }

                Spacer(Modifier.height(18.dp))
                STEPS.forEachIndexed { i, label ->
                    val state = when {
                        i < step || done -> 2           // selesai
                        i == step && !failed -> 1       // berjalan
                        failed && i == STEPS.lastIndex -> 3
                        else -> 0                        // menunggu
                    }
                    StepRow(label, state)
                }
            }
        }
    }
}

@Composable
private fun StepRow(label: String, state: Int) {
    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(24.dp), contentAlignment = Alignment.Center) {
            when (state) {
                2 -> Box(Modifier.size(22.dp).clip(CircleShape).background(MtzColors.Mint), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
                }
                1 -> CircularProgressIndicator(color = MtzColors.Blue, strokeWidth = 2.5.dp, modifier = Modifier.size(20.dp))
                3 -> Box(Modifier.size(22.dp).clip(CircleShape).background(MtzColors.Sun), contentAlignment = Alignment.Center) {
                    Text("!", style = MtzType.Caption, color = Color.White)
                }
                else -> Box(Modifier.size(10.dp).clip(CircleShape).background(MtzColors.Line))
            }
        }
        Spacer(Modifier.width(12.dp))
        Text(
            label,
            style = MtzType.Label.copy(fontWeight = if (state == 1) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Medium),
            color = if (state == 0) MtzColors.Muted else MtzColors.Ink,
        )
    }
}

/** Loading umum (impor, unduh, dll.) dengan bola biru yang berdenyut. */
@Composable
fun SoftBusyOverlay(message: String) {
    val infinite = rememberInfiniteTransition(label = "busy")
    val pulse by infinite.animateFloat(0.85f, 1.1f, infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "pulse")
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0x550E1B3D))
            .pointerInput(Unit) { detectTapGestures { } },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier.glass(RoundedCornerShape(28.dp)).padding(horizontal = 32.dp, vertical = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(Modifier.size(56.dp).scale(pulse).clip(CircleShape).background(MtzColors.Orb))
            Spacer(Modifier.height(16.dp))
            Text(message, style = MtzType.Label)
        }
    }
}

/** Popup saat mengunduh tema dari Jelajah: cincin progres, persen, dan tombol batal. */
@Composable
fun DownloadOverlay(title: String, progress: Float, verifying: Boolean, onCancel: () -> Unit) {
    val infinite = rememberInfiniteTransition(label = "dl")
    val angle by infinite.animateFloat(0f, 360f, infiniteRepeatable(tween(1200, easing = LinearEasing)), label = "spin")
    val known = progress >= 0f && !verifying
    val shown by animateFloatAsState(if (known) progress else 0f, tween(300), label = "dlp")
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0x6615161B))
            .pointerInput(Unit) { detectTapGestures { } },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier
                .padding(horizontal = 32.dp)
                .fillMaxWidth()
                .glass(RoundedCornerShape(32.dp))
                .background(Color.White.copy(alpha = 0.6f))
                .padding(horizontal = 24.dp, vertical = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(Modifier.size(120.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    val stroke = 9.dp.toPx()
                    val tl = androidx.compose.ui.geometry.Offset(stroke / 2, stroke / 2)
                    val sz = androidx.compose.ui.geometry.Size(size.width - stroke, size.height - stroke)
                    drawArc(MtzColors.Line, 0f, 360f, false, topLeft = tl, size = sz, style = Stroke(stroke))
                    drawArc(
                        Brush.sweepGradient(listOf(MtzColors.BlueSoft, MtzColors.Blue, MtzColors.BlueDeep, MtzColors.BlueSoft)),
                        startAngle = if (known) -90f else angle,
                        sweepAngle = if (known) 360f * shown else 90f,
                        useCenter = false,
                        topLeft = tl,
                        size = sz,
                        style = Stroke(stroke, cap = StrokeCap.Round),
                    )
                }
                Text(
                    when {
                        verifying -> "🔍"
                        known -> "${(shown * 100).toInt()}%"
                        else -> "⬇️"
                    },
                    style = MtzType.Title,
                )
            }
            Spacer(Modifier.height(18.dp))
            Text(
                if (verifying) "Memeriksa isi tema…" else "Sedang mengunduh",
                style = MtzType.Caption.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                color = MtzColors.Blue,
            )
            Text(title, style = MtzType.Title.copy(fontSize = 20.sp, textAlign = TextAlign.Center), maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(6.dp))
            Text(
                "Dari MiuiThemez · tema otomatis masuk ke Koleksi setelah selesai.",
                style = MtzType.Caption,
                color = MtzColors.Muted,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(18.dp))
            if (!verifying) PillButton("Batalkan", onCancel, small = true, container = Color.White, content = MtzColors.Ink)
        }
    }
}

/** Popup kartu setelah tema dikirim ke aplikasi Tema dan pengguna kembali ke aplikasi. */
@Composable
fun InstalledSuccessCard(
    title: String,
    previewPath: String?,
    colorKey: String,
    onClose: () -> Unit,
    onOpenCollection: () -> Unit,
) {
    val pop by animateFloatAsState(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "pop")
    val visible = remember { androidx.compose.animation.core.MutableTransitionState(false).apply { targetState = true } }
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0x6615161B))
            .pointerInput(Unit) { detectTapGestures { onClose() } },
        contentAlignment = Alignment.Center,
    ) {
        AnimatedVisibility(visibleState = visible, enter = fadeIn(tween(200)) + scaleIn(initialScale = 0.8f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))) {
            Box(
                Modifier
                    .padding(horizontal = 28.dp)
                    .fillMaxWidth()
                    .pointerInput(Unit) { detectTapGestures { } },
            ) {
                Confetti(Modifier.matchParentSize().scale(1.25f), count = 26, seed = 3)
                Column(
                    Modifier
                        .fillMaxWidth()
                        .glass(RoundedCornerShape(34.dp))
                        .background(Color.White.copy(alpha = 0.7f))
                        .padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(Modifier.size(132.dp), contentAlignment = Alignment.Center) {
                        Box(Modifier.fillMaxSize().clip(CircleShape).background(MtzColors.Mint.copy(alpha = 0.18f)))
                        ThemePreview(
                            previewPath, title, accentFor(colorKey),
                            Modifier.size(104.dp).border(4.dp, MtzColors.Mint, CircleShape),
                            CircleShape,
                        )
                        Box(
                            Modifier.align(Alignment.BottomEnd).padding(8.dp).size(38.dp).scale(pop)
                                .clip(CircleShape).background(MtzColors.Mint).border(3.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) { Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(22.dp)) }
                    }
                    Spacer(Modifier.height(16.dp))
                    Box(Modifier.clip(CircleShape).background(MtzColors.Neon).padding(horizontal = 12.dp, vertical = 5.dp)) {
                        Text("✦ Berhasil", style = MtzType.Caption.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold), color = MtzColors.Ink)
                    }
                    Spacer(Modifier.height(10.dp))
                    Text("Tema terpasang! 🎉", style = MtzType.Title, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "\"$title\" sudah dikirim ke aplikasi Tema. Kalau tampilan HP belum berubah, buka Tema lalu ketuk Terapkan.",
                        style = MtzType.Body,
                        color = MtzColors.Muted,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(20.dp))
                    CtaButton("Mantap!", onClose, Modifier.fillMaxWidth())
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Lihat Koleksi",
                        style = MtzType.Label,
                        color = MtzColors.Blue,
                        modifier = Modifier.clip(CircleShape).clickableNoRipple(onOpenCollection).padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }
            }
        }
    }
}

private fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier =
    this.pointerInput(onClick) { detectTapGestures { onClick() } }
