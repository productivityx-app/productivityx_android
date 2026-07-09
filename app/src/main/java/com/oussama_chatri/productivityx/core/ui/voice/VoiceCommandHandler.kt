package com.oussama_chatri.productivityx.core.ui.voice

import android.content.Intent
import com.oussama_chatri.productivityx.core.ui.navigation.MainRoute
import com.oussama_chatri.productivityx.features.home.presentation.state.QuickAction

object VoiceCommandHandler {
    fun parseCommand(text: String): QuickAction? {
        val lower = text.lowercase().trim()

        return when {
            lower.contains("new note") || lower.contains("create note") || lower.contains("write note") ->
                QuickAction.NEW_NOTE
            lower.contains("new task") || lower.contains("create task") || lower.contains("add task") ->
                QuickAction.NEW_TASK
            lower.contains("start timer") || lower.contains("start focus") || lower.contains("pomodoro") ->
                QuickAction.START_TIMER
            lower.contains("ai") || lower.contains("chat") || lower.contains("assistant") ->
                QuickAction.AI_CHAT
            lower.contains("calculator") || lower.contains("calc") || lower.contains("math") ->
                QuickAction.CALCULATOR
            else -> null
        }
    }

    fun getNavigationRoute(action: QuickAction): MainRoute = when (action) {
        QuickAction.NEW_NOTE -> MainRoute.Notes
        QuickAction.NEW_TASK -> MainRoute.Tasks
        QuickAction.START_TIMER -> MainRoute.Pomodoro
        QuickAction.AI_CHAT -> MainRoute.Ai
        QuickAction.CALCULATOR -> MainRoute.Home // Calculator is a bottom sheet on Home
    }

    fun getDeepLink(action: QuickAction): String = when (action) {
        QuickAction.NEW_NOTE -> "productivityx://notes/new"
        QuickAction.NEW_TASK -> "productivityx://tasks/new"
        QuickAction.START_TIMER -> "productivityx://pomodoro/start"
        QuickAction.AI_CHAT -> "productivityx://ai/chat"
        QuickAction.CALCULATOR -> "productivityx://calculator"
    }
}
