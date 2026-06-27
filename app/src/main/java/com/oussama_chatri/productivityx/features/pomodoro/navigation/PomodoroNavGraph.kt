package com.oussama_chatri.productivityx.features.pomodoro.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.oussama_chatri.productivityx.features.pomodoro.presentation.screen.PomodoroScreen
import com.oussama_chatri.productivityx.features.pomodoro.presentation.screen.SessionHistoryScreen
import kotlinx.serialization.Serializable

sealed class PomodoroRoute {
    @Serializable data object Timer   : PomodoroRoute()
    @Serializable data object History : PomodoroRoute()
}

fun NavGraphBuilder.pomodoroNavGraph(navController: NavHostController) {

    composable<PomodoroRoute.Timer> {
        PomodoroScreen()
    }

    composable<PomodoroRoute.History> {
        SessionHistoryScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }
}
