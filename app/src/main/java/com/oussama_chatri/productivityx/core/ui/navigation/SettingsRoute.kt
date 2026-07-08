package com.oussama_chatri.productivityx.core.ui.navigation

import kotlinx.serialization.Serializable

sealed class SettingsRoute {
    @Serializable data object Profile        : SettingsRoute()
    @Serializable data object EditProfile    : SettingsRoute()
    @Serializable data object Preferences    : SettingsRoute()
    @Serializable data object ChangePassword : SettingsRoute()
    @Serializable data object TermsAndConditions : SettingsRoute()
    @Serializable data object PrivacyPolicy : SettingsRoute()
    @Serializable data object Licenses : SettingsRoute()
    @Serializable data object Credits : SettingsRoute()
    @Serializable data object Faq : SettingsRoute()
}
