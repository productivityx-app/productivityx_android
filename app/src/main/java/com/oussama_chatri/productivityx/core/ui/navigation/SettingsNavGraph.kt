package com.oussama_chatri.productivityx.core.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.oussama_chatri.productivityx.features.settings.presentation.changepassword.ChangePasswordScreen
import com.oussama_chatri.productivityx.features.settings.presentation.editprofile.EditProfileScreen
import com.oussama_chatri.productivityx.features.settings.presentation.preferences.PreferencesScreen
import com.oussama_chatri.productivityx.features.settings.presentation.profile.ProfileScreen

fun NavGraphBuilder.settingsNavGraph(
    navController: NavController,
    onSignedOut: () -> Unit
) {
    composable<SettingsRoute.Profile> {
        ProfileScreen(
            onNavigateToEditProfile    = { navController.navigate(SettingsRoute.EditProfile) },
            onNavigateToChangePassword = { navController.navigate(SettingsRoute.ChangePassword) },
            onNavigateToPreferences    = { navController.navigate(SettingsRoute.Preferences) },
            onSignedOut                = onSignedOut
        )
    }

    composable<SettingsRoute.EditProfile> {
        EditProfileScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable<SettingsRoute.Preferences> {
        PreferencesScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable<SettingsRoute.ChangePassword> {
        ChangePasswordScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }
}
