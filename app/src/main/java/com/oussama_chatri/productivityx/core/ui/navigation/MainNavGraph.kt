package com.oussama_chatri.productivityx.core.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
import com.oussama_chatri.productivityx.features.home.presentation.HomeScreen
import com.oussama_chatri.productivityx.features.notes.presentation.NotesRoute
import com.oussama_chatri.productivityx.features.search.navigation.searchNavGraph
import com.oussama_chatri.productivityx.features.notes.presentation.editor.NoteEditorScreen
import com.oussama_chatri.productivityx.features.notes.presentation.list.NotesScreen
import com.oussama_chatri.productivityx.features.notes.presentation.trash.TrashScreen
import com.oussama_chatri.productivityx.features.pomodoro.navigation.PomodoroRoute
import com.oussama_chatri.productivityx.features.pomodoro.navigation.pomodoroNavGraph
import com.oussama_chatri.productivityx.features.tasks.navigation.TaskRoutes
import com.oussama_chatri.productivityx.features.tasks.navigation.tasksNavGraph

private data class TabConfig(
    val title: String,
    val description: String = "",
    val fabAdd: Boolean = true,
    val fabAi: Boolean = true,
)

private val tabConfigs = mapOf(
    MainRoute.Home::class.qualifiedName    to TabConfig("Dashboard",  "Overview of your day",         fabAdd = true,  fabAi = false),
    MainRoute.Notes::class.qualifiedName   to TabConfig("Notes",      "Capture your thoughts",         fabAdd = true,  fabAi = true),
    MainRoute.Tasks::class.qualifiedName   to TabConfig("Tasks",      "Get things done",               fabAdd = true,  fabAi = true),
    MainRoute.Calendar::class.qualifiedName to TabConfig("Events",    "Your schedule at a glance",     fabAdd = true,  fabAi = true),
    MainRoute.Pomodoro::class.qualifiedName to TabConfig("Pomodoro",  "Stay in the flow",              fabAdd = false, fabAi = true),
)

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

        composable<MainRoute.Calendar> {
            CalendarTab(rootNavController)
        }

        composable<MainRoute.Ai> {
            AiTab(rootNavController)
        }

        composable<MainRoute.Profile> {
            ProfileTab(rootNavController)
        }

        searchNavGraph(rootNavController)
    }
}

internal fun NavHostController.navigateToTab(route: MainRoute) {
    navigate(route) {
        popUpTo(MainRoute.Home) {
            saveState = true
        }
        launchSingleTop = true
        restoreState    = true
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TabScaffold(
    config: TabConfig,
    onNavigateToProfile: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToAi: () -> Unit,
    onFabAdd: (() -> Unit)? = null,
    bottomNavCurrentRoute: String?,
    onNavItemClick: (MainRoute) -> Unit,
    modifier: Modifier = Modifier,
    showTopBar: Boolean = true,
    showBottomBar: Boolean = true,
    additionalActions: @Composable () -> Unit = {},
    content: @Composable (Modifier) -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (showTopBar) {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text       = config.title,
                            style      = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        if (config.description.isNotBlank()) {
                            Text(
                                text       = config.description,
                                style      = MaterialTheme.typography.bodyMedium,
                                color      = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                actions = {
                    // Screen-specific actions (e.g. trash, filter) inserted here
                    additionalActions?.invoke()
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Outlined.Person, contentDescription = "Profile")
                    }
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(Icons.Outlined.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = { /* TODO: notifications */ }) {
                        Icon(Icons.Outlined.Notifications, contentDescription = "Notifications")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                PxBottomNavBar(
                    currentRoute   = bottomNavCurrentRoute,
                    onNavItemClick = onNavItemClick,
                    modifier       = Modifier.navigationBarsPadding(),
                )
            }
        },
        floatingActionButton = {
            if (config.fabAdd || config.fabAi) {
                FabStack(onFabAdd = onFabAdd, onFabAi = onNavigateToAi, config = config)
            }
        },
    ) { innerPadding ->
        content(Modifier.fillMaxSize().padding(innerPadding))
    }
}

@Composable
private fun FabStack(
    onFabAdd: (() -> Unit)?,
    onFabAi: () -> Unit,
    config: TabConfig,
) {
    if (config.fabAdd && config.fabAi) {
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FloatingActionButton(
                onClick      = onFabAi,
                containerColor = MaterialTheme.colorScheme.primary,
            ) {
                Icon(Icons.Outlined.AutoAwesome, contentDescription = "AI", tint = Color.White)
            }
            if (onFabAdd != null) {
                FloatingActionButton(
                    onClick      = onFabAdd,
                    containerColor = MaterialTheme.colorScheme.primary,
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = "Add", tint = Color.White)
                }
            }
        }
    } else if (config.fabAdd && onFabAdd != null) {
        SingleFab(Icons.Outlined.Add, onClick = onFabAdd)
    } else if (config.fabAi) {
        SingleFab(Icons.Outlined.AutoAwesome, onClick = onFabAi)
    }
}

@Composable
private fun SingleFab(icon: ImageVector, onClick: () -> Unit) {
    FloatingActionButton(
        onClick      = onClick,
        containerColor = MaterialTheme.colorScheme.primary,
    ) {
        Icon(icon, contentDescription = null, tint = Color.White)
    }
}

// Home Tab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTab(rootNavController: NavHostController) {
    val rootBackStack by rootNavController.currentBackStackEntryAsState()
    val config        = tabConfigs[MainRoute.Home::class.qualifiedName] ?: tabConfigs.entries.first().value

    TabScaffold(
        config              = config,
        onNavigateToProfile = { rootNavController.navigate(MainRoute.Profile) },
        onNavigateToSearch  = { rootNavController.navigate(MainRoute.Search) },
        onNavigateToAi      = { rootNavController.navigateToTab(MainRoute.Ai) },
        onFabAdd            = { rootNavController.navigateToTab(MainRoute.Notes) },
        bottomNavCurrentRoute = rootBackStack?.destination?.route,
        onNavItemClick      = { rootNavController.navigateToTab(it) },
    ) { modifier ->
        HomeScreen(
            onNavigateToProfile   = { rootNavController.navigate(MainRoute.Profile) },
            onNavigateToNotes     = { rootNavController.navigateToTab(MainRoute.Notes) },
            onNavigateToTasks     = { rootNavController.navigateToTab(MainRoute.Tasks) },
            onNavigateToCalendar  = { rootNavController.navigateToTab(MainRoute.Calendar) },
            onNavigateToPomodoro  = { rootNavController.navigateToTab(MainRoute.Pomodoro) },
            modifier              = modifier,
        )
    }
}

// Notes Tab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotesTab(rootNavController: NavHostController) {
    val notesNavController = rememberNavController()
    val notesBackStack     by notesNavController.currentBackStackEntryAsState()
    val notesRoute         = notesBackStack?.destination?.route

    val isTopLevel = notesRoute?.contains("NoteEditor") == false &&
            notesRoute?.contains("Trash") == false

    val rootBackStack by rootNavController.currentBackStackEntryAsState()
    val config = tabConfigs[MainRoute.Notes::class.qualifiedName] ?: tabConfigs.entries.first().value

    TabScaffold(
        config              = config,
        onNavigateToProfile = { rootNavController.navigate(MainRoute.Profile) },
        onNavigateToSearch  = { rootNavController.navigate(MainRoute.Search) },
        onNavigateToAi      = { rootNavController.navigateToTab(MainRoute.Ai) },
        onFabAdd            = { notesNavController.navigate(NotesRoute.NoteEditor()) },
        bottomNavCurrentRoute = rootBackStack?.destination?.route,
        onNavItemClick      = { rootNavController.navigateToTab(it) },
        showBottomBar       = isTopLevel,
        additionalActions   = {
            IconButton(onClick = { notesNavController.navigate(NotesRoute.Trash) }) {
                Icon(Icons.Outlined.DeleteOutline, contentDescription = "Trash", tint = Color(0xFFCCCCD8))
            }
        },
    ) { modifier ->
        NavHost(
            navController    = notesNavController,
            startDestination = NotesRoute.NotesList,
            modifier         = modifier,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TasksTab(rootNavController: NavHostController) {
    val tasksNavController = rememberNavController()
    val tasksBackStack     by tasksNavController.currentBackStackEntryAsState()
    val tasksRoute         = tasksBackStack?.destination?.route

    val isTopLevel = tasksRoute == TaskRoutes.TASKS

    val rootBackStack by rootNavController.currentBackStackEntryAsState()
    val config = tabConfigs[MainRoute.Tasks::class.qualifiedName] ?: tabConfigs.entries.first().value

    TabScaffold(
        config              = config,
        onNavigateToProfile = { rootNavController.navigate(MainRoute.Profile) },
        onNavigateToSearch  = { rootNavController.navigate(MainRoute.Search) },
        onNavigateToAi      = { rootNavController.navigateToTab(MainRoute.Ai) },
        onFabAdd            = { /* TODO: open add task sheet */ },
        bottomNavCurrentRoute = rootBackStack?.destination?.route,
        onNavItemClick      = { rootNavController.navigateToTab(it) },
        showBottomBar       = isTopLevel,
    ) { modifier ->
        NavHost(
            navController    = tasksNavController,
            startDestination = TaskRoutes.TASKS,
            modifier         = modifier,
        ) {
            tasksNavGraph(tasksNavController)
        }
    }
}

// Pomodoro Tab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PomodoroTab(rootNavController: NavHostController) {
    val pomodoroNavController = rememberNavController()
    val pomodoroBackStack     by pomodoroNavController.currentBackStackEntryAsState()
    val pomodoroRoute         = pomodoroBackStack?.destination?.route

    val isTopLevel = pomodoroRoute?.contains("History") == false

    val rootBackStack by rootNavController.currentBackStackEntryAsState()
    val config = tabConfigs[MainRoute.Pomodoro::class.qualifiedName] ?: tabConfigs.entries.first().value

    TabScaffold(
        config              = config,
        onNavigateToProfile = { rootNavController.navigate(MainRoute.Profile) },
        onNavigateToSearch  = { rootNavController.navigate(MainRoute.Search) },
        onNavigateToAi      = { rootNavController.navigateToTab(MainRoute.Ai) },
        bottomNavCurrentRoute = rootBackStack?.destination?.route,
        onNavItemClick      = { rootNavController.navigateToTab(it) },
        showTopBar          = isTopLevel,
        showBottomBar       = isTopLevel,
        additionalActions   = {
            IconButton(onClick = { pomodoroNavController.navigate(PomodoroRoute.History) }) {
                Icon(Icons.Outlined.History, contentDescription = "History", tint = Color(0xFFCCCCD8))
            }
        },
    ) { modifier ->
        NavHost(
            navController    = pomodoroNavController,
            startDestination = PomodoroRoute.Timer,
            modifier         = modifier,
        ) {
            pomodoroNavGraph(pomodoroNavController)
        }
    }
}

// Calendar (Events) Tab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarTab(rootNavController: NavHostController) {
    val calendarNavController = rememberNavController()
    val calendarBackStack     by calendarNavController.currentBackStackEntryAsState()
    val calendarRoute         = calendarBackStack?.destination?.route

    val isTopLevel = calendarRoute?.contains("EventDetail") == false

    val rootBackStack by rootNavController.currentBackStackEntryAsState()
    val config = tabConfigs[MainRoute.Calendar::class.qualifiedName] ?: tabConfigs.entries.first().value

    TabScaffold(
        config              = config,
        onNavigateToProfile = { rootNavController.navigate(MainRoute.Profile) },
        onNavigateToSearch  = { rootNavController.navigate(MainRoute.Search) },
        onNavigateToAi      = { rootNavController.navigateToTab(MainRoute.Ai) },
        onFabAdd            = { calendarNavController.navigate(Routes.Calendar(showAddEvent = true)) },
        bottomNavCurrentRoute = rootBackStack?.destination?.route,
        onNavItemClick      = { rootNavController.navigateToTab(it) },
        showBottomBar       = isTopLevel,
    ) { modifier ->
        NavHost(
            navController    = calendarNavController,
            startDestination = Routes.Calendar(),
            modifier         = modifier,
        ) {
            eventsNavGraph(calendarNavController)
        }
    }
}

// AI Tab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AiTab(rootNavController: NavHostController) {
    val aiNavController = rememberNavController()
    val aiBackStack     by aiNavController.currentBackStackEntryAsState()
    val aiRoute         = aiBackStack?.destination?.route

    val isTopLevel = aiRoute?.contains("ConversationList") == false

    val rootBackStack by rootNavController.currentBackStackEntryAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (isTopLevel) {
                PxBottomNavBar(
                    currentRoute   = rootBackStack?.destination?.route,
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

// Profile Tab

@Composable
private fun ProfileTab(rootNavController: NavHostController) {
    val profileNavController = rememberNavController()

    NavHost(
        navController    = profileNavController,
        startDestination = SettingsRoute.Profile,
        modifier         = Modifier.fillMaxSize(),
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
