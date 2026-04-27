package com.oussama_chatri.productivityx.features.ai.presentation.navigation

import kotlinx.serialization.Serializable

/**
 * Add these route objects to your existing Routes.kt (core/ui/navigation/Routes.kt).
 *
 * Example:
 *
 *   object Routes {
 *       // … existing routes …
 *       @Serializable object Ai
 *       @Serializable data class AiConversation(val conversationId: String)
 *       @Serializable object ConversationList
 *   }
 */

// Kept here as documentation — actual objects live in Routes.kt

@Serializable
object AiRoute

@Serializable
data class AiConversationRoute(val conversationId: String)

@Serializable
object ConversationListRoute
