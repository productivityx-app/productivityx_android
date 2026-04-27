package com.oussama_chatri.productivityx.features.ai.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.oussama_chatri.productivityx.core.ui.navigation.Routes
import com.oussama_chatri.productivityx.features.ai.presentation.screen.AiScreen
import com.oussama_chatri.productivityx.features.ai.presentation.screen.ConversationListScreen
import java.util.UUID

fun NavGraphBuilder.aiNavGraph(navController: NavHostController) {

    composable<Routes.AiChat> {
        AiScreen(
            conversationId      = null,
            onNavigateToHistory = { navController.navigate(Routes.ConversationList) },
        )
    }

    composable<Routes.AiConversation> { backStackEntry ->
        val route: Routes.AiConversation = backStackEntry.toRoute()
        AiScreen(
            conversationId      = UUID.fromString(route.conversationId),
            onNavigateToHistory = { navController.navigate(Routes.ConversationList) },
        )
    }

    composable<Routes.ConversationList> {
        ConversationListScreen(
            onBack             = { navController.popBackStack() },
            onOpenConversation = { id ->
                navController.navigate(Routes.AiConversation(id.toString()))
            },
            onNewConversation  = {
                navController.popBackStack()
                navController.navigate(Routes.AiChat)
            },
        )
    }
}