package com.oussama_chatri.productivityx.features.tasks.navigation

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.oussama_chatri.productivityx.features.tasks.presentation.screens.AddEditTaskSheet
import com.oussama_chatri.productivityx.features.tasks.presentation.screens.TaskDetailScreen
import com.oussama_chatri.productivityx.features.tasks.presentation.screens.TaskStatsScreen
import com.oussama_chatri.productivityx.features.tasks.presentation.screens.TaskTrashScreen
import com.oussama_chatri.productivityx.features.tasks.presentation.screens.TasksScreen
import com.oussama_chatri.productivityx.features.tasks.presentation.viewmodel.TaskStatsViewModel

object TaskRoutes {
    const val TASKS = "tasks"
    const val TASK_DETAIL = "tasks/{taskId}"
    const val ADD_TASK = "tasks/add"
    const val EDIT_TASK = "tasks/{taskId}/edit"
    const val ADD_SUBTASK = "tasks/{parentTaskId}/subtask/add"
    const val TASK_TRASH = "tasks/trash"
    const val TASK_STATS = "tasks/stats"

    fun taskDetail(taskId: String) = "tasks/$taskId"
    fun editTask(taskId: String) = "tasks/$taskId/edit"
    fun addSubtask(parentTaskId: String) = "tasks/$parentTaskId/subtask/add"
}

fun NavGraphBuilder.tasksNavGraph(navController: NavController) {

    composable(route = TaskRoutes.TASKS) {
        TasksScreen(
            onTaskClick = { taskId -> navController.navigate(TaskRoutes.taskDetail(taskId)) },
            onAddTask = { navController.navigate(TaskRoutes.ADD_TASK) },
            onNavigateToStats = { navController.navigate(TaskRoutes.TASK_STATS) }
        )
    }

    composable(
        route = TaskRoutes.TASK_DETAIL,
        arguments = listOf(navArgument("taskId") { type = NavType.StringType })
    ) { backStackEntry ->
        val taskId = requireNotNull(backStackEntry.arguments?.getString("taskId"))
        TaskDetailScreen(
            taskId = taskId,
            onNavigateBack = { navController.popBackStack() },
            onEditTask = { id -> navController.navigate(TaskRoutes.editTask(id)) }
        )
    }

    composable(route = TaskRoutes.ADD_TASK) {
        AddEditTaskSheet(
            onDismiss = { navController.popBackStack() }
        )
    }

    composable(
        route = TaskRoutes.EDIT_TASK,
        arguments = listOf(navArgument("taskId") { type = NavType.StringType })
    ) { backStackEntry ->
        val taskId = requireNotNull(backStackEntry.arguments?.getString("taskId"))
        AddEditTaskSheet(
            taskId = taskId,
            onDismiss = { navController.popBackStack() }
        )
    }

    composable(
        route = TaskRoutes.ADD_SUBTASK,
        arguments = listOf(navArgument("parentTaskId") { type = NavType.StringType })
    ) { backStackEntry ->
        val parentTaskId = requireNotNull(backStackEntry.arguments?.getString("parentTaskId"))
        AddEditTaskSheet(
            parentTaskId = parentTaskId,
            onDismiss = { navController.popBackStack() }
        )
    }

    composable(route = TaskRoutes.TASK_TRASH) {
        TaskTrashScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable(route = TaskRoutes.TASK_STATS) {
        val viewModel: TaskStatsViewModel = hiltViewModel()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        TaskStatsScreen(
            onNavigateBack = { navController.popBackStack() },
            uiState = uiState
        )
    }
}
