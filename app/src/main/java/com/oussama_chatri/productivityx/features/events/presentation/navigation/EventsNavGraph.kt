package com.oussama_chatri.productivityx.features.events.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.oussama_chatri.productivityx.core.ui.navigation.Routes
import com.oussama_chatri.productivityx.features.events.presentation.screen.CalendarScreen
import com.oussama_chatri.productivityx.features.events.presentation.screen.EventDetailScreen

fun NavGraphBuilder.eventsNavGraph(navController: NavHostController) {

    composable<Routes.Calendar> {
        CalendarScreen(
            onNavigateToEventDetail = { eventId ->
                navController.navigate(Routes.EventDetail(eventId))
            }
        )
    }

    composable<Routes.EventDetail> { backStackEntry ->
        val route: Routes.EventDetail = backStackEntry.toRoute()
        EventDetailScreen(
            eventId = route.eventId,
            onBack  = { navController.popBackStack() },
            onEdit  = { eventId ->
                navController.popBackStack()
                navController.navigate(Routes.Calendar)
                // sheet opened via CalendarViewModel after nav
            }
        )
    }
}
