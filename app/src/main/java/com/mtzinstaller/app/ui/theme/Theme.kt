package com.mtzinstaller.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object MtzColors {
    // Gaya "sky glass": latar biru langit lembut, kartu putih kaca, aksen biru cerah
    val Cream = Color(0xFFEFF5FF)      // permukaan terang (sheet, dialog)
    val Ink = Color(0xFF0E1B3D)        // teks utama & tombol utama (navy pekat)
    val Blue = Color(0xFF2E7CF6)       // aksen utama
    val BlueDeep = Color(0xFF0B3D91)
    val BlueSoft = Color(0xFF9CC8FF)
    val Coral = Color(0xFFFF8A6B)
    val Sun = Color(0xFFFFC94D)
    val Mint = Color(0xFF4FD1A5)
    val Sky = Color(0xFF5AA9FF)
    val Lilac = Color(0xFF9B8CFF)
    val Peach = Color(0xFFFFB36B)
    val Muted = Color(0xFF6B7A99)
    val Line = Color(0xFFE3ECFA)
    val Lime = Color(0xFFDCEBFF)       // tombol sekunder: biru muda + teks navy
    val Pink = Color(0xFFFF7EB3)
    val Glass = Color(0xD9FFFFFF)      // putih 85% untuk kartu kaca
    val accents = listOf(Sky, Lilac, Coral, Mint, Pink, Sun, Peach, Blue)

    val Background = Brush.verticalGradient(listOf(Color(0xFFC9E0FF), Color(0xFFE6F0FF), Color(0xFFF6F9FF)))
    val Orb = Brush.radialGradient(listOf(Color(0xFF9CD2FF), Color(0xFF3D8BFF), Color(0xFF0B3D91)))
}

/** Gradien ikon kategori (gaya ubin warna-warni di desain). */
fun tileGradient(c: Color): Brush = Brush.linearGradient(listOf(c.copy(alpha = 0.75f), c))

object MtzType {
    val Display = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.ExtraBold, fontSize = 40.sp, lineHeight = 44.sp, letterSpacing = (-0.8).sp)
    val Title = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, lineHeight = 28.sp, letterSpacing = (-0.3).sp)
    val Heading = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 19.sp, lineHeight = 24.sp)
    val Label = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 18.sp)
    val Body = TextStyle(fontSize = 13.sp, lineHeight = 19.sp)
    val Caption = TextStyle(fontSize = 11.5.sp, lineHeight = 15.sp, fontWeight = FontWeight.Medium)
}

fun accentFor(key: String): Color =
    MtzColors.accents[Math.floorMod(key.hashCode(), MtzColors.accents.size)]

@Composable
fun MtzAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = MtzColors.Ink,
            onPrimary = Color.White,
            secondary = MtzColors.Coral,
            background = MtzColors.Cream,
            onBackground = MtzColors.Ink,
            surface = MtzColors.Cream,
            onSurface = MtzColors.Ink,
        ),
        content = content,
    )
}
