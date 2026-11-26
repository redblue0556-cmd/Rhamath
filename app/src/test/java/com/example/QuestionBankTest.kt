package com.example

import com.example.data.QuestionBank
import com.example.ui.components.LatexParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QuestionBankTest {
    @Test
    fun allChaptersGenerate100ValidUniqueQuestions() {
        for (ch in QuestionBank.chapters.indices) {
            val questions = QuestionBank.getChapterQuestions(ch)
            assertEquals(100, questions.size)

            val uniquePrompts = questions.map { it.prompt }.toSet()
            assertEquals("Chapter $ch should have 100 unique prompts", 100, uniquePrompts.size)

            for ((idx, q) in questions.withIndex()) {
                assertEquals("Question chapter index mismatch at $ch-$idx", ch, q.chapterIndex)
                assertEquals("Options count should be 5 at $ch-$idx", 5, q.options.size)
                assertEquals("Options should be unique at $ch-$idx", 5, q.options.toSet().size)
                assertTrue("correctIndex out of bounds at $ch-$idx", q.correctIndex in 0..4)
                assertTrue("Steps should not be empty at $ch-$idx", q.steps.isNotEmpty())

                // Verify solution breakdown and LaTeX parsing
                val breakdown = QuestionBank.buildSolutionBreakdown(q)
                assertTrue("Solution chains should not be empty at $ch-$idx", breakdown.chains.isNotEmpty())

                LatexParser.parse(q.formula)
                for (chain in breakdown.chains) {
                    LatexParser.parse(chain.left)
                    for (row in chain.rows) {
                        LatexParser.parse(row)
                    }
                }
            }
        }
    }

    @Test
    fun generatorProducesValidQuestionsAcrossAllFilters() {
        for (filter in -1..4) {
            val q = QuestionBank.generateRandomQuestion(filter, seed = 12345 + filter)
            assertTrue(q.chapterIndex in 0..4)
            assertEquals(5, q.options.size)
            assertTrue(q.correctIndex in 0..4)
        }
    }
}
