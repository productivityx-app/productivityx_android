package com.oussama_chatri.productivityx.core.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import com.oussama_chatri.productivityx.core.ui.components.PxBottomNavBar

fun NavGraphBuilder.mainNavGraph(rootNavController: NavHostController) {
    navigation<MainGraph>(startDestination = MainRoute.Home) {
        composable<MainRoute.Home> { MainShell(rootNavController, "Home") }
        composable<MainRoute.Notes> { MainShell(rootNavController, "Notes") }
        composable<MainRoute.Tasks> { MainShell(rootNavController, "Tasks") }
        composable<MainRoute.Calendar> { MainShell(rootNavController, "Calendar") }
        composable<MainRoute.Pomodoro> { MainShell(rootNavController, "Pomodoro") }
        composable<MainRoute.Ai> { MainShell(rootNavController, "AI") }
        composable<MainRoute.Search> { MainShell(rootNavController, "Search") }
        composable<MainRoute.Profile> { MainShell(rootNavController, "Profile") }
    }
}

// Temporary scaffold shell — replace each composable when those features are built
@Composable
private fun MainShell(navController: NavHostController, title: String) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination

    Scaffold(
        bottomBar = {
            PxBottomNavBar(
                currentRoute = currentRoute?.route,
                onNavItemClick = { route ->
                    navController.navigate(route) {
                        popUpTo(MainRoute.Home) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                modifier = Modifier.navigationBarsPadding()
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}