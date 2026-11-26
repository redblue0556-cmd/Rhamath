package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ProgressRepository
import com.example.data.Question
import com.example.data.QuestionBank
import com.example.data.QuizMode
import com.example.data.UserProgress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AppScreen {
    SPLASH,
    LOBBY,
    QUIZ,
    LAB
}

data class QuizUiState(
    val chapterSelection: Int = 0, // -1 means random all chapters in generator mode
    val mode: QuizMode = QuizMode.BANK,
    val questionIndex: Int = 0,
    val question: Question = QuestionBank.getBankQuestion(0, 0),
    val selectedOptionIndex: Int? = null,
    val xpGainedOnThisQuestion: Int? = null
)

data class LabUiState(
    val radiusInput: String = "14",
    val angleInput: String = "90",
    val usePiFraction: Boolean = true
) {
    val radius: Double get() = (radiusInput.replace(',', '.').toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
    val angleDeg: Double get() = (angleInput.replace(',', '.').toDoubleOrNull() ?: 0.0).coerceIn(0.0, 360.0)
    val piValue: Double get() = if (usePiFraction) 22.0 / 7.0 else 3.14
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ProgressRepository(application.applicationContext)

    val progress: StateFlow<UserProgress> = repository.progressFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserProgress()
    )

    private val _currentScreen = MutableStateFlow(AppScreen.SPLASH)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _quizState = MutableStateFlow(QuizUiState())
    val quizState: StateFlow<QuizUiState> = _quizState.asStateFlow()

    private val _labState = MutableStateFlow(LabUiState())
    val labState: StateFlow<LabUiState> = _labState.asStateFlow()

    private val _generatorChapterFilter = MutableStateFlow(-1)
    val generatorChapterFilter: StateFlow<Int> = _generatorChapterFilter.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun finishSplash() {
        _currentScreen.value = AppScreen.LOBBY
    }

    fun goToLobby() {
        _currentScreen.value = AppScreen.LOBBY
    }

    fun openLab() {
        _currentScreen.value = AppScreen.LAB
    }

    fun setGeneratorChapterFilter(chapter: Int) {
        _generatorChapterFilter.value = chapter
    }

    fun openBankChapter(chapterIndex: Int) {
        val completed = progress.value.completedKeys
        // Start at first incomplete question in chapter, or 0 if all completed
        val firstUnfinished = (0 until QuestionBank.QUESTIONS_PER_CHAPTER).firstOrNull { idx ->
            !completed.contains("$chapterIndex-$idx")
        } ?: 0

        _quizState.value = QuizUiState(
            chapterSelection = chapterIndex,
            mode = QuizMode.BANK,
            questionIndex = firstUnfinished,
            question = QuestionBank.getBankQuestion(chapterIndex, firstUnfinished),
            selectedOptionIndex = null,
            xpGainedOnThisQuestion = null
        )
        _currentScreen.value = AppScreen.QUIZ
    }

    fun openGeneratorQuiz(chapterFilter: Int = _generatorChapterFilter.value) {
        val q = QuestionBank.generateRandomQuestion(chapterFilter)
        _quizState.value = QuizUiState(
            chapterSelection = chapterFilter,
            mode = QuizMode.GENERATOR,
            questionIndex = 0,
            question = q,
            selectedOptionIndex = null,
            xpGainedOnThisQuestion = null
        )
        _currentScreen.value = AppScreen.QUIZ
    }

    fun jumpToBankQuestion(index: Int) {
        val current = _quizState.value
        if (current.mode != QuizMode.BANK) return
        val clamped = index.coerceIn(0, QuestionBank.QUESTIONS_PER_CHAPTER - 1)
        _quizState.value = current.copy(
            questionIndex = clamped,
            question = QuestionBank.getBankQuestion(current.chapterSelection, clamped),
            selectedOptionIndex = null,
            xpGainedOnThisQuestion = null
        )
    }

    fun selectAnswer(optionIndex: Int) {
        val current = _quizState.value
        if (current.selectedOptionIndex != null) return

        val isCorrect = optionIndex == current.question.correctIndex
        val bankKey = if (current.mode == QuizMode.BANK) {
            "${current.question.chapterIndex}-${current.questionIndex}"
        } else {
            null
        }

        if (!isCorrect) {
            _quizState.update {
                it.copy(
                    selectedOptionIndex = optionIndex,
                    xpGainedOnThisQuestion = 0
                )
            }
            return
        }

        // Optimistically show answer immediately, then sync DataStore
        val alreadyClaimed = bankKey != null && progress.value.completedKeys.contains(bankKey)
        _quizState.update {
            it.copy(
                selectedOptionIndex = optionIndex,
                xpGainedOnThisQuestion = if (alreadyClaimed) 0 else 100
            )
        }

        viewModelScope.launch {
            val awarded = repository.recordCorrectAnswer(bankKey)
            _quizState.update { state ->
                if (state.question == current.question) {
                    state.copy(xpGainedOnThisQuestion = if (awarded) 100 else 0)
                } else state
            }
        }
    }

    fun navigateQuiz(delta: Int) {
        val current = _quizState.value
        if (current.mode == QuizMode.GENERATOR) {
            val q = QuestionBank.generateRandomQuestion(current.chapterSelection)
            _quizState.value = current.copy(
                question = q,
                selectedOptionIndex = null,
                xpGainedOnThisQuestion = null
            )
            return
        }

        val nextIdx = current.questionIndex + delta
        if (nextIdx < 0) return
        if (nextIdx >= QuestionBank.QUESTIONS_PER_CHAPTER) {
            _toastMessage.value = "🎉 Bab selesai! GANBARU!"
            _currentScreen.value = AppScreen.LOBBY
            return
        }
        _quizState.value = current.copy(
            questionIndex = nextIdx,
            question = QuestionBank.getBankQuestion(current.chapterSelection, nextIdx),
            selectedOptionIndex = null,
            xpGainedOnThisQuestion = null
        )
    }

    fun updateLabRadius(value: String) {
        _labState.update { it.copy(radiusInput = value) }
    }

    fun updateLabAngle(value: String) {
        val num = value.replace(',', '.').toDoubleOrNull()
        val sanitized = if (num != null && num > 360.0) "360" else value
        _labState.update { it.copy(angleInput = sanitized) }
    }

    fun setLabPiFraction(useFraction: Boolean) {
        _labState.update { it.copy(usePiFraction = useFraction) }
    }

    fun resetProgress() {
        viewModelScope.launch {
            repository.resetProgress()
            _toastMessage.value = "Progres berhasil direset."
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }
}
