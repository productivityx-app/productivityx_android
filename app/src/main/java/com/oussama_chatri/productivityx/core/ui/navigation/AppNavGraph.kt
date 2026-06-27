package com.oussama_chatri.productivityx.core.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavGraph(
    onNavControllerReady: (NavHostController) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    onNavControllerReady(navController)

    NavHost(
        navController = navController,
        startDestination = AuthGraph,
        modifier = modifier
    ) {
        authNavGraph(navController)
        mainNavGraph(navController)
    }
}