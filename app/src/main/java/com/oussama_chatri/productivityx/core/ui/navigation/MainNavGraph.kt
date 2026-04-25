package com.oussama_chatri.productivityx.core.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import com.oussama_chatri.productivityx.features.events.presentation.navigation.eventsNavGraph
import com.oussama_chatri.productivityx.features.notes.presentation.NotesRoute
import com.oussama_chatri.productivityx.features.notes.presentation.editor.NoteEditorScreen
import com.oussama_chatri.productivityx.features.notes.presentation.list.NotesScreen
import com.oussama_chatri.productivityx.features.notes.presentation.trash.TrashScreen
import com.oussama_chatri.productivityx.features.tasks.navigation.tasksNavGraph

fun NavGraphBuilder.mainNavGraph(rootNavController: NavHostController) {
    navigation<MainGraph>(startDestination = MainRoute.Home) {

        composable<MainRoute.Home> {
            MainShell(rootNavController, startDestination = "home")
        }

        composable<MainRoute.Notes> {
            NotesTab(rootNavController)
        }

        composable<MainRoute.Tasks> {
            TasksTab(rootNavController)
        }

        composable<MainRoute.Calendar> {
            CalendarTab(rootNavController)
        }

        composable<MainRoute.Pomodoro> {
            MainShell(rootNavController, startDestination = "pomodoro")
        }

        composable<MainRoute.Ai> {
            MainShell(rootNavController, startDestination = "ai")
        }

        composable<MainRoute.Search> {
            MainShell(rootNavController, startDestination = "search")
        }

        // Profile tab — replaced MainShell placeholder with real nested nav
        composable<MainRoute.Profile> {
            ProfileTab(rootNavController)
        }
    }
}

// Notes uses its own nested NavHost so the bottom bar persists on the list screen
// but hides on editor / trash
@Composable
private fun NotesTab(rootNavController: NavHostController) {
    val notesNavController = rememberNavController()
    val backStackEntry     by notesNavController.currentBackStackEntryAsState()
    val currentRoute       = backStackEntry?.destination?.route

    val isTopLevel = currentRoute?.contains("NoteEditor") == false &&
            currentRoute?.contains("Trash") == false

    val rootBackStackEntry by rootNavController.currentBackStackEntryAsState()
    val rootRoute          = rootBackStackEntry?.destination

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (isTopLevel) {
                PxBottomNavBar(
                    currentRoute   = rootRoute?.route,
                    onNavItemClick = { route ->
                        rootNavController.navigate(route) {
                            popUpTo(MainRoute.Home) { saveState = true }
                            launchSingleTop = true
                            restoreState    = true
                        }
                    },
                    modifier = Modifier.navigationBarsPadding()
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController    = notesNavController,
            startDestination = NotesRoute.NotesList,
            modifier         = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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
                    }
                )
            }

            composable<NotesRoute.NoteEditor> { backStackEntry ->
                val route = backStackEntry.toRoute<NotesRoute.NoteEditor>()
                NoteEditorScreen(
                    noteId         = route.noteId,
                    onNavigateBack = { notesNavController.navigateUp() }
                )
            }

            composable<NotesRoute.Trash> {
                TrashScreen(
                    onNavigateBack = { notesNavController.navigateUp() }
                )
            }
        }
    }
}

// Tasks uses its own nested NavHost — bottom bar persists on the list, hides on detail/add
@Composable
private fun TasksTab(rootNavController: NavHostController) {
    val tasksNavController = rememberNavController()
    val backStackEntry     by tasksNavController.currentBackStackEntryAsState()
    val currentRoute       = backStackEntry?.destination?.route

    val isTopLevel = currentRoute == com.oussama_chatri.productivityx.features.tasks.navigation.TaskRoutes.TASKS

    val rootBackStackEntry by rootNavController.currentBackStackEntryAsState()
    val rootRoute          = rootBackStackEntry?.destination

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (isTopLevel) {
                PxBottomNavBar(
                    currentRoute   = rootRoute?.route,
                    onNavItemClick = { route ->
                        rootNavController.navigate(route) {
                            popUpTo(MainRoute.Home) { saveState = true }
                            launchSingleTop = true
                            restoreState    = true
                        }
                    },
                    modifier = Modifier.navigationBarsPadding()
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController    = tasksNavController,
            startDestination = com.oussama_chatri.productivityx.features.tasks.navigation.TaskRoutes.TASKS,
            modifier         = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            tasksNavGraph(tasksNavController)
        }
    }
}

// Calendar uses its own nested NavHost — bottom bar persists on calendar, hides on event detail
@Composable
private fun CalendarTab(rootNavController: NavHostController) {
    val calendarNavController = rememberNavController()
    val backStackEntry        by calendarNavController.currentBackStackEntryAsState()
    val currentRoute          = backStackEntry?.destination?.route

    val isTopLevel = currentRoute?.contains("EventDetail") == false

    val rootBackStackEntry by rootNavController.currentBackStackEntryAsState()
    val rootRoute          = rootBackStackEntry?.destination

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (isTopLevel) {
                PxBottomNavBar(
                    currentRoute   = rootRoute?.route,
                    onNavItemClick = { route ->
                        rootNavController.navigate(route) {
                            popUpTo(MainRoute.Home) { saveState = true }
                            launchSingleTop = true
                            restoreState    = true
                        }
                    },
                    modifier = Modifier.navigationBarsPadding()
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController    = calendarNavController,
            startDestination = Routes.Calendar,
            modifier         = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            eventsNavGraph(calendarNavController)
        }
    }
}

// Profile tab — nested NavHost so bottom bar stays on ProfileScreen
// but disappears on EditProfile / Preferences / ChangePassword sub-screens
@Composable
private fun ProfileTab(rootNavController: NavHostController) {
    val profileNavController = rememberNavController()
    val backStackEntry       by profileNavController.currentBackStackEntryAsState()
    val currentRoute         = backStackEntry?.destination?.route

    // Bottom bar only visible on the root profile screen
    val isTopLevel = currentRoute?.let {
        !it.contains("EditProfile") &&
        !it.contains("Preferences") &&
        !it.contains("ChangePassword")
    } ?: true

    val rootBackStackEntry by rootNavController.currentBackStackEntryAsState()
    val rootRoute          = rootBackStackEntry?.destination

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (isTopLevel) {
                PxBottomNavBar(
                    currentRoute   = rootRoute?.route,
                    onNavItemClick = { route ->
                        rootNavController.navigate(route) {
                            popUpTo(MainRoute.Home) { saveState = true }
                            launchSingleTop = true
                            restoreState    = true
                        }
                    },
                    modifier = Modifier.navigationBarsPadding()
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController    = profileNavController,
            startDestination = SettingsRoute.Profile,
            modifier         = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            settingsNavGraph(
                navController = profileNavController,
                onSignedOut   = {
                    rootNavController.navigate(AuthRoute.Login) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}

// Generic shell for sections not yet fully implemented
@Composable
private fun MainShell(navController: NavHostController, startDestination: String) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute   = backStackEntry?.destination

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            PxBottomNavBar(
                currentRoute   = currentRoute?.route,
                onNavItemClick = { route ->
                    navController.navigate(route) {
                        popUpTo(MainRoute.Home) { saveState = true }
                        launchSingleTop = true
                        restoreState    = true
                    }
                },
                modifier = Modifier.navigationBarsPadding()
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        )
    }
}
