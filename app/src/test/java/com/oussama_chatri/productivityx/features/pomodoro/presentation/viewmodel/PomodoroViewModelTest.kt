package com.oussama_chatri.productivityx.features.pomodoro.presentation.viewmodel

import android.content.Context
import com.oussama_chatri.productivityx.core.enums.PomodoroType
import com.oussama_chatri.productivityx.core.storage.PreferencesDataStore
import com.oussama_chatri.productivityx.features.pomodoro.domain.repository.PomodoroRepository
import com.oussama_chatri.productivityx.features.pomodoro.domain.usecase.GetTodayStatsUseCase
import com.oussama_chatri.productivityx.features.pomodoro.presentation.event.PomodoroUiEvent
import com.oussama_chatri.productivityx.features.pomodoro.presentation.state.AmbientSound
import com.oussama_chatri.productivityx.features.pomodoro.service.AmbientSoundManager
import com.oussama_chatri.productivityx.features.tasks.domain.usecase.ObserveTasksUseCase
import com.oussama_chatri.productivityx.features.tasks.domain.usecase.UpdateTaskStatusUseCase
import com.oussama_chatri.productivityx.features.tasks.domain.usecase.UpdateTaskUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PomodoroViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var context: Context
    private lateinit var getTodayStatsUseCase: GetTodayStatsUseCase
    private lateinit var preferencesDataStore: PreferencesDataStore
    private lateinit var updateTaskStatusUseCase: UpdateTaskStatusUseCase
    private lateinit var updateTaskUseCase: UpdateTaskUseCase
    private lateinit var pomodoroRepository: PomodoroRepository
    private lateinit var observeTasksUseCase: ObserveTasksUseCase
    private lateinit var ambientSoundManager: AmbientSoundManager

    private lateinit var viewModel: PomodoroViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        context = mockk(relaxed = true)
        getTodayStatsUseCase = mockk(relaxed = true)
        preferencesDataStore = mockk(relaxed = true)
        updateTaskStatusUseCase = mockk(relaxed = true)
        updateTaskUseCase = mockk(relaxed = true)
        pomodoroRepository = mockk(relaxed = true)
        observeTasksUseCase = mockk(relaxed = true)
        ambientSoundManager = mockk(relaxed = true)

        coEvery { observeTasksUseCase.invoke(any(), any()) } returns flowOf(emptyList())
        coEvery { preferencesDataStore.pomodoroSessionId } returns flowOf(null)

        viewModel = PomodoroViewModel(
            context,
            getTodayStatsUseCase,
            preferencesDataStore,
            updateTaskStatusUseCase,
            updateTaskUseCase,
            pomodoroRepository,
            observeTasksUseCase,
            ambientSoundManager
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `SelectType event updates selectedType in state`() = runTest {
        // Act
        viewModel.onEvent(PomodoroUiEvent.SelectType(PomodoroType.LONG_BREAK))

        // Assert
        assertEquals(PomodoroType.LONG_BREAK, viewModel.uiState.value.selectedType)
    }

    @Test
    fun `ToggleFocusMode event toggles isFocusMode in state`() = runTest {
        val initialFocusMode = viewModel.uiState.value.isFocusMode

        // Act
        viewModel.onEvent(PomodoroUiEvent.ToggleFocusMode)

        // Assert
        assertEquals(!initialFocusMode, viewModel.uiState.value.isFocusMode)
    }

    @Test
    fun `SelectAmbientSound event updates sound and plays it if running or focus mode`() = runTest {
        // Arrange
        viewModel.onEvent(PomodoroUiEvent.ToggleFocusMode) // Enable focus mode
        
        // Act
        viewModel.onEvent(PomodoroUiEvent.SelectAmbientSound(AmbientSound.RAIN))

        // Assert
        assertEquals(AmbientSound.RAIN, viewModel.uiState.value.selectedAmbientSound)
        io.mockk.verify(exactly = 1) { ambientSoundManager.playSound(AmbientSound.RAIN) }
    }

    @Test
    fun `ShowTaskPicker event updates showTaskPickerSheet in state`() = runTest {
        // Act
        viewModel.onEvent(PomodoroUiEvent.ShowTaskPicker)

        // Assert
        assertTrue(viewModel.uiState.value.showTaskPickerSheet)
    }

    @Test
    fun `SelectTask event updates linked task and closes picker`() = runTest {
        // Arrange
        viewModel.onEvent(PomodoroUiEvent.ShowTaskPicker)

        // Act
        viewModel.onEvent(PomodoroUiEvent.SelectTask("task-1", "Finish testing"))

        // Assert
        assertEquals("task-1", viewModel.uiState.value.linkedTaskId)
        assertEquals("Finish testing", viewModel.uiState.value.linkedTaskTitle)
        assertFalse(viewModel.uiState.value.showTaskPickerSheet)
    }

    @Test
    fun `UnlinkTask event clears linked task`() = runTest {
        // Arrange
        viewModel.onEvent(PomodoroUiEvent.SelectTask("task-1", "Finish testing"))

        // Act
        viewModel.onEvent(PomodoroUiEvent.UnlinkTask)

        // Assert
        assertNull(viewModel.uiState.value.linkedTaskId)
        assertNull(viewModel.uiState.value.linkedTaskTitle)
    }
}
