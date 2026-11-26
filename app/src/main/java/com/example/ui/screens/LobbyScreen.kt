package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.ChapterInfo
import com.example.data.QuestionBank
import com.example.data.UserProgress
import com.example.ui.components.MangaButton
import com.example.ui.components.MangaClickableCard
import com.example.ui.components.MangaPanel
import com.example.ui.components.MangaProgressBar
import com.example.ui.components.mangaHalftoneBackground
import com.example.ui.theme.LocalMangaPalette

@Composable
fun LobbyScreen(
    progress: UserProgress,
    generatorFilter: Int,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    onGeneratorFilterChange: (Int) -> Unit,
    onOpenChapter: (Int) -> Unit,
    onOpenGenerator: () -> Unit,
    onOpenLab: () -> Unit,
    onResetProgress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalMangaPalette.current
    var showResetDialog by remember { mutableStateOf(false) }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(stringResource(R.string.reset_confirm_title)) },
            text = { Text(stringResource(R.string.reset_confirm_msg)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetDialog = false
                        onResetProgress()
                    }
                ) {
                    Text(stringResource(R.string.confirm), color = palette.crimson, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .mangaHalftoneBackground(palette.paper, palette.halftoneDot)
            .safeDrawingPadding()
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 12.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            val isWide = maxWidth >= 600.dp

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 920.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Hero Status Row: Rank + Stars
                if (isWide) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        RankCard(progress = progress, modifier = Modifier.weight(2f))
                        StarsCard(stars = progress.stars, modifier = Modifier.weight(1f))
                    }
                } else {
                    RankCard(progress = progress, modifier = Modifier.fillMaxWidth())
                    StarsCard(stars = progress.stars, modifier = Modifier.fillMaxWidth())
                }

                // Chapter Selector Panel
                MangaPanel(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.choose_chapter),
                        style = MaterialTheme.typography.displayMedium,
                        color = palette.ink
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val chapters = QuestionBank.chapters
                    if (isWide) {
                        val rows = chapters.chunked(2)
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            for (rowItems in rows) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    for (ch in rowItems) {
                                        ChapterCard(
                                            chapter = ch,
                                            completedCount = progress.completedCountForChapter(ch.index),
                                            onClick = { onOpenChapter(ch.index) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    if (rowItems.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            for (ch in chapters) {
                                ChapterCard(
                                    chapter = ch,
                                    completedCount = progress.completedCountForChapter(ch.index),
                                    onClick = { onOpenChapter(ch.index) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                // Smart Procedural Generator Panel
                GeneratorPanel(
                    selectedFilter = generatorFilter,
                    onSelectFilter = onGeneratorFilterChange,
                    onGenerate = onOpenGenerator,
                    isWide = isWide
                )

                // Lab, Theme Toggle & Reset Row
                if (isWide) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        LabActionButton(onClick = onOpenLab, modifier = Modifier.weight(2f))
                        ThemeToggleButton(
                            isDarkMode = isDarkMode,
                            onClick = onToggleDarkMode,
                            modifier = Modifier.weight(1f)
                        )
                        ResetActionButton(onClick = { showResetDialog = true }, modifier = Modifier.weight(1f))
                    }
                } else {
                    LabActionButton(onClick = onOpenLab, modifier = Modifier.fillMaxWidth())
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ThemeToggleButton(
                            isDarkMode = isDarkMode,
                            onClick = onToggleDarkMode,
                            modifier = Modifier.weight(1f)
                        )
                        ResetActionButton(
                            onClick = { showResetDialog = true },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.footer_motto),
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.muted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun RankCard(
    progress: UserProgress,
    modifier: Modifier = Modifier
) {
    val palette = LocalMangaPalette.current
    MangaPanel(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .clip(CircleShape)
                    .background(palette.panelSecondary)
                    .border(3.dp, palette.ink, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🥷", fontSize = 32.sp)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Peringkat ${progress.rank.grade}",
                    style = MaterialTheme.typography.titleLarge,
                    color = palette.ink
                )
                Text(
                    text = progress.rank.title,
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.muted
                )
                Spacer(modifier = Modifier.height(6.dp))
                MangaProgressBar(
                    progress = progress.xpInLevel / 500f,
                    height = 16.dp
                )
                Spacer(modifier = Modifier.height(4.dp))
                val xpLabel = if (progress.levelIndex < 5) {
                    "${progress.xp} XP · ${progress.xpToNext} XP ke peringkat berikut"
                } else {
                    "${progress.xp} XP · MAKS"
                }
                Text(
                    text = xpLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.ink
                )
            }
        }
    }
}

@Composable
private fun StarsCard(
    stars: Int,
    modifier: Modifier = Modifier
) {
    val palette = LocalMangaPalette.current
    MangaPanel(
        modifier = modifier,
        backgroundColor = palette.ink,
        contentColor = palette.onInk
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "⭐ $stars",
                style = MaterialTheme.typography.displayMedium,
                color = palette.onInk
            )
            Text(
                text = stringResource(R.string.stars_collected),
                style = MaterialTheme.typography.labelSmall,
                color = palette.onInk.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
private fun ChapterCard(
    chapter: ChapterInfo,
    completedCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalMangaPalette.current
    MangaClickableCard(
        onClick = onClick,
        modifier = modifier.testTag("chapter_card_${chapter.index}"),
        contentPadding = PaddingValues(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${chapter.emoji} BAB ${chapter.index + 1}",
                style = MaterialTheme.typography.titleLarge,
                color = palette.ink
            )
            if (completedCount == QuestionBank.QUESTIONS_PER_CHAPTER) {
                Box(
                    modifier = Modifier
                        .background(palette.green)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "SELESAI ✔",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.onGreen
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = chapter.title,
            style = MaterialTheme.typography.titleMedium,
            color = palette.ink
        )
        Text(
            text = chapter.description,
            style = MaterialTheme.typography.bodyMedium,
            color = palette.muted
        )
        Spacer(modifier = Modifier.height(8.dp))
        MangaProgressBar(
            progress = completedCount / QuestionBank.QUESTIONS_PER_CHAPTER.toFloat(),
            height = 10.dp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "$completedCount/${QuestionBank.QUESTIONS_PER_CHAPTER} selesai",
            style = MaterialTheme.typography.labelSmall,
            color = palette.ink
        )
    }
}

@Composable
private fun GeneratorPanel(
    selectedFilter: Int,
    onSelectFilter: (Int) -> Unit,
    onGenerate: () -> Unit,
    isWide: Boolean
) {
    val palette = LocalMangaPalette.current
    var dropdownExpanded by remember { mutableStateOf(false) }

    val filterLabel = if (selectedFilter < 0) {
        "🎲 Acak Semua Bab"
    } else {
        "Bab ${selectedFilter + 1}: ${QuestionBank.chapters[selectedFilter].title}"
    }

    MangaPanel(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = palette.crimson
            )
            Text(
                text = stringResource(R.string.generator_title),
                style = MaterialTheme.typography.headlineMedium,
                color = palette.ink
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.generator_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = palette.muted
        )
        Spacer(modifier = Modifier.height(10.dp))

        val selectorBox: @Composable (Modifier) -> Unit = { mod ->
            Box(modifier = mod) {
                MangaButton(
                    onClick = { dropdownExpanded = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("generator_chapter_dropdown")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = filterLabel,
                            style = MaterialTheme.typography.labelLarge,
                            color = palette.ink,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = "Pilih Bab",
                            tint = palette.ink
                        )
                    }
                }
                DropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("🎲 Acak Semua Bab") },
                        onClick = {
                            onSelectFilter(-1)
                            dropdownExpanded = false
                        }
                    )
                    QuestionBank.chapters.forEach { ch ->
                        DropdownMenuItem(
                            text = { Text("${ch.emoji} Bab ${ch.index + 1}: ${ch.title}") },
                            onClick = {
                                onSelectFilter(ch.index)
                                dropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }

        val generateBtn: @Composable (Modifier) -> Unit = { mod ->
            MangaButton(
                onClick = onGenerate,
                backgroundColor = palette.crimson,
                contentColor = palette.onCrimson,
                modifier = mod.testTag("generator_start_button")
            ) {
                Text(
                    text = "⚡ ${stringResource(R.string.create_new_question)}",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }

        if (isWide) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                selectorBox(Modifier.weight(1.2f))
                generateBtn(Modifier.weight(1f))
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                selectorBox(Modifier.fillMaxWidth())
                generateBtn(Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun LabActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalMangaPalette.current
    MangaButton(
        onClick = onClick,
        backgroundColor = palette.ink,
        contentColor = palette.onInk,
        modifier = modifier.testTag("open_lab_button")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Science,
                contentDescription = null,
                tint = palette.onInk
            )
            Text(
                text = "🧪 ${stringResource(R.string.lab_button)}",
                style = MaterialTheme.typography.titleMedium,
                color = palette.onInk
            )
        }
    }
}

@Composable
private fun ThemeToggleButton(
    isDarkMode: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalMangaPalette.current
    MangaButton(
        onClick = onClick,
        backgroundColor = palette.panelSurface,
        contentColor = palette.ink,
        modifier = modifier.testTag("theme_toggle_button")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = if (isDarkMode) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                contentDescription = null,
                tint = palette.ink
            )
            Text(
                text = if (isDarkMode) "Kertas Manga" else "Mode Gelap",
                style = MaterialTheme.typography.titleMedium,
                color = palette.ink
            )
        }
    }
}

@Composable
private fun ResetActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalMangaPalette.current
    MangaButton(
        onClick = onClick,
        backgroundColor = palette.panelSurface,
        contentColor = palette.ink,
        modifier = modifier.testTag("reset_progress_button")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.RestartAlt,
                contentDescription = null,
                tint = palette.crimson
            )
            Text(
                text = stringResource(R.string.reset_progress),
                style = MaterialTheme.typography.titleMedium,
                color = palette.ink
            )
        }
    }
}
