package com.oussama_chatri.productivityx.core.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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

        composable<AuthRoute.Splash>(
            enterTransition = {
                scaleIn(tween(400, delayMillis = 200), initialScale = 2f) + fadeIn(tween(400))
            },
            exitTransition = {
                scaleOut(tween(200), targetScale = 0.5f) + fadeOut(tween(200))
            },
        ) {
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

        composable<AuthRoute.Onboarding>(
            enterTransition = {
                slideInHorizontally(tween(300)) + fadeIn(tween(300))
            },
            exitTransition = {
                slideOutHorizontally(tween(300)) { -it / 3 } + fadeOut(tween(300))
            },
            popEnterTransition = {
                slideInHorizontally(tween(300)) { -it / 3 } + fadeIn(tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(tween(300)) + fadeOut(tween(300))
            },
        ) {
            OnboardingScreen(
                onGetStarted = {
                    navController.navigateAndClearBackStack(AuthRoute.Login)
                }
            )
        }

        composable<AuthRoute.Login>(
            enterTransition = {
                slideInHorizontally(tween(300)) + fadeIn(tween(300))
            },
            exitTransition = {
                slideOutHorizontally(tween(250)) { -it / 3 } + fadeOut(tween(250))
            },
            popEnterTransition = {
                slideInHorizontally(tween(250)) { -it / 3 } + fadeIn(tween(250))
            },
            popExitTransition = {
                slideOutHorizontally(tween(300)) + fadeOut(tween(300))
            },
        ) {
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

        composable<AuthRoute.Register>(
            enterTransition = {
                slideInHorizontally(tween(300)) + fadeIn(tween(300))
            },
            exitTransition = {
                slideOutHorizontally(tween(250)) { -it / 3 } + fadeOut(tween(250))
            },
            popEnterTransition = {
                slideInHorizontally(tween(250)) { -it / 3 } + fadeIn(tween(250))
            },
            popExitTransition = {
                slideOutHorizontally(tween(300)) + fadeOut(tween(300))
            },
        ) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    navController.navigateAndClearBackStack(MainGraph)
                }
            )
        }

        composable<AuthRoute.VerifyEmail>(
            enterTransition = {
                slideInHorizontally(tween(300)) + fadeIn(tween(300))
            },
            exitTransition = {
                slideOutHorizontally(tween(250)) { -it / 3 } + fadeOut(tween(250))
            },
            popEnterTransition = {
                slideInHorizontally(tween(250)) { -it / 3 } + fadeIn(tween(250))
            },
            popExitTransition = {
                slideOutHorizontally(tween(300)) + fadeOut(tween(300))
            },
        ) { backStackEntry ->
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

        composable<AuthRoute.ForgotPassword>(
            enterTransition = {
                slideInHorizontally(tween(300)) + fadeIn(tween(300))
            },
            exitTransition = {
                slideOutHorizontally(tween(250)) { -it / 3 } + fadeOut(tween(250))
            },
            popEnterTransition = {
                slideInHorizontally(tween(250)) { -it / 3 } + fadeIn(tween(250))
            },
            popExitTransition = {
                slideOutHorizontally(tween(300)) + fadeOut(tween(300))
            },
        ) {
            ForgotPasswordScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToResetPassword = { token ->
                    navController.safeNavigate(AuthRoute.ResetPassword(token))
                }
            )
        }

        composable<AuthRoute.ResetPassword>(
            enterTransition = {
                slideInHorizontally(tween(300)) + fadeIn(tween(300))
            },
            exitTransition = {
                slideOutHorizontally(tween(250)) { -it / 3 } + fadeOut(tween(250))
            },
            popEnterTransition = {
                slideInHorizontally(tween(250)) { -it / 3 } + fadeIn(tween(250))
            },
            popExitTransition = {
                slideOutHorizontally(tween(300)) + fadeOut(tween(300))
            },
        ) { backStackEntry ->
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