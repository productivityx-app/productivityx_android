package com.oussama_chatri.productivityx.features.tasks.presentation.event

import com.oussama_chatri.productivityx.core.enums.Priority
import com.oussama_chatri.productivityx.core.enums.TaskStatus
import com.oussama_chatri.productivityx.core.enums.TaskView
import com.oussama_chatri.productivityx.features.tasks.presentation.state.TaskTab
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

// Events from TasksScreen
sealed interface TasksEvent {
    data class SelectTab(val tab: TaskTab) : TasksEvent
    data class ToggleView(val view: TaskView) : TasksEvent
    data class FilterByStatus(val status: TaskStatus?) : TasksEvent
    data class FilterByPriority(val priority: Priority?) : TasksEvent
    data class CompleteTask(val taskId: String) : TasksEvent
    data class DeleteTask(val taskId: String) : TasksEvent
    data class UndoDelete(val taskId: String) : TasksEvent
    data class ReorderTasks(val items: List<Pair<String, Int>>) : TasksEvent
    data object Refresh : TasksEvent
}

// Events from AddEditTaskSheet / AddEditTaskScreen
sealed interface AddEditTaskEvent {
    data class TitleChanged(val value: String) : AddEditTaskEvent
    data class DescriptionChanged(val value: String) : AddEditTaskEvent
    data class StatusChanged(val status: TaskStatus) : AddEditTaskEvent
    data class PriorityChanged(val priority: Priority) : AddEditTaskEvent
    data class DueDateChanged(val date: LocalDate?) : AddEditTaskEvent
    data class DueTimeChanged(val time: LocalTime?) : AddEditTaskEvent
    data class ReminderChanged(val instant: Instant?) : AddEditTaskEvent
    data class EstimatedMinutesChanged(val minutes: Int?) : AddEditTaskEvent
    data class NewSubtaskTitleChanged(val title: String) : AddEditTaskEvent
    data object AddSubtask : AddEditTaskEvent
    data class RemoveSubtask(val subtaskId: String) : AddEditTaskEvent
    data object Save : AddEditTaskEvent
    data object Delete : AddEditTaskEvent
    data object ClearError : AddEditTaskEvent
}

// Events from TaskDetailScreen
sealed interface TaskDetailEvent {
    data class StatusChanged(val status: TaskStatus) : TaskDetailEvent
    data class CompleteTask(val taskId: String) : TaskDetailEvent
    data object DeleteTask : TaskDetailEvent
    data object RestoreTask : TaskDetailEvent
    data object Refresh : TaskDetailEvent
}

// Events from TaskTrashScreen
sealed interface TaskTrashEvent {
    data class RestoreTask(val taskId: String) : TaskTrashEvent
    data class HardDeleteTask(val taskId: String) : TaskTrashEvent
    data object Refresh : TaskTrashEvent
}
