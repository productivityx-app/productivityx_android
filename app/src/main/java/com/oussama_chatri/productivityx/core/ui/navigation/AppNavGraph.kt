package com.oussama_chatri.productivityx.core.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavGraph(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AuthGraph,
        modifier = modifier
    ) {
        authNavGraph(navController)
        mainNavGraph(navController)
    }
}