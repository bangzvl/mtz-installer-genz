package com.mtzinstaller.app.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.mtzinstaller.app.R
import com.mtzinstaller.app.ui.components.CtaButton
import com.mtzinstaller.app.ui.components.neo
import com.mtzinstaller.app.ui.theme.MtzColors
import com.mtzinstaller.app.ui.theme.MtzType
import kotlinx.coroutines.launch

private data class IntroPage(val tag: String, val title: String, val body: String, val top: Color, val bottom: Color)

private val PAGES = listOf(
    IntroPage(
        "Selamat datang",
        "MTZ Installer gen Z",
        "Aplikasi untuk mengumpulkan, melihat pratinjau, dan memasang tema .mtz di HP Xiaomi, Redmi, dan POCO.",
        Color(0xFF6E4BFF), Color(0xFFB9A8FF),
    ),
    IntroPage(
        "Jelajahi & unduh",
        "Ribuan tema, sekali ketuk",
        "Cari tema di MiuiThemez langsung dari aplikasi. Tekan Download dan tema otomatis masuk ke Koleksi kamu.",
        Color(0xFF3B23B8), Color(0xFF8C7BFF),
    ),
    IntroPage(
        "Pasang tanpa ribet",
        "Glow up HP kamu ✨",
        "Pratinjau tema, ketuk Pasang, dan ikuti langkah singkatnya. Ada panduan otomatis kalau aplikasi Tema butuh setup sekali.",
        Color(0xFF15161B), Color(0xFF4A3B9E),
    ),
)

