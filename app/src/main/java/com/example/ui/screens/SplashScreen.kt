package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.MangaButton
import com.example.ui.components.MangaPanel
import com.example.ui.components.MangaProgressBar
import com.example.ui.components.mangaHalftoneBackground
import com.example.ui.components.mangaSpeedLines
import com.example.ui.theme.LocalMangaPalette
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onStartApp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalMangaPalette.current
    var progressPercent by remember { mutableIntStateOf(0) }

    val statusMessages = remember {
        listOf(
            "Memuat Chapter Soal...",
            "Mengasah Logika Aljabar...",
            "Menggambar Panel Lingkaran...",
            "Siap! GANBARU!"
        )
    }

    LaunchedEffect(Unit) {
        while (progressPercent < 100) {
            delay(45)
            progressPercent = (progressPercent + 4).coerceAtMost(100)
        }
    }

    val messageIndex = (progressPercent / 34).coerceIn(0, statusMessages.lastIndex)

    Box(
        modifier = modifier
            .fillMaxSize()
            .mangaHalftoneBackground(palette.paper, palette.halftoneDot)
            .mangaSpeedLines(palette.ink.copy(alpha = 0.13f))
            .pointerInput(Unit) {
                detectTapGestures(onTap = { progressPercent = 100 })
            }
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        MangaPanel(
            modifier = Modifier.widthIn(max = 440.dp),
            shadowOffset = 6.dp
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = stringResource(R.string.chapter_tag),
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.muted
                )
                Text(
                    text = "🥷📐⭕",
                    fontSize = 52.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "RHAJESH MATCH",
                    style = MaterialTheme.typography.displayLarge,
                    color = palette.ink,
                    textAlign = TextAlign.Center
                )
                Box(
                    modifier = Modifier
                        .background(palette.ink)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.app_subtitle),
                        style = MaterialTheme.typography.titleLarge,
                        color = palette.onInk,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                MangaProgressBar(
                    progress = progressPercent / 100f,
                    height = 22.dp
                )

                Text(
                    text = statusMessages[messageIndex],
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = palette.ink,
                    modifier = Modifier.padding(top = 4.dp)
                )

                if (progressPercent < 100) {
                    Text(
                        text = stringResource(R.string.tap_to_skip),
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.muted
                    )
                }

                AnimatedVisibility(
                    visible = progressPercent >= 100,
                    enter = fadeIn(tween(220)) + scaleIn(initialScale = 0.9f, animationSpec = tween(220))
                ) {
                    MangaButton(
                        onClick = onStartApp,
                        backgroundColor = palette.crimson,
                        contentColor = palette.onCrimson,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .testTag("start_learning_button")
                    ) {
                        Text(
                            text = stringResource(R.string.start_learning),
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
