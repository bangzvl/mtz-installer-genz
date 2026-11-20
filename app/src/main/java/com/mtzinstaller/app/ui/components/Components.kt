package com.mtzinstaller.app.ui.components

import android.graphics.BitmapFactory
import android.text.format.DateUtils
import android.util.LruCache
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.mtzinstaller.app.ui.theme.tileGradient
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mtzinstaller.app.ui.theme.MtzColors
import com.mtzinstaller.app.ui.theme.MtzType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.random.Random

// ---------- Gaya "sky glass": bayangan biru lembut + tepi putih tipis ----------

/**
 * Elevasi lembut kebiruan + garis tepi putih tipis (efek kaca). Pasang sebelum clip().
 * Parameter lama (offset/stroke/shadow) dipertahankan agar pemanggil lama tetap jalan;
 * [shadow] dipakai sebagai warna bayangan jika bukan Ink.
 */
fun Modifier.neo(shape: Shape, offset: Dp = 3.dp, stroke: Dp = 2.dp, shadow: Color = MtzColors.Ink): Modifier {
    val glow = if (shadow == MtzColors.Ink) MtzColors.Blue else shadow
    return this
        .shadow(elevation = offset * 4, shape = shape, ambientColor = glow.copy(alpha = 0.18f), spotColor = glow.copy(alpha = 0.28f))
        .border(1.dp, Color.White.copy(alpha = 0.75f), shape)
}

/** Kartu kaca putih transparan dengan bayangan lembut. */
fun Modifier.glass(shape: Shape = RoundedCornerShape(24.dp)): Modifier =
    this.neo(shape).clip(shape).background(MtzColors.Glass)

@Composable
fun Sticker(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MtzColors.Lime,
    content: Color = MtzColors.Ink,
    angle: Float = 0f,
    fontSize: Int = 13,
) {
    Box(
        modifier
            .rotate(angle)
            .neo(CircleShape, offset = 2.dp)
            .clip(CircleShape)
            .background(color)
            .padding(horizontal = 12.dp, vertical = 5.dp),
    ) {
        Text(text, color = content, style = MtzType.Label.copy(fontSize = fontSize.sp))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Ticker(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .glass(CircleShape)
            .padding(vertical = 9.dp),
    ) {
        Text(
            text.repeat(3),
            color = MtzColors.Blue,
            style = MtzType.Label.copy(fontSize = 13.sp, letterSpacing = 0.3.sp),
            maxLines = 1,
            modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE, velocity = 40.dp),
        )
    }
}

// ---------- Tombol & chip ----------

@Composable
fun PillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    container: Color = MtzColors.Ink,
    content: Color = Color.White,
    small: Boolean = false,
    enabled: Boolean = true,
) {
    Box(
        modifier
            .neo(CircleShape)
            .clip(CircleShape)
            .background(container)
            .alpha(if (enabled) 1f else 0.5f)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = if (small) 16.dp else 28.dp, vertical = if (small) 9.dp else 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = content, style = if (small) MtzType.Label.copy(fontSize = 13.sp) else MtzType.Label.copy(fontSize = 17.sp))
    }
}

@Composable
fun CountChip(emoji: String, count: Int, modifier: Modifier = Modifier) {
    Row(
        modifier
            .glass(CircleShape)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(emoji, fontSize = 13.sp)
        Spacer(Modifier.width(6.dp))
        Text("$count", style = MtzType.Label)
    }
}

@Composable
fun CircleIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    container: Color = Color.White,
    tint: Color = MtzColors.Ink,
    size: Dp = 40.dp,
) {
    Box(
        modifier.size(size).neo(CircleShape, offset = 2.dp).clip(CircleShape).background(container).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription, tint = tint, modifier = Modifier.size(size * 0.5f))
    }
}

@Composable
fun SectionTitle(text: String, emoji: String = "", modifier: Modifier = Modifier) {
    Text(if (emoji.isEmpty()) text else "$text $emoji", style = MtzType.Heading, modifier = modifier)
}

// ---------- Pratinjau tema ----------

private val previewCache = object : LruCache<String, ImageBitmap>(24) {}

