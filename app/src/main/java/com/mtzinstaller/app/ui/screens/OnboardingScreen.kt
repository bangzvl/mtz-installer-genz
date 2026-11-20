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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.mtzinstaller.app.ui.components.CtaButton
import com.mtzinstaller.app.ui.components.Sticker
import com.mtzinstaller.app.ui.components.neo
import com.mtzinstaller.app.ui.theme.MtzColors
import com.mtzinstaller.app.ui.theme.MtzType

@Composable
fun OnboardingScreen(onStart: () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF4F9BFF), Color(0xFF8CC2FF), Color(0xFFDDEBFF)))),
    ) {
        // Awan lembut
        Cloud(Modifier.offset(x = (-40).dp, y = 120.dp), 180.dp)
        Cloud(Modifier.align(Alignment.TopEnd).offset(x = 50.dp, y = 60.dp), 160.dp)

        Column(Modifier.fillMaxSize().statusBarsPadding(), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(18.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🎨", fontSize = 16.sp)
                Spacer(Modifier.width(6.dp))
                Text("mtz installer", color = Color.White, style = MtzType.Label)
            }
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                // Tulisan raksasa samar di belakang (seperti "HAPPN" pada desain)
                Text(
                    "MTZ",
                    color = Color.White.copy(alpha = 0.35f),
                    fontSize = 150.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-4).sp,
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 6.dp),
                )
                PhoneMock(Color(0xFF9B8CFF), 104.dp, Modifier.offset(x = (-92).dp, y = 40.dp).rotate(-12f))
                PhoneMock(Color(0xFF4FD1A5), 104.dp, Modifier.offset(x = 92.dp, y = 40.dp).rotate(12f))
                PhoneMock(Color(0xFFFF8A6B), 128.dp, Modifier.offset(y = 20.dp).zIndex(1f))
                Column(
                    Modifier.align(Alignment.BottomStart).padding(start = 20.dp, bottom = 24.dp).zIndex(2f).rotate(-6f),
                ) {
                    Text("GLOW", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Black, lineHeight = 38.sp)
                    Text("UP ✨", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Black, lineHeight = 38.sp)
                }
                Sticker(".mtz", Modifier.zIndex(2f).offset(x = 86.dp, y = 120.dp), color = Color.White, content = MtzColors.Blue, fontSize = 15)
            }

            // Panel putih bawah
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp))
                    .background(Color.White.copy(alpha = 0.92f))
                    .navigationBarsPadding()
                    .padding(horizontal = 28.dp, vertical = 26.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("MTZ Installer", style = MtzType.Title)
                    Spacer(Modifier.width(8.dp))
                    Sticker("gen Z ✌️", color = MtzColors.Blue, content = Color.White, fontSize = 13)
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    "Tema impianmu, cuma satu ketukan",
                    style = MtzType.Heading,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Cari tema, lihat pratinjaunya, lalu pasang ke HP Xiaomi kamu dengan mudah.",
                    style = MtzType.Body.copy(fontSize = 14.sp, lineHeight = 21.sp),
                    color = MtzColors.Muted,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(22.dp))
                CtaButton("Mulai sekarang", onStart, Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun Cloud(modifier: Modifier, width: Dp) {
    Box(modifier.size(width, width * 0.45f)) {
        Box(Modifier.size(width * 0.55f).align(Alignment.BottomStart).clip(CircleShape).background(Color.White.copy(alpha = 0.35f)))
        Box(Modifier.size(width * 0.6f).align(Alignment.TopCenter).clip(CircleShape).background(Color.White.copy(alpha = 0.35f)))
        Box(Modifier.size(width * 0.5f).align(Alignment.BottomEnd).clip(CircleShape).background(Color.White.copy(alpha = 0.35f)))
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
            Text("Jum, 20 Nov", style = MtzType.Caption.copy(fontSize = 8.sp), color = Color.White.copy(alpha = 0.85f))
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
