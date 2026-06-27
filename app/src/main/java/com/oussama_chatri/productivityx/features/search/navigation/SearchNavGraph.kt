package com.oussama_chatri.productivityx.features.search.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.oussama_chatri.productivityx.core.ui.navigation.MainRoute
import com.oussama_chatri.productivityx.core.ui.navigation.Routes
import com.oussama_chatri.productivityx.features.search.presentation.SearchScreen

fun NavGraphBuilder.searchNavGraph(rootNavController: NavHostController) {
    composable<MainRoute.Search> {
        SearchScreen(
            onNavigateBack = { rootNavController.popBackStack() },
            onNoteClick = { noteId ->
                rootNavController.navigate(Routes.NoteEditor(noteId))
            },
            onTaskClick = { taskId ->
                rootNavController.navigate(Routes.TaskDetail(taskId))
            },
            onEventClick = { eventId ->
                rootNavController.navigate(Routes.EventDetail(eventId))
            }
        )
    }
}
