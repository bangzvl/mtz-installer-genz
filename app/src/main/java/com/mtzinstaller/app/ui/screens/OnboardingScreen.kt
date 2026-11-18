package com.mtzinstaller.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.mtzinstaller.app.ui.components.Confetti
import com.mtzinstaller.app.ui.components.PillButton
import com.mtzinstaller.app.ui.components.Sticker
import com.mtzinstaller.app.ui.theme.MtzColors
import com.mtzinstaller.app.ui.theme.MtzType

@Composable
fun OnboardingScreen(onStart: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(MtzColors.Cream)
            .systemBarsPadding()
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(0.6f))
        Row(verticalAlignment = Alignment.Bottom) {
            Text("MTZ", style = MtzType.Display.copy(fontSize = 76.sp, lineHeight = 76.sp))
            Text("🎨", fontSize = 46.sp, modifier = Modifier.padding(start = 8.dp, bottom = 10.dp))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("INSTALLER", style = MtzType.Title.copy(fontSize = 26.sp, letterSpacing = 2.sp))
            Spacer(Modifier.width(10.dp))
            Sticker("gen Z ✌️", fontSize = 18)
        }
        Spacer(Modifier.height(20.dp))
        HeroCollage(Modifier.fillMaxWidth().height(270.dp))
        Spacer(Modifier.height(28.dp))
        Text("Glow up HP kamu, sesukamu 💅", style = MtzType.Heading, textAlign = TextAlign.Center)
        Spacer(Modifier.height(10.dp))
        Text(
            "Impor file .mtz dari penyimpanan, lihat pratinjaunya, lalu terapkan ke ponsel Xiaomi kamu hanya dengan satu ketukan.",
            style = MtzType.Body.copy(fontSize = 14.sp, lineHeight = 22.sp),
            color = MtzColors.Muted,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.weight(1f))
        PillButton("Gas, mulai →", onStart, Modifier.width(220.dp), container = MtzColors.Lime, content = MtzColors.Ink)
        Spacer(Modifier.height(36.dp))
    }
}

@Composable
private fun HeroCollage(modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Confetti(Modifier.fillMaxSize(), count = 30)
        PhoneMock(MtzColors.Mint, 104.dp, Modifier.offset(x = (-86).dp, y = 14.dp).rotate(-12f))
        PhoneMock(MtzColors.Sun, 104.dp, Modifier.offset(x = 86.dp, y = 14.dp).rotate(12f))
        PhoneMock(MtzColors.Coral, 124.dp, Modifier.zIndex(1f))
        Sticker(".mtz", Modifier.zIndex(2f).offset(x = 76.dp, y = 92.dp), color = MtzColors.Pink, content = Color.White, angle = 6f, fontSize = 17)
        Sticker("no ribet ✨", Modifier.zIndex(2f).offset(x = (-90).dp, y = (-112).dp))
    }
}

@Composable
private fun PhoneMock(color: Color, width: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier
            .width(width)
            .height(width * 1.9f)
            .shadow(14.dp, RoundedCornerShape(22.dp))
            .clip(RoundedCornerShape(22.dp))
            .background(MtzColors.Ink)
            .padding(4.dp),
    ) {
        Column(
            Modifier.fillMaxSize().clip(RoundedCornerShape(18.dp)).background(color).padding(10.dp),
        ) {
            Box(Modifier.align(Alignment.CenterHorizontally).width(30.dp).height(6.dp).clip(RoundedCornerShape(3.dp)).background(MtzColors.Ink))
            Spacer(Modifier.height(10.dp))
            Text("09:41", style = MtzType.Title.copy(fontSize = 22.sp), color = MtzColors.Ink)
            Text("Rab, 18 Nov", style = MtzType.Caption.copy(fontSize = 8.sp), color = MtzColors.Ink.copy(alpha = 0.7f))
            Spacer(Modifier.weight(1f))
            repeat(2) {
                Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    repeat(3) {
                        Box(Modifier.size(width / 5).clip(RoundedCornerShape(6.dp)).background(Color.White.copy(alpha = 0.55f)))
                    }
                }
            }
        }
    }
}
