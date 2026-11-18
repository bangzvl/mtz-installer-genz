package com.mtzinstaller.app.ui.theme

import android.graphics.Typeface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object MtzColors {
    val Cream = Color(0xFFF4EFE9)
    val Ink = Color(0xFF111111)
    val Coral = Color(0xFFEE5B3D)
    val Sun = Color(0xFFF7C548)
    val Mint = Color(0xFF8FCB8B)
    val Sky = Color(0xFF7FC6DC)
    val Lilac = Color(0xFF9D86E9)
    val Peach = Color(0xFFF4A64A)
    val Muted = Color(0xFF8B857E)
    val Line = Color(0xFFEDE7E0)
    val Lime = Color(0xFFC6F432)
    val Pink = Color(0xFFFF5CA8)
    val accents = listOf(Coral, Sun, Lime, Sky, Lilac, Pink, Peach, Mint)
}

/** Font condensed tebal, mendekati gaya judul "Quiz" pada desain. */
private val Condensed = FontFamily(Typeface.create("sans-serif-condensed", Typeface.NORMAL))

object MtzType {
    val Display = TextStyle(fontFamily = Condensed, fontWeight = FontWeight.Black, fontSize = 56.sp, lineHeight = 58.sp, letterSpacing = (-1).sp)
    val Title = TextStyle(fontFamily = Condensed, fontWeight = FontWeight.Black, fontSize = 28.sp, lineHeight = 30.sp)
    val Heading = TextStyle(fontFamily = Condensed, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 26.sp)
    val Label = TextStyle(fontFamily = Condensed, fontWeight = FontWeight.Bold, fontSize = 15.sp, lineHeight = 18.sp)
    val Body = TextStyle(fontSize = 13.sp, lineHeight = 19.sp)
    val Caption = TextStyle(fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight.Medium)
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
