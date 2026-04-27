package com.oussama_chatri.productivityx.core.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.oussama_chatri.productivityx.core.ui.components.PxBottomNavBar
import com.oussama_chatri.productivityx.features.ai.presentation.navigation.aiNavGraph
import com.oussama_chatri.productivityx.features.events.presentation.navigation.eventsNavGraph
import com.oussama_chatri.productivityx.features.notes.presentation.NotesRoute
import com.oussama_chatri.productivityx.features.notes.presentation.editor.NoteEditorScreen
import com.oussama_chatri.productivityx.features.notes.presentation.list.NotesScreen
import com.oussama_chatri.productivityx.features.notes.presentation.trash.TrashScreen
import com.oussama_chatri.productivityx.features.pomodoro.navigation.PomodoroRoute
import com.oussama_chatri.productivityx.features.pomodoro.navigation.pomodoroNavGraph
import com.oussama_chatri.productivityx.features.tasks.navigation.TaskRoutes
import com.oussama_chatri.productivityx.features.tasks.navigation.tasksNavGraph

fun NavGraphBuilder.mainNavGraph(rootNavController: NavHostController) {
    navigation<MainGraph>(startDestination = MainRoute.Home) {

        composable<MainRoute.Home> {
            HomeTab(rootNavController)
        }

        composable<MainRoute.Notes> {
            NotesTab(rootNavController)
        }

        composable<MainRoute.Tasks> {
            TasksTab(rootNavController)
        }

        composable<MainRoute.Pomodoro> {
            PomodoroTab(rootNavController)
        }

        composable<MainRoute.Ai> {
            AiTab(rootNavController)
        }

        // Profile is not a bottom-nav tab — reached via HomeTab's avatar button
        composable<MainRoute.Profile> {
            ProfileTab(rootNavController)
        }
    }
}

// Shared bottom-bar nav helper

private fun NavHostController.navigateToTab(route: MainRoute) {
    navigate(route) {
        popUpTo(MainRoute.Home) {
            saveState = true
        }
        launchSingleTop = true
        restoreState    = true
    }
}

// ── Home Tab ──────────────────────────────────────────────────────────────────

@Composable
private fun HomeTab(rootNavController: NavHostController) {
    val rootBackStack by rootNavController.currentBackStackEntryAsState()
    val currentRoute  = rootBackStack?.destination?.route

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            PxBottomNavBar(
                currentRoute   = currentRoute,
                onNavItemClick = { rootNavController.navigateToTab(it) },
                modifier       = Modifier.navigationBarsPadding(),
            )
        },
    ) { innerPadding ->
        // TODO: replace with real HomeScreen once implemented
        // HomeScreen(
        //     onNavigateToProfile = { rootNavController.navigate(MainRoute.Profile) },
        //     modifier = Modifier.padding(innerPadding),
        // )
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        )
    }
}

// Notes Tab

@Composable
private fun NotesTab(rootNavController: NavHostController) {
    val notesNavController = rememberNavController()
    val notesBackStack     by notesNavController.currentBackStackEntryAsState()
    val notesRoute         = notesBackStack?.destination?.route

    // Bottom bar visible only on the notes list, not inside editor/trash
    val isTopLevel = notesRoute?.contains("NoteEditor") == false &&
            notesRoute?.contains("Trash") == false

    val rootBackStack by rootNavController.currentBackStackEntryAsState()
    val currentRoute  = rootBackStack?.destination?.route

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (isTopLevel) {
                PxBottomNavBar(
                    currentRoute   = currentRoute,
                    onNavItemClick = { rootNavController.navigateToTab(it) },
                    modifier       = Modifier.navigationBarsPadding(),
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController    = notesNavController,
            startDestination = NotesRoute.NotesList,
            modifier         = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            composable<NotesRoute.NotesList> {
                NotesScreen(
                    onNavigateToEditor = { noteId ->
                        notesNavController.navigate(NotesRoute.NoteEditor(noteId))
                    },
                    onNavigateToSearch = {
                        rootNavController.navigate(MainRoute.Search)
                    },
                    onNavigateToTrash = {
                        notesNavController.navigate(NotesRoute.Trash)
                    },
                )
            }

            composable<NotesRoute.NoteEditor> { entry ->
                val route = entry.toRoute<NotesRoute.NoteEditor>()
                NoteEditorScreen(
                    noteId         = route.noteId,
                    onNavigateBack = { notesNavController.navigateUp() },
                )
            }

            composable<NotesRoute.Trash> {
                TrashScreen(
                    onNavigateBack = { notesNavController.navigateUp() },
                )
            }
        }
    }
}

// Tasks Tab

