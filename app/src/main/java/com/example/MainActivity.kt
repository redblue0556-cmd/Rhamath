package com.example

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.screens.LabScreen
import com.example.ui.screens.LobbyScreen
import com.example.ui.screens.QuizScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.RhajeshMatchTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        setContent {
            val viewModel: MainViewModel = viewModel()
            val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

            LaunchedEffect(isDarkMode) {
                if (isDarkMode) {
                    enableEdgeToEdge(
                        statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
                        navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
                    )
                } else {
                    enableEdgeToEdge(
                        statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
                        navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                    )
                }
            }

            RhajeshMatchTheme(darkTheme = isDarkMode) {
                RhajeshMatchApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun RhajeshMatchApp(
    viewModel: MainViewModel = viewModel()
) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val progress by viewModel.progress.collectAsStateWithLifecycle()
    val quizState by viewModel.quizState.collectAsStateWithLifecycle()
    val labState by viewModel.labState.collectAsStateWithLifecycle()
    val generatorFilter by viewModel.generatorChapterFilter.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(toastMessage) {
        val msg = toastMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToast()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Crossfade(
            targetState = currentScreen,
            animationSpec = tween(220),
            label = "screenCrossfade"
        ) { screen ->
            when (screen) {
                AppScreen.SPLASH -> SplashScreen(
                    onStartApp = viewModel::finishSplash
                )
                AppScreen.LOBBY -> LobbyScreen(
                    progress = progress,
                    generatorFilter = generatorFilter,
                    isDarkMode = isDarkMode,
                    onToggleDarkMode = viewModel::toggleDarkMode,
                    onGeneratorFilterChange = viewModel::setGeneratorChapterFilter,
                    onOpenChapter = viewModel::openBankChapter,
                    onOpenGenerator = { viewModel.openGeneratorQuiz(generatorFilter) },
                    onOpenLab = viewModel::openLab,
                    onResetProgress = viewModel::resetProgress
                )
                AppScreen.QUIZ -> QuizScreen(
                    quizState = quizState,
                    progress = progress,
                    onSelectAnswer = viewModel::selectAnswer,
                    onNavigate = viewModel::navigateQuiz,
                    onJumpToQuestion = viewModel::jumpToBankQuestion,
                    onBackToLobby = viewModel::goToLobby
                )
                AppScreen.LAB -> LabScreen(
                    labState = labState,
                    onRadiusChange = viewModel::updateLabRadius,
                    onAngleChange = viewModel::updateLabAngle,
                    onPiToggle = viewModel::setLabPiFraction,
                    onBackToLobby = viewModel::goToLobby
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(16.dp)
        )
    }
}