@Composable
fun ThemePreview(
    path: String?,
    title: String,
    accent: Color,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
) {
    val bitmap by produceState(initialValue = path?.let { previewCache.get(it) }, path) {
        if (path != null && value == null) {
            value = withContext(Dispatchers.IO) {
                BitmapFactory.decodeFile(path)?.asImageBitmap()?.also { previewCache.put(path, it) }
            }
        }
    }
    Box(modifier.clip(shape).background(accent), contentAlignment = Alignment.Center) {
        val bmp = bitmap
        if (bmp != null) {
            Image(bmp, contentDescription = title, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        } else {
            Text(title.take(1).uppercase(Locale.getDefault()), style = MtzType.Title, color = MtzColors.Ink.copy(alpha = 0.7f))
        }
    }
}

// ---------- Navigasi bawah melayang ----------

enum class NavTab(val label: String, val icon: ImageVector) {
    Home("Beranda", Icons.Filled.Home),
    Browse("Jelajah", Icons.Filled.Search),
    Collection("Koleksi", Icons.AutoMirrored.Filled.List),
    Info("Info", Icons.Filled.Info),
}

@Composable
fun FloatingNavBar(
    current: NavTab,
    onSelect: (NavTab) -> Unit,
    modifier: Modifier = Modifier,
    onCenter: (() -> Unit)? = null,
) {
    Row(
        modifier
            .padding(horizontal = 18.dp)
            .fillMaxWidth()
            .glass(RoundedCornerShape(30.dp))
            .padding(horizontal = 6.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NavTab.entries.forEachIndexed { index, tab ->
            if (index == 2 && onCenter != null) GlowOrb(onCenter)
            val active = tab == current
            val tint by animateColorAsState(if (active) MtzColors.Ink else MtzColors.Muted.copy(alpha = 0.8f), label = "navTint")
            Column(
                Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onSelect(tab) }
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(tab.icon, tab.label, tint = tint, modifier = Modifier.size(22.dp))
                Text(tab.label, color = tint, style = MtzType.Caption.copy(fontSize = 11.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Medium))
            }
        }
    }
}

/** Tombol bulat bercahaya di tengah navigasi (aksi cepat: impor .mtz). */
@Composable
fun GlowOrb(onClick: () -> Unit, size: Dp = 54.dp) {
    val pulse = rememberInfiniteTransition(label = "orb")
    val glow by pulse.animateFloat(0.25f, 0.55f, infiniteRepeatable(tween(1600), RepeatMode.Reverse), label = "glow")
    Box(
        Modifier
            .size(size)
            .shadow(18.dp, CircleShape, ambientColor = MtzColors.Blue.copy(alpha = glow), spotColor = MtzColors.Blue.copy(alpha = glow))
            .clip(CircleShape)
            .background(MtzColors.Orb)
            .border(2.dp, Color.White.copy(alpha = 0.8f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Filled.Add, "Impor .mtz", tint = Color.White, modifier = Modifier.size(26.dp))
    }
}

/** Tombol utama gaya desain: pil navy dengan lingkaran putih berisi panah. */
@Composable
fun CtaButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Row(
        modifier
            .neo(CircleShape)
            .clip(CircleShape)
            .background(MtzColors.Ink)
            .alpha(if (enabled) 1f else 0.55f)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(44.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = MtzColors.Ink, modifier = Modifier.size(20.dp))
        }
        Text(
            text,
            color = Color.White,
            style = MtzType.Label.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.weight(1f).padding(horizontal = 10.dp),
        )
    }
}

/** Ubin ikon warna-warni + label (baris kategori di Beranda). */
@Composable
fun QuickAction(icon: String, label: String, color: Color, onClick: () -> Unit) {
    Column(
        Modifier.width(66.dp).clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick).padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .size(52.dp)
                .neo(RoundedCornerShape(16.dp), offset = 2.dp, shadow = color)
                .clip(RoundedCornerShape(16.dp))
                .background(tileGradient(color)),
            contentAlignment = Alignment.Center,
        ) { Text(icon, fontSize = 22.sp) }
        Spacer(Modifier.height(6.dp))
        Text(label, style = MtzType.Caption, color = MtzColors.Ink, maxLines = 1)
    }
}

// ---------- Hiasan confetti ----------

private class Piece(val x: Float, val y: Float, val angle: Float, val color: Color, val w: Float, val h: Float)

@Composable
fun Confetti(modifier: Modifier = Modifier, count: Int = 28, seed: Int = 7) {
    val pieces = remember(count, seed) {
        val r = Random(seed)
        List(count) {
            Piece(
                x = r.nextFloat(), y = r.nextFloat(), angle = r.nextFloat() * 360f,
                color = (MtzColors.accents + MtzColors.Ink)[r.nextInt(MtzColors.accents.size + 1)],
                w = 4f + r.nextFloat() * 3f, h = 12f + r.nextFloat() * 10f,
            )
        }
    }
    Canvas(modifier) {
        pieces.forEach { p ->
            val cx = p.x * size.width
            val cy = p.y * size.height
            val w = p.w.dp.toPx()
            val h = p.h.dp.toPx()
            rotate(p.angle, pivot = Offset(cx, cy)) {
                drawRoundRect(p.color, topLeft = Offset(cx - w / 2, cy - h / 2), size = Size(w, h), cornerRadius = CornerRadius(w / 2))
            }
        }
    }
}

// ---------- Format ----------

fun formatSize(bytes: Long): String = when {
    bytes >= 1024L * 1024 -> String.format(Locale.getDefault(), "%.1f MB", bytes / 1024f / 1024f)
    bytes >= 1024 -> "${bytes / 1024} KB"
    else -> "$bytes B"
}

fun relativeTime(ts: Long): String =
    DateUtils.getRelativeTimeSpanString(ts, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS).toString()