@Composable
fun OnboardingScreen(onStart: () -> Unit) {
    val pager = rememberPagerState { PAGES.size }
    val scope = rememberCoroutineScope()
    val page = PAGES[pager.currentPage]
    val top by animateColorAsState(page.top, tween(400), label = "top")
    val bottom by animateColorAsState(page.bottom, tween(400), label = "bottom")

    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(top, bottom, Color(0xFFF1F0FF))))) {
        Cloud(Modifier.offset(x = (-40).dp, y = 120.dp), 180.dp)
        Cloud(Modifier.align(Alignment.TopEnd).offset(x = 50.dp, y = 70.dp), 150.dp)

        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(painterResource(R.drawable.app_logo), null, Modifier.size(30.dp).clip(RoundedCornerShape(9.dp)))
                Spacer(Modifier.width(8.dp))
                Text("mtz installer", color = Color.White, style = MtzType.Label)
                Spacer(Modifier.weight(1f))
                if (pager.currentPage < PAGES.lastIndex) {
                    Text(
                        "Lewati",
                        color = Color.White,
                        style = MtzType.Label,
                        modifier = Modifier.clip(CircleShape).clickable(onClick = onStart).padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }
            }

            HorizontalPager(pager, Modifier.fillMaxWidth().weight(1f)) { index ->
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    when (index) {
                        0 -> IllustrationWelcome()
                        1 -> IllustrationBrowse()
                        else -> IllustrationInstall()
                    }
                }
            }

            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp))
                    .background(Color.White.copy(alpha = 0.95f))
                    .navigationBarsPadding()
                    .padding(horizontal = 28.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(Modifier.clip(CircleShape).background(MtzColors.Lime).padding(horizontal = 12.dp, vertical = 5.dp)) {
                    Text("${pager.currentPage + 1}/3 · ${page.tag}", style = MtzType.Caption.copy(fontWeight = FontWeight.Bold), color = MtzColors.Blue)
                }
                Spacer(Modifier.height(12.dp))
                Text(page.title, style = MtzType.Title, textAlign = TextAlign.Center)
                Spacer(Modifier.height(8.dp))
                Text(
                    page.body,
                    style = MtzType.Body.copy(fontSize = 14.sp, lineHeight = 21.sp),
                    color = MtzColors.Muted,
                    textAlign = TextAlign.Center,
                    minLines = 3,
                )
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    PAGES.indices.forEach { i ->
                        val w by animateDpAsState(if (i == pager.currentPage) 26.dp else 8.dp, label = "dot")
                        Box(
                            Modifier.height(8.dp).width(w).clip(CircleShape)
                                .background(if (i == pager.currentPage) MtzColors.Blue else MtzColors.Line),
                        )
                    }
                }
                Spacer(Modifier.height(18.dp))
                val last = pager.currentPage == PAGES.lastIndex
                CtaButton(
                    if (last) "Masuk ke aplikasi" else "Lanjut",
                    { if (last) onStart() else scope.launch { pager.animateScrollToPage(pager.currentPage + 1) } },
                    Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun IllustrationWelcome() {
    val float = rememberInfiniteTransition(label = "float")
    val y by float.animateFloat(-6f, 6f, infiniteRepeatable(tween(1800), RepeatMode.Reverse), label = "y")
    Box(contentAlignment = Alignment.Center) {
        Text("MTZ", color = Color.White.copy(alpha = 0.18f), fontSize = 150.sp, fontWeight = FontWeight.Black, letterSpacing = (-4).sp)
        Image(
            painterResource(R.drawable.app_logo),
            contentDescription = "Logo MTZ Installer gen Z",
            modifier = Modifier
                .offset(y = y.dp)
                .size(190.dp)
                .neo(RoundedCornerShape(48.dp), offset = 6.dp, shadow = MtzColors.Blue)
                .clip(RoundedCornerShape(48.dp)),
        )
        Chip("gen Z ✌️", MtzColors.Neon, MtzColors.Ink, Modifier.offset(x = 90.dp, y = (-100).dp).rotate(8f))
        Chip(".mtz", Color.White, MtzColors.Blue, Modifier.offset(x = (-100).dp, y = 96.dp).rotate(-8f))
    }
}

@Composable
private fun IllustrationBrowse() {
    Box(contentAlignment = Alignment.Center) {
        PhoneMock(Color(0xFF9B8CFF), 104.dp, Modifier.offset(x = (-86).dp, y = 20.dp).rotate(-12f))
        PhoneMock(Color(0xFF4FD1A5), 104.dp, Modifier.offset(x = 86.dp, y = 20.dp).rotate(12f))
        PhoneMock(Color(0xFFFF8A6B), 128.dp, Modifier.zIndex(1f))
        Row(
            Modifier
                .zIndex(2f)
                .offset(y = 150.dp)
                .neo(CircleShape)
                .clip(CircleShape)
                .background(Color.White)
                .padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("⬇️", fontSize = 14.sp)
            Spacer(Modifier.width(6.dp))
            Text("Mengunduh tema 72%", style = MtzType.Label.copy(fontSize = 13.sp), color = MtzColors.Ink)
        }
    }
}

@Composable
private fun IllustrationInstall() {
    val pulse = rememberInfiniteTransition(label = "pulse")
    val s by pulse.animateFloat(0.95f, 1.05f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "s")
    Box(contentAlignment = Alignment.Center) {
        Box(Modifier.size(210.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.12f)))
        Box(
            Modifier.size(160.dp).scale(s).clip(CircleShape)
                .background(Brush.linearGradient(listOf(Color(0xFFFF8A6B), Color(0xFF9B8CFF))))
                .border(5.dp, MtzColors.Neon, CircleShape),
            contentAlignment = Alignment.Center,
        ) { Text("10:24", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold) }
        Box(
            Modifier.offset(x = 62.dp, y = 62.dp).size(52.dp).clip(CircleShape).background(MtzColors.Mint)
                .border(4.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center,
        ) { Text("✓", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black) }
        Chip("Terpasang 🎉", Color.White, MtzColors.Ink, Modifier.offset(x = (-70).dp, y = (-110).dp).rotate(-6f))
    }
}

@Composable
private fun Chip(text: String, bg: Color, fg: Color, modifier: Modifier) {
    Box(modifier.neo(CircleShape).clip(CircleShape).background(bg).padding(horizontal = 14.dp, vertical = 7.dp)) {
        Text(text, color = fg, style = MtzType.Label)
    }
}

@Composable
private fun Cloud(modifier: Modifier, width: Dp) {
    Box(modifier.size(width, width * 0.45f)) {
        Box(Modifier.size(width * 0.55f).align(Alignment.BottomStart).clip(CircleShape).background(Color.White.copy(alpha = 0.18f)))
        Box(Modifier.size(width * 0.6f).align(Alignment.TopCenter).clip(CircleShape).background(Color.White.copy(alpha = 0.18f)))
        Box(Modifier.size(width * 0.5f).align(Alignment.BottomEnd).clip(CircleShape).background(Color.White.copy(alpha = 0.18f)))
    }
}

@Composable
private fun PhoneMock(color: Color, width: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier
            .width(width)
            .height(width * 1.95f)
            .neo(RoundedCornerShape(24.dp), shadow = color)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .padding(4.dp),
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(20.dp))
                .background(Brush.verticalGradient(listOf(color.copy(alpha = 0.7f), color)))
                .padding(10.dp),
        ) {
            Box(Modifier.align(Alignment.CenterHorizontally).width(28.dp).height(6.dp).clip(RoundedCornerShape(3.dp)).background(Color.White.copy(alpha = 0.7f)))
            Spacer(Modifier.height(10.dp))
            Text("09:41", style = MtzType.Title.copy(fontSize = 22.sp), color = Color.White)
            Spacer(Modifier.weight(1f))
            repeat(2) {
                Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    repeat(3) {
                        Box(Modifier.size(width / 5).clip(RoundedCornerShape(7.dp)).background(Color.White.copy(alpha = 0.6f)))
                    }
                }
            }
        }
    }
}
