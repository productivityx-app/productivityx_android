package com.oussama_chatri.productivityx.core.ui.navigation

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.oussama_chatri.productivityx.core.ui.components.PxBottomNavBar
import com.oussama_chatri.productivityx.features.home.presentation.HomeScreen

@Composable
private fun HomeTab(rootNavController: NavHostController) {
    val rootBackStack by rootNavController.currentBackStackEntryAsState()
    val currentRoute  = rootBackStack?.destination?.route

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            PxBottomNavBar(
                currentRoute   = currentRoute,
                onNavItemClick = { rootNavController.navigateToTab(it) },
                modifier       = Modifier.navigationBarsPadding(),
            )
        },
    ) { innerPadding ->
        HomeScreen(
            onNavigateToProfile   = { rootNavController.navigate(MainRoute.Profile) },
            onNavigateToNotes     = { rootNavController.navigateToTab(MainRoute.Notes) },
            onNavigateToTasks     = { rootNavController.navigateToTab(MainRoute.Tasks) },
            onNavigateToCalendar  = { rootNavController.navigateToTab(MainRoute.Calendar) },
            onNavigateToPomodoro  = { rootNavController.navigateToTab(MainRoute.Pomodoro) },
            onNavigateToAi        = { rootNavController.safeNavigate(MainRoute.Ai) },
            modifier              = Modifier.padding(innerPadding),
        )
    }
}
