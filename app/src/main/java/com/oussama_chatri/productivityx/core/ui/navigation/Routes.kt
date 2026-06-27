package com.oussama_chatri.productivityx.core.ui.navigation

import kotlinx.serialization.Serializable

@Serializable object AuthGraph
@Serializable object MainGraph

sealed class AuthRoute {
    @Serializable data object Splash : AuthRoute()
    @Serializable data object Onboarding : AuthRoute()
    @Serializable data object Login : AuthRoute()
    @Serializable data object Register : AuthRoute()
    @Serializable data class VerifyEmail(val email: String = "") : AuthRoute()
    @Serializable data object ForgotPassword : AuthRoute()
    @Serializable data class ResetPassword(val token: String = "") : AuthRoute()
}

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

sealed interface Routes {

    @Serializable data object Splash         : Routes
    @Serializable data object Onboarding     : Routes
    @Serializable data object Login          : Routes
    @Serializable data object Register       : Routes
    @Serializable data class  VerifyEmail(val email: String) : Routes
    @Serializable data object ForgotPassword : Routes
    @Serializable data class  ResetPassword(val token: String) : Routes

    @Serializable data object Home           : Routes
    @Serializable data object Notes          : Routes
    @Serializable data class  NoteEditor(val noteId: String? = null) : Routes
    @Serializable data object NoteTrash      : Routes

    @Serializable data object Tasks          : Routes
    @Serializable data class  TaskDetail(val taskId: String) : Routes

    @Serializable data class Calendar(val showAddEvent: Boolean = false) : Routes
    @Serializable data class  EventDetail(val eventId: String) : Routes

    @Serializable data object Pomodoro       : Routes
    @Serializable data object PomodoroHistory : Routes

    // AI — AiChat is the default/new-conversation entry point
    @Serializable data object AiChat             : Routes
    @Serializable data class  AiConversation(val conversationId: String) : Routes
    @Serializable data object ConversationList   : Routes

    @Serializable data object Profile        : Routes
    @Serializable data object EditProfile    : Routes
    @Serializable data object Preferences    : Routes
    @Serializable data object ChangePassword : Routes
}

// Bottom-nav shorthand objects (used in AppNavGraph bottom bar)
@Serializable object HomeRoute
@Serializable object NotesRoute
@Serializable object TasksRoute
@Serializable object CalendarRoute
@Serializable object AiRoute