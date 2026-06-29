package com.oussama_chatri.productivityx.core.ui.navigation

import androidx.navigation.NavController

fun NavController.safeNavigate(route: Any) {
    try {
        navigate(route)
    } catch (_: Exception) {
    }
}

fun NavController.navigateAndClearBackStack(route: Any) {
    try {
        navigate(route) {
            popUpTo(0) { inclusive = true }
            launchSingleTop = true
        }
    } catch (_: Exception) {
    }
}
