package com.oussama_chatri.productivityx.core.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.oussama_chatri.productivityx.features.settings.presentation.changepassword.ChangePasswordScreen
import com.oussama_chatri.productivityx.features.settings.presentation.editprofile.EditProfileScreen
import com.oussama_chatri.productivityx.features.settings.presentation.preferences.CreditsScreen
import com.oussama_chatri.productivityx.features.settings.presentation.preferences.FaqScreen
import com.oussama_chatri.productivityx.features.settings.presentation.preferences.LicensesScreen
import com.oussama_chatri.productivityx.features.settings.presentation.preferences.PreferencesScreen
import com.oussama_chatri.productivityx.features.settings.presentation.preferences.PrivacyPolicyScreen
import com.oussama_chatri.productivityx.features.settings.presentation.preferences.TermsAndConditionsScreen
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
            onNavigateBack = { navController.popBackStack() },
            onNavigateToLicenses = { navController.navigate(SettingsRoute.Licenses) },
            onNavigateToCredits = { navController.navigate(SettingsRoute.Credits) },
            onNavigateToFaq = { navController.navigate(SettingsRoute.Faq) },
            onNavigateToTermsAndConditions = { navController.navigate(SettingsRoute.TermsAndConditions) },
            onNavigateToPrivacyPolicy = { navController.navigate(SettingsRoute.PrivacyPolicy) }
        )
    }

    composable<SettingsRoute.ChangePassword> {
        ChangePasswordScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable<SettingsRoute.TermsAndConditions> {
        TermsAndConditionsScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable<SettingsRoute.PrivacyPolicy> {
        PrivacyPolicyScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable<SettingsRoute.Licenses> {
        LicensesScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable<SettingsRoute.Credits> {
        CreditsScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable<SettingsRoute.Faq> {
        FaqScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }
}
