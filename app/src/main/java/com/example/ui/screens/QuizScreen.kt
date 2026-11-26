package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.Question
import com.example.data.QuestionBank
import com.example.data.QuizMode
import com.example.data.UserProgress
import com.example.ui.QuizUiState
import com.example.ui.components.MangaButton
import com.example.ui.components.MangaClickableCard
import com.example.ui.components.MangaPanel
import com.example.ui.components.MangaSectionBox
import com.example.ui.components.MangaSpeechBubble
import com.example.ui.components.MathRichText
import com.example.ui.components.SolutionNotebookBox
import com.example.ui.components.mangaHalftoneBackground
import com.example.ui.theme.LocalMangaPalette

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun QuizScreen(
    quizState: QuizUiState,
    progress: UserProgress,
    onSelectAnswer: (Int) -> Unit,
    onNavigate: (Int) -> Unit,
    onJumpToQuestion: (Int) -> Unit,
    onBackToLobby: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBackToLobby)

    val palette = LocalMangaPalette.current
    var showPanelPicker by remember { mutableStateOf(false) }

    if (showPanelPicker && quizState.mode == QuizMode.BANK) {
        PanelPickerDialog(
            chapterIndex = quizState.chapterSelection,
            currentIndex = quizState.questionIndex,
            completedKeys = progress.completedKeys,
            onSelectIndex = { idx ->
                showPanelPicker = false
                onJumpToQuestion(idx)
            },
            onDismiss = { showPanelPicker = false }
        )
    }

    val scrollState = rememberScrollState()
    val feedbackBringIntoView = remember { BringIntoViewRequester() }
    LaunchedEffect(quizState.question, quizState.mode, quizState.questionIndex) {
        scrollState.scrollTo(0)
    }
    LaunchedEffect(quizState.selectedOptionIndex) {
        if (quizState.selectedOptionIndex != null) {
            kotlinx.coroutines.delay(140)
            feedbackBringIntoView.bringIntoView()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .mangaHalftoneBackground(palette.paper, palette.halftoneDot)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Quiz Header
            QuizTopHeader(
                quizState = quizState,
                isQuestionDone = progress.completedKeys.contains("${quizState.question.chapterIndex}-${quizState.questionIndex}"),
                onBackToLobby = onBackToLobby,
                onOpenPanelPicker = { showPanelPicker = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 1100.dp)
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )

            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                val isTwoPane = maxWidth >= 840.dp

                if (isTwoPane) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 1100.dp)
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            QuestionAndOptionsColumn(
                                quizState = quizState,
                                onSelectAnswer = onSelectAnswer
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        Column(
                            modifier = Modifier
                                .weight(1.1f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (quizState.selectedOptionIndex != null) {
                                FeedbackAndSolutionPanel(
                                    question = quizState.question,
                                    selectedIndex = quizState.selectedOptionIndex,
                                    xpGained = quizState.xpGainedOnThisQuestion ?: 0
                                )
                            } else {
                                MangaPanel(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = "📖 PANEL PEMBAHASAN",
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = palette.ink
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Pilih salah satu jawaban (A–E) di panel kiri untuk melihat kunci jawaban, langkah penyelesaian ke bawah, dan tips anti-jebakan!",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = palette.muted
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 760.dp)
                            .verticalScroll(scrollState)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        QuestionAndOptionsColumn(
                            quizState = quizState,
                            onSelectAnswer = onSelectAnswer
                        )

                        AnimatedVisibility(
                            visible = quizState.selectedOptionIndex != null,
                            modifier = Modifier.bringIntoViewRequester(feedbackBringIntoView),
                            enter = fadeIn() + scaleIn(
                                initialScale = 0.96f,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                            )
                        ) {
                            val selected = quizState.selectedOptionIndex
                            if (selected != null) {
                                FeedbackAndSolutionPanel(
                                    question = quizState.question,
                                    selectedIndex = selected,
                                    xpGained = quizState.xpGainedOnThisQuestion ?: 0
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            // Bottom Navigation Controls
            QuizBottomBar(
                quizState = quizState,
                onNavigate = onNavigate,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 1100.dp)
                    .align(Alignment.CenterHorizontally)
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            )
        }
    }
}

@Composable
private fun QuizTopHeader(
    quizState: QuizUiState,
    isQuestionDone: Boolean,
    onBackToLobby: () -> Unit,
    onOpenPanelPicker: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalMangaPalette.current
    val ch = QuestionBank.chapters[quizState.question.chapterIndex]
    val headerSub = (if (quizState.mode == QuizMode.GENERATOR) "⚡ GENERATOR · " else "") +
        "BAB ${quizState.question.chapterIndex + 1}: ${ch.title}"
    val panelTitle = if (quizState.mode == QuizMode.GENERATOR) {
        "SOAL BARU"
    } else {
        "PANEL ${quizState.questionIndex + 1} / ${QuestionBank.QUESTIONS_PER_CHAPTER}" +
            if (isQuestionDone) " ✔" else ""
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MangaButton(
            onClick = onBackToLobby,
            contentPadding = PaddingValues(12.dp),
            modifier = Modifier.testTag("quiz_home_button")
        ) {
            Icon(
                imageVector = Icons.Filled.Home,
                contentDescription = stringResource(R.string.back_to_lobby),
                tint = palette.ink
            )
        }

        MangaPanel(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = headerSub,
                style = MaterialTheme.typography.labelSmall,
                color = palette.muted,
                maxLines = 1
            )
            Text(
                text = panelTitle,
                style = MaterialTheme.typography.titleLarge,
                color = palette.ink
            )
        }

        if (quizState.mode == QuizMode.BANK) {
            MangaButton(
                onClick = onOpenPanelPicker,
                backgroundColor = palette.ink,
                contentColor = palette.onInk,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                modifier = Modifier.testTag("panel_picker_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.GridView,
                        contentDescription = "Pilih Nomor Soal",
                        tint = palette.onInk,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "1–100",
                        style = MaterialTheme.typography.labelLarge,
                        color = palette.onInk
                    )
                }
            }
        }
    }
}

@Composable
private fun QuestionAndOptionsColumn(
    quizState: QuizUiState,
    onSelectAnswer: (Int) -> Unit
) {
    val palette = LocalMangaPalette.current
    val q = quizState.question
    val selected = quizState.selectedOptionIndex
    val letters = "ABCDE"

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        MangaPanel(modifier = Modifier.fillMaxWidth()) {
            MathRichText(
                text = q.prompt,
                color = palette.ink,
                fontSize = 16.sp,
                lineHeight = 25.sp
            )
        }

        q.options.forEachIndexed { idx, opt ->
            val isCorrectOpt = idx == q.correctIndex
            val isSelectedWrong = selected == idx && !isCorrectOpt
            val bgColor = when {
                selected == null -> palette.panelSurface
                isCorrectOpt -> palette.green
                isSelectedWrong -> palette.crimson
                else -> palette.panelSurface
            }
            val fgColor = when {
                selected != null && (isCorrectOpt || isSelectedWrong) -> Color.White
                else -> palette.ink
            }

            MangaClickableCard(
                onClick = { onSelectAnswer(idx) },
                enabled = selected == null,
                backgroundColor = bgColor,
                contentColor = fgColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("option_$idx"),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "${letters[idx]}.",
                        style = MaterialTheme.typography.titleLarge,
                        color = fgColor
                    )
                    Box(modifier = Modifier.weight(1f)) {
                        MathRichText(
                            text = opt,
                            color = fgColor,
                            fontSize = 15.sp,
                            lineHeight = 22.sp
                        )
                    }
                    if (selected != null && isCorrectOpt) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Benar",
                            tint = Color.White
                        )
                    } else if (isSelectedWrong) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Salah",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeedbackAndSolutionPanel(
    question: Question,
    selectedIndex: Int,
    xpGained: Int
) {
    val palette = LocalMangaPalette.current
    val letters = "ABCDE"
    val isCorrect = selectedIndex == question.correctIndex
    val breakdown = remember(question) { QuestionBank.buildSolutionBreakdown(question) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (isCorrect) {
            val xpText = if (xpGained > 0) "(+$xpGained XP)" else "(sudah diklaim)"
            Text(
                text = "BAM! BENAR! $xpText",
                style = MaterialTheme.typography.displayMedium,
                color = palette.green,
                modifier = Modifier.testTag("feedback_banner")
            )
            MangaSpeechBubble {
                Text(
                    text = "SUKSES! Tetap baca panel pembahasan agar paham alurnya ya!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.ink
                )
            }
        } else {
            Text(
                text = "BELUM TEPAT!",
                style = MaterialTheme.typography.displayMedium,
                color = palette.crimson,
                modifier = Modifier.testTag("feedback_banner")
            )
            MangaSpeechBubble {
                Text(
                    text = "Belum Tepat! Mari kita bedah panel penyelesaiannya: jawaban benar adalah ${letters[question.correctIndex]}.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.ink
                )
            }
            MangaSectionBox(
                title = "🔎 KENAPA JAWABANMU KELIRU?",
                headerColor = palette.crimson,
                headerTextColor = palette.onCrimson,
                borderColor = palette.crimson
            ) {
                MathRichText(
                    text = "Kamu memilih ${letters[selectedIndex]} (${question.options[selectedIndex]}). Nilainya berbeda dengan hasil perhitungan. Penyebab paling umum: salah urutan operasi, salah tanda (+/−), atau tertukar rumus. Cocokkan pekerjaanmu dengan Langkah 1–${question.steps.size} di bawah, lalu coba soal berikutnya!",
                    color = palette.ink,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }

        MangaPanel(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "📖 PEMBAHASAN LENGKAP",
                style = MaterialTheme.typography.displayMedium,
                color = palette.ink
            )
            Spacer(modifier = Modifier.height(10.dp))

            // 1. DIK & DIT
            MangaSectionBox(title = "🎯 1. DIK & DIT") {
                MathRichText(
                    text = "Dit : ${question.questionAsked}",
                    color = palette.ink,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Dik :",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = palette.ink
                )
                Spacer(modifier = Modifier.height(2.dp))
                for (d in question.given) {
                    MathRichText(
                        text = "• $d",
                        color = palette.ink,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 6.dp, top = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. KONSEP & RUMUS UTAMA
            MangaSectionBox(title = "📘 2. KONSEP & RUMUS UTAMA") {
                MathRichText(
                    text = question.concept,
                    color = palette.ink,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(palette.yellowHighlight)
                        .border(2.dp, palette.ink)
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    MathRichText(
                        text = "$$${question.formula}$$",
                        color = palette.ink,
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. PENYELESAIAN
            MangaSectionBox(title = "🛠️ 3. PENYELESAIAN") {
                SolutionNotebookBox(breakdown = breakdown)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // JAWABAN BOX
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(palette.green)
                    .border(2.5.dp, palette.ink)
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "✅ JAWABAN: ${letters[question.correctIndex]}.",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = palette.onGreen
                    )
                    MathRichText(
                        text = question.options[question.correctIndex],
                        color = palette.onGreen,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // KESIMPULAN
            MangaSectionBox(title = "💬 PENJELASAN KESIMPULAN") {
                MathRichText(
                    text = "Dari langkah-langkah di atas, hasil akhirnya adalah ${question.options[question.correctIndex]}. Jadi jawaban yang benar adalah pilihan ${letters[question.correctIndex]}.",
                    color = palette.ink,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                MathRichText(
                    text = "Intinya: ${question.concept}",
                    color = palette.ink,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4. CEK ULANG
            MangaSectionBox(title = "🔁 4. CEK ULANG") {
                MathRichText(
                    text = "Masukkan jawaban ${letters[question.correctIndex]} (${question.options[question.correctIndex]}) kembali ke soal. Hasilnya harus cocok dengan data yang diketahui. Jika belum cocok, periksa lagi tanda dan urutan operasi.",
                    color = palette.ink,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // TIPS
            MangaSpeechBubble {
                MathRichText(
                    text = "💡 Tips Manga / Jebakan: ${question.tip}",
                    color = palette.ink,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun QuizBottomBar(
    quizState: QuizUiState,
    onNavigate: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalMangaPalette.current
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (quizState.mode == QuizMode.BANK) {
            val hasPrev = quizState.questionIndex > 0
            MangaButton(
                onClick = { onNavigate(-1) },
                enabled = hasPrev,
                backgroundColor = if (hasPrev) palette.panelSurface else palette.panelSecondary,
                contentColor = if (hasPrev) palette.ink else palette.muted,
                modifier = Modifier
                    .weight(1f)
                    .testTag("prev_question_button")
            ) {
                Text(
                    text = stringResource(R.string.prev_question),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
            }
        }

        MangaButton(
            onClick = { onNavigate(1) },
            backgroundColor = palette.crimson,
            contentColor = palette.onCrimson,
            modifier = Modifier
                .weight(1f)
                .testTag("next_question_button")
        ) {
            val nextLabel = if (quizState.mode == QuizMode.GENERATOR) {
                "⚡ ${stringResource(R.string.new_question_next)}"
            } else if (quizState.questionIndex == QuestionBank.QUESTIONS_PER_CHAPTER - 1) {
                "Selesai ✔"
            } else {
                stringResource(R.string.next_question)
            }
            Text(
                text = nextLabel,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
private fun PanelPickerDialog(
    chapterIndex: Int,
    currentIndex: Int,
    completedKeys: Set<String>,
    onSelectIndex: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LocalMangaPalette.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Pilih Panel (Bab ${chapterIndex + 1})",
                style = MaterialTheme.typography.headlineMedium
            )
        },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 52.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(QuestionBank.QUESTIONS_PER_CHAPTER) { idx ->
                    val done = completedKeys.contains("$chapterIndex-$idx")
                    val isCurrent = idx == currentIndex
                    val bg = when {
                        isCurrent -> palette.crimson
                        done -> palette.green
                        else -> palette.panelSurface
                    }
                    val fg = if (isCurrent || done) Color.White else palette.ink
                    Box(
                        modifier = Modifier
                            .height(48.dp)
                            .background(bg)
                            .border(2.dp, palette.ink)
                            .clickable { onSelectIndex(idx) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (done && !isCurrent) "${idx + 1}✓" else "${idx + 1}",
                            style = MaterialTheme.typography.labelLarge,
                            color = fg
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup", fontWeight = FontWeight.Bold)
            }
        }
    )
}
