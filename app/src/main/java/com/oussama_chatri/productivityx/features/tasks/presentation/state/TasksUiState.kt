package com.oussama_chatri.productivityx.features.tasks.presentation.state

import com.oussama_chatri.productivityx.core.enums.Priority
import com.oussama_chatri.productivityx.core.enums.TaskStatus
import com.oussama_chatri.productivityx.core.enums.TaskView
import com.oussama_chatri.productivityx.features.tasks.domain.model.Task
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

data class TasksUiState(
    val isLoading: Boolean = false,
    val tasks: List<Task> = emptyList(),
    val activeTab: TaskTab = TaskTab.ALL,
    val viewMode: TaskView = TaskView.LIST,
    val filterStatus: TaskStatus? = null,
    val filterPriority: Priority? = null,
    val error: String? = null,
    val isSyncing: Boolean = false
)

data class TaskDetailUiState(
    val isLoading: Boolean = false,
    val task: Task? = null,
    val error: String? = null
)

data class AddEditTaskUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val taskId: String? = null,
    val title: String = "",
    val description: String = "",
    val status: TaskStatus = TaskStatus.TODO,
    val priority: Priority = Priority.MEDIUM,
    val dueDate: LocalDate? = null,
    val dueTime: LocalTime? = null,
    val reminderAt: Instant? = null,
    val estimatedMinutes: Int? = null,
    val parentTaskId: String? = null,
    val linkedEventId: String? = null,
    val newSubtaskTitle: String = "",
    val subtasks: List<Task> = emptyList(),
    val titleError: String? = null,
    val isSaved: Boolean = false,
    val isDeleted: Boolean = false
) {
    val isEditMode: Boolean get() = taskId != null
    val canSave: Boolean get() = title.isNotBlank()
}

data class TaskTrashUiState(
    val isLoading: Boolean = false,
    val tasks: List<Task> = emptyList(),
    val error: String? = null
)

enum class TaskTab { ALL, TODAY, UPCOMING, COMPLETED }

val TaskTab.label: String get() = when (this) {
    TaskTab.ALL -> "All"
    TaskTab.TODAY -> "Today"
    TaskTab.UPCOMING -> "Upcoming"
    TaskTab.COMPLETED -> "Completed"
}

// Filtered list helpers used by ViewModel
fun List<Task>.filterForTab(tab: TaskTab): List<Task> = when (tab) {
    TaskTab.ALL -> filter { !it.isDeleted && it.status != TaskStatus.CANCELLED }
    TaskTab.TODAY -> filter { it.isDueToday }
    TaskTab.UPCOMING -> filter {
        !it.isDeleted
                && it.dueDate != null
                && it.dueDate.isAfter(LocalDate.now())
                && it.status != TaskStatus.DONE
                && it.status != TaskStatus.CANCELLED
    }
    TaskTab.COMPLETED -> filter { it.status == TaskStatus.DONE && !it.isDeleted }
}

val TaskStatus.displayLabel: String get() = when (this) {
    TaskStatus.TODO -> "To Do"
    TaskStatus.IN_PROGRESS -> "In Progress"
    TaskStatus.ON_HOLD -> "On Hold"
    TaskStatus.DONE -> "Done"
    TaskStatus.CANCELLED -> "Cancelled"
}