@Composable
private fun TasksTab(rootNavController: NavHostController) {
    val tasksNavController = rememberNavController()
    val tasksBackStack     by tasksNavController.currentBackStackEntryAsState()
    val tasksRoute         = tasksBackStack?.destination?.route

    val isTopLevel = tasksRoute == TaskRoutes.TASKS

    val rootBackStack by rootNavController.currentBackStackEntryAsState()
    val currentRoute  = rootBackStack?.destination?.route

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (isTopLevel) {
                PxBottomNavBar(
                    currentRoute   = currentRoute,
                    onNavItemClick = { rootNavController.navigateToTab(it) },
                    modifier       = Modifier.navigationBarsPadding(),
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController    = tasksNavController,
            startDestination = TaskRoutes.TASKS,
            modifier         = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            tasksNavGraph(tasksNavController)
        }
    }
}

// Pomodoro Tab

@Composable
private fun PomodoroTab(rootNavController: NavHostController) {
    val pomodoroNavController = rememberNavController()
    val pomodoroBackStack     by pomodoroNavController.currentBackStackEntryAsState()
    val pomodoroRoute         = pomodoroBackStack?.destination?.route

    // Bottom bar visible on the timer screen, not on session history
    val isTopLevel = pomodoroRoute?.contains("History") == false

    val rootBackStack by rootNavController.currentBackStackEntryAsState()
    val currentRoute  = rootBackStack?.destination?.route

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (isTopLevel) {
                PxBottomNavBar(
                    currentRoute   = currentRoute,
                    onNavItemClick = { rootNavController.navigateToTab(it) },
                    modifier       = Modifier.navigationBarsPadding(),
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController    = pomodoroNavController,
            startDestination = PomodoroRoute.Timer,
            modifier         = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            pomodoroNavGraph(pomodoroNavController)
        }
    }
}

// AI Tab

@Composable
private fun AiTab(rootNavController: NavHostController) {
    val aiNavController = rememberNavController()
    val aiBackStack     by aiNavController.currentBackStackEntryAsState()
    val aiRoute         = aiBackStack?.destination?.route

    // Bottom bar hidden when viewing conversation list (it's a full-screen overlay)
    val isTopLevel = aiRoute?.contains("ConversationList") == false

    val rootBackStack by rootNavController.currentBackStackEntryAsState()
    val currentRoute  = rootBackStack?.destination?.route

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (isTopLevel) {
                PxBottomNavBar(
                    currentRoute   = currentRoute,
                    onNavItemClick = { rootNavController.navigateToTab(it) },
                    modifier       = Modifier.navigationBarsPadding(),
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController    = aiNavController,
            startDestination = Routes.AiChat,
            modifier         = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            aiNavGraph(aiNavController)
        }
    }
}

// Calendar Tab
// Kept as a utility — Calendar is reachable from Home dashboard cards and task links
// without its own bottom-nav tab. If you want it as a tab, add it to PxBottomNavBar.

@Composable
private fun CalendarTab(rootNavController: NavHostController) {
    val calendarNavController = rememberNavController()
    val calendarBackStack     by calendarNavController.currentBackStackEntryAsState()
    val calendarRoute         = calendarBackStack?.destination?.route

    val isTopLevel = calendarRoute?.contains("EventDetail") == false

    val rootBackStack by rootNavController.currentBackStackEntryAsState()
    val currentRoute  = rootBackStack?.destination?.route

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (isTopLevel) {
                PxBottomNavBar(
                    currentRoute   = currentRoute,
                    onNavItemClick = { rootNavController.navigateToTab(it) },
                    modifier       = Modifier.navigationBarsPadding(),
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController    = calendarNavController,
            startDestination = Routes.Calendar,
            modifier         = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            eventsNavGraph(calendarNavController)
        }
    }
}

// Profile Tab
// Not in bottom nav — opened via avatar tap on the Home top bar.

@Composable
private fun ProfileTab(rootNavController: NavHostController) {
    val profileNavController = rememberNavController()
    val profileBackStack     by profileNavController.currentBackStackEntryAsState()
    val profileRoute         = profileNavController.currentBackStackEntryAsState().value?.destination?.route

    val isTopLevel = profileRoute?.let {
        !it.contains("EditProfile") &&
                !it.contains("Preferences") &&
                !it.contains("ChangePassword")
    } ?: true

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        NavHost(
            navController    = profileNavController,
            startDestination = SettingsRoute.Profile,
            modifier         = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            settingsNavGraph(
                navController = profileNavController,
                onSignedOut   = {
                    rootNavController.navigate(AuthRoute.Login) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }
    }
}