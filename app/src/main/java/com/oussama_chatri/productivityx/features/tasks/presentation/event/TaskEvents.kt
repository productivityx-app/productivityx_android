package com.oussama_chatri.productivityx.features.tasks.presentation.event

import com.oussama_chatri.productivityx.core.enums.Priority
import com.oussama_chatri.productivityx.core.enums.RecurrenceType
import com.oussama_chatri.productivityx.core.enums.TaskStatus
import com.oussama_chatri.productivityx.core.enums.TaskView
import com.oussama_chatri.productivityx.features.tasks.domain.model.TaskFilter
import com.oussama_chatri.productivityx.features.tasks.presentation.state.TaskTab
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

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

    // Smart filter
    data class SetSmartFilter(val filter: com.oussama_chatri.productivityx.features.tasks.domain.model.TaskSmartFilter) : TasksEvent
    data class SetTagFilter(val tag: String?) : TasksEvent
    data class SetSearchQuery(val query: String) : TasksEvent
    data class SetCustomDateRange(val start: LocalDate?, val end: LocalDate?) : TasksEvent

    // Multi-select
    data class ToggleTaskSelection(val taskId: String) : TasksEvent
    data object SelectAll : TasksEvent
    data object ClearSelection : TasksEvent
    data object EnterMultiSelectMode : TasksEvent
    data object ExitMultiSelectMode : TasksEvent

    // Bulk actions
    data object BulkComplete : TasksEvent
    data object BulkDelete : TasksEvent
    data class BulkReschedule(val date: LocalDate) : TasksEvent
    data class BulkSetPriority(val priority: Priority) : TasksEvent
    data class BulkAddTag(val tag: String) : TasksEvent

    // Calendar navigation
    data class CalendarNavigateMonth(val delta: Int) : TasksEvent
    data class SelectCalendarDate(val date: LocalDate?) : TasksEvent

    // Timeline navigation
    data class TimelineZoomIn(val days: Int = 7) : TasksEvent
    data class TimelineZoomOut(val days: Int = 7) : TasksEvent
    data class TimelinePan(val delta: Int) : TasksEvent

    // Drag-drop
    data class MoveTaskToStatus(val taskId: String, val status: TaskStatus) : TasksEvent
    data class MoveTaskToPosition(val taskId: String, val position: Int) : TasksEvent
}

sealed interface AddEditTaskEvent {
    data class TitleChanged(val value: String) : AddEditTaskEvent
    data class DescriptionChanged(val value: String) : AddEditTaskEvent
    data class StatusChanged(val status: TaskStatus) : AddEditTaskEvent
    data class PriorityChanged(val priority: Priority) : AddEditTaskEvent
    data class DueDateChanged(val date: LocalDate?) : AddEditTaskEvent
    data class DueTimeChanged(val time: LocalTime?) : AddEditTaskEvent
    data class ReminderChanged(val instant: Instant?) : AddEditTaskEvent
    data class ReminderMinutesChanged(val minutes: Int?) : AddEditTaskEvent
    data class EstimatedMinutesChanged(val minutes: Int?) : AddEditTaskEvent
    data class NewSubtaskTitleChanged(val title: String) : AddEditTaskEvent
    data object AddSubtask : AddEditTaskEvent
    data class RemoveSubtask(val subtaskId: String) : AddEditTaskEvent
    data object Save : AddEditTaskEvent
    data object Delete : AddEditTaskEvent
    data object ClearError : AddEditTaskEvent

    // Tags
    data class AddTag(val tag: String) : AddEditTaskEvent
    data class RemoveTag(val tag: String) : AddEditTaskEvent
    data class NewTagChanged(val tag: String) : AddEditTaskEvent

    // Recurrence
    data class RecurrenceTypeChanged(val type: RecurrenceType) : AddEditTaskEvent
    data class RecurrenceEndDateChanged(val date: LocalDate?) : AddEditTaskEvent
    data class RecurrenceIntervalChanged(val interval: Int?) : AddEditTaskEvent
    data class RecurrenceDaysOfWeekChanged(val days: List<Int>?) : AddEditTaskEvent

    // Priority matrix
    data object TogglePriorityMatrix : AddEditTaskEvent
}

sealed interface TaskDetailEvent {
    data class StatusChanged(val status: TaskStatus) : TaskDetailEvent
    data class CompleteTask(val taskId: String) : TaskDetailEvent
    data object DeleteTask : TaskDetailEvent
    data object RestoreTask : TaskDetailEvent
    data object Refresh : TaskDetailEvent

    // Inline editing
    data object StartEditTitle : TaskDetailEvent
    data object CancelEditTitle : TaskDetailEvent
    data class TitleChanged(val title: String) : TaskDetailEvent
    data object SaveTitle : TaskDetailEvent

    data object StartEditDescription : TaskDetailEvent
    data object CancelEditDescription : TaskDetailEvent
    data class DescriptionChanged(val description: String) : TaskDetailEvent
    data object SaveDescription : TaskDetailEvent

    // Subtask
    data class AddSubtaskInline(val title: String) : TaskDetailEvent
    data class ToggleSubtask(val subtaskId: String) : TaskDetailEvent

    // Reminder / Scheduling
    data class UpdateDueDate(val date: LocalDate?) : TaskDetailEvent
    data class UpdateDueTime(val time: LocalTime?) : TaskDetailEvent
    data class UpdateReminder(val instant: Instant?) : TaskDetailEvent
    data class UpdatePriority(val priority: Priority) : TaskDetailEvent

    // Celebration dismiss
    data object DismissCelebration : TaskDetailEvent
}

sealed interface TaskTrashEvent {
    data class RestoreTask(val taskId: String) : TaskTrashEvent
    data class HardDeleteTask(val taskId: String) : TaskTrashEvent
    data object Refresh : TaskTrashEvent
}

sealed interface TaskStatsEvent {
    data object LoadStats : TaskStatsEvent
    data object Refresh : TaskStatsEvent
    data class ShowBadge(val badge: com.oussama_chatri.productivityx.core.enums.Badge) : TaskStatsEvent
    data class ShareBadge(val badge: com.oussama_chatri.productivityx.core.enums.Badge) : TaskStatsEvent
}
