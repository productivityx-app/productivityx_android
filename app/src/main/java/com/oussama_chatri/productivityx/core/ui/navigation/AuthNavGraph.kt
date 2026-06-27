package com.oussama_chatri.productivityx.core.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import com.oussama_chatri.productivityx.features.auth.presentation.screen.ForgotPasswordScreen
import com.oussama_chatri.productivityx.features.auth.presentation.screen.LoginScreen
import com.oussama_chatri.productivityx.features.auth.presentation.screen.OnboardingScreen
import com.oussama_chatri.productivityx.features.auth.presentation.screen.RegisterScreen
import com.oussama_chatri.productivityx.features.auth.presentation.screen.ResetPasswordScreen
import com.oussama_chatri.productivityx.features.auth.presentation.screen.SplashScreen
import com.oussama_chatri.productivityx.features.auth.presentation.screen.VerifyEmailScreen

fun NavGraphBuilder.authNavGraph(navController: NavController) {
    navigation<AuthGraph>(startDestination = AuthRoute.Splash) {

        composable<AuthRoute.Splash> {
            SplashScreen(
                onNavigateToOnboarding = {
                    navController.navigateAndClearBackStack(AuthRoute.Onboarding)
                },
                onNavigateToLogin = {
                    navController.navigateAndClearBackStack(AuthRoute.Login)
                },
                onNavigateToHome = {
                    navController.navigateAndClearBackStack(MainGraph)
                }
            )
        }

        composable<AuthRoute.Onboarding> {
            OnboardingScreen(
                onGetStarted = {
                    navController.navigateAndClearBackStack(AuthRoute.Login)
                }
            )
        }

        composable<AuthRoute.Login> {
            LoginScreen(
                onNavigateToRegister = {
                    navController.safeNavigate(AuthRoute.Register)
                },
                onNavigateToForgotPassword = {
                    navController.safeNavigate(AuthRoute.ForgotPassword)
                },
                onNavigateToVerifyEmail = { email ->
                    navController.safeNavigate(AuthRoute.VerifyEmail(email))
                },
                onLoginSuccess = {
                    navController.navigateAndClearBackStack(MainGraph)
                }
            )
        }

        composable<AuthRoute.Register> {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    navController.navigateAndClearBackStack(MainGraph)
                }
            )
        }

        composable<AuthRoute.VerifyEmail> { backStackEntry ->
            val route = backStackEntry.toRoute<AuthRoute.VerifyEmail>()
            VerifyEmailScreen(
                email = route.email,
                onVerifySuccess = {
                    navController.navigateAndClearBackStack(MainGraph)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<AuthRoute.ForgotPassword> {
            ForgotPasswordScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToResetPassword = { token ->
                    navController.safeNavigate(AuthRoute.ResetPassword(token))
                }
            )
        }

        composable<AuthRoute.ResetPassword> { backStackEntry ->
            val route = backStackEntry.toRoute<AuthRoute.ResetPassword>()
            ResetPasswordScreen(
                token = route.token,
                onResetSuccess = {
                    navController.navigateAndClearBackStack(AuthRoute.Login)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}