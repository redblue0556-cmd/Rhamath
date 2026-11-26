package com.example.data

import androidx.compose.runtime.Immutable

@Immutable
data class ChapterInfo(
    val index: Int,
    val title: String,
    val description: String,
    val emoji: String
)

@Immutable
data class Question(
    val chapterIndex: Int,
    val prompt: String,
    val options: List<String>,
    val correctIndex: Int,
    val given: List<String>,
    val formula: String,
    val steps: List<String>,
    val tip: String,
    val concept: String
) {
    val questionAsked: String
        get() {
            val parts = prompt.split(Regex("(?<=[.?])\\s+(?=[A-Z])"))
            return (parts.lastOrNull() ?: prompt).replace(Regex("\\.{3,}"), "…")
        }
}

@Immutable
data class RankInfo(
    val grade: String,
    val title: String,
    val levelIndex: Int
)

@Immutable
data class UserProgress(
    val xp: Int = 0,
    val stars: Int = 0,
    val completedKeys: Set<String> = emptySet()
) {
    val levelIndex: Int get() = (xp / 500).coerceAtMost(5)
    val rank: RankInfo get() = RANKS[levelIndex]
    val xpInLevel: Int get() = if (levelIndex >= 5) 500 else xp % 500
    val xpToNext: Int get() = if (levelIndex >= 5) 0 else 500 - (xp % 500)

    fun completedCountForChapter(chapterIndex: Int): Int {
        val prefix = "$chapterIndex-"
        return completedKeys.count { it.startsWith(prefix) }
    }

    companion object {
        val RANKS = listOf(
            RankInfo("E", "Pemula Aljabar", 0),
            RankInfo("D", "Murid Fungsi", 1),
            RankInfo("C", "Ahli Komposisi", 2),
            RankInfo("B", "Pendekar Invers", 3),
            RankInfo("A", "Master Lingkaran", 4),
            RankInfo("S", "Legenda Manga", 5)
        )
    }
}

enum class QuizMode {
    BANK,
    GENERATOR
}

@Immutable
data class SolutionChain(
    val left: String,
    val rows: List<String>,
    val isRaw: Boolean = false
)

@Immutable
data class SolutionBreakdown(
    val chains: List<SolutionChain>,
    val explanations: List<String>
)
