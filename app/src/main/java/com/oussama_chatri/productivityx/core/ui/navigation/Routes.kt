package com.oussama_chatri.productivityx.core.ui.navigation

import kotlinx.serialization.Serializable

// Top-level navigation graph containers
@Serializable object AuthGraph
@Serializable object MainGraph

// Auth routes
sealed class AuthRoute {
    @Serializable data object Splash : AuthRoute()
    @Serializable data object Onboarding : AuthRoute()
    @Serializable data object Login : AuthRoute()
    @Serializable data object Register : AuthRoute()
    @Serializable data class VerifyEmail(val email: String = "") : AuthRoute()
    @Serializable data object ForgotPassword : AuthRoute()
    @Serializable data class ResetPassword(val token: String = "") : AuthRoute()
}

// Main (post-auth) routes
sealed class MainRoute {
    @Serializable data object Home : MainRoute()
    @Serializable data object Notes : MainRoute()
    @Serializable data object Tasks : MainRoute()
    @Serializable data object Calendar : MainRoute()
    @Serializable data object Pomodoro : MainRoute()
    @Serializable data object Ai : MainRoute()
    @Serializable data object Search : MainRoute()
    @Serializable data object Profile : MainRoute()
}