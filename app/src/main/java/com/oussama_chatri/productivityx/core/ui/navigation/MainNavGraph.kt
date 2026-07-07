package com.oussama_chatri.productivityx.core.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.oussama_chatri.productivityx.core.ui.theme.Breakpoints
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.oussama_chatri.productivityx.R
import com.oussama_chatri.productivityx.core.ui.components.PxBottomNavBar
import com.oussama_chatri.productivityx.core.ui.notifications.LocalNotificationState
import com.oussama_chatri.productivityx.core.ui.theme.ProductivityXTheme
import com.oussama_chatri.productivityx.core.ui.theme.PxColors
import com.oussama_chatri.productivityx.features.ai.presentation.navigation.aiNavGraph
import com.oussama_chatri.productivityx.features.events.presentation.navigation.eventsNavGraph
import com.oussama_chatri.productivityx.features.home.presentation.HomeScreen
import com.oussama_chatri.productivityx.features.notes.presentation.NotesRoute
import com.oussama_chatri.productivityx.features.search.navigation.searchNavGraph
import com.oussama_chatri.productivityx.features.events.presentation.screen.EventDetailScreen
import com.oussama_chatri.productivityx.features.notes.presentation.editor.NoteEditorScreen
import com.oussama_chatri.productivityx.features.notes.presentation.list.NotesScreen
import com.oussama_chatri.productivityx.features.tasks.presentation.screens.TaskDetailScreen
import com.oussama_chatri.productivityx.features.notes.presentation.trash.TrashScreen
import com.oussama_chatri.productivityx.features.pomodoro.navigation.PomodoroRoute
import com.oussama_chatri.productivityx.features.pomodoro.navigation.pomodoroNavGraph
import com.oussama_chatri.productivityx.features.tasks.navigation.TaskRoutes
import com.oussama_chatri.productivityx.features.tasks.navigation.tasksNavGraph

private data class TabConfig(
    val titleRes: Int,
    val descriptionRes: Int? = null,
    val fabAdd: Boolean = true,
    val fabAi: Boolean = true,
)

private val tabConfigs = mapOf(
    MainRoute.Home::class.qualifiedName    to TabConfig(R.string.tab_dashboard,  R.string.tab_dashboard_desc,       fabAdd = true,  fabAi = false),
    MainRoute.Notes::class.qualifiedName   to TabConfig(R.string.nav_notes,      R.string.tab_notes_desc,          fabAdd = true,  fabAi = true),
    MainRoute.Tasks::class.qualifiedName   to TabConfig(R.string.nav_tasks,      R.string.tab_tasks_desc,          fabAdd = true,  fabAi = true),
    MainRoute.Calendar::class.qualifiedName to TabConfig(R.string.tab_events,    R.string.tab_events_desc,         fabAdd = true,  fabAi = true),
    MainRoute.Pomodoro::class.qualifiedName to TabConfig(R.string.nav_pomodoro,  R.string.tab_pomodoro_desc,       fabAdd = false, fabAi = true),
)

fun NavGraphBuilder.mainNavGraph(rootNavController: NavHostController) {
    navigation<MainGraph>(startDestination = MainRoute.Home) {

        composable<MainRoute.Home>(
            enterTransition = { fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) + scaleIn(spring(stiffness = Spring.StiffnessMediumLow), initialScale = 0.92f) },
            exitTransition = { fadeOut(spring(stiffness = Spring.StiffnessMediumLow)) },
        ) {
            HomeTab(rootNavController)
        }

        composable<MainRoute.Notes>(
            enterTransition = { fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) + scaleIn(spring(stiffness = Spring.StiffnessMediumLow), initialScale = 0.92f) },
            exitTransition = { fadeOut(spring(stiffness = Spring.StiffnessMediumLow)) },
        ) {
            NotesTab(rootNavController)
        }

        composable<MainRoute.Tasks>(
            enterTransition = { fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) + scaleIn(spring(stiffness = Spring.StiffnessMediumLow), initialScale = 0.92f) },
            exitTransition = { fadeOut(spring(stiffness = Spring.StiffnessMediumLow)) },
        ) {
            TasksTab(rootNavController)
        }

        composable<MainRoute.Pomodoro>(
            enterTransition = { fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) + scaleIn(spring(stiffness = Spring.StiffnessMediumLow), initialScale = 0.92f) },
            exitTransition = { fadeOut(spring(stiffness = Spring.StiffnessMediumLow)) },
        ) {
            PomodoroTab(rootNavController)
        }

        composable<MainRoute.Calendar>(
            enterTransition = { fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) + scaleIn(spring(stiffness = Spring.StiffnessMediumLow), initialScale = 0.92f) },
            exitTransition = { fadeOut(spring(stiffness = Spring.StiffnessMediumLow)) },
        ) {
            CalendarTab(rootNavController)
        }

        composable<MainRoute.Ai>(
            enterTransition = { fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) + scaleIn(spring(stiffness = Spring.StiffnessMediumLow), initialScale = 0.92f) },
            exitTransition = { fadeOut(spring(stiffness = Spring.StiffnessMediumLow)) },
        ) {
            AiTab(rootNavController)
        }

        composable<MainRoute.Profile>(
            enterTransition = { fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) + scaleIn(spring(stiffness = Spring.StiffnessMediumLow), initialScale = 0.92f) },
            exitTransition = { fadeOut(spring(stiffness = Spring.StiffnessMediumLow)) },
        ) {
            ProfileTab(rootNavController)
        }

        composable<Routes.NoteEditor>(
            enterTransition = {
                slideInHorizontally(tween(300)) { it / 4 } + fadeIn(tween(300))
            },
            exitTransition = {
                slideOutHorizontally(tween(250)) { -it / 3 } + fadeOut(tween(250))
            },
            popEnterTransition = {
                slideInHorizontally(tween(250)) { -it / 3 } + fadeIn(tween(250))
            },
            popExitTransition = {
                slideOutHorizontally(tween(300)) { it / 4 } + fadeOut(tween(300))
            },
        ) { entry ->
            val route = entry.toRoute<Routes.NoteEditor>()
            NoteEditorScreen(
                noteId         = route.noteId,
                onNavigateBack = { rootNavController.popBackStack() },
            )
        }

        composable<Routes.TaskDetail>(
            enterTransition = {
                slideInHorizontally(tween(300)) { it / 4 } + fadeIn(tween(300))
            },
            exitTransition = {
                slideOutHorizontally(tween(250)) { -it / 3 } + fadeOut(tween(250))
            },
            popEnterTransition = {
                slideInHorizontally(tween(250)) { -it / 3 } + fadeIn(tween(250))
            },
            popExitTransition = {
                slideOutHorizontally(tween(300)) { it / 4 } + fadeOut(tween(300))
            },
        ) { entry ->
            val route = entry.toRoute<Routes.TaskDetail>()
            TaskDetailScreen(
                taskId         = route.taskId,
                onNavigateBack = { rootNavController.popBackStack() },
                onEditTask     = { rootNavController.popBackStack() },
            )
        }

        composable<Routes.EventDetail>(
            enterTransition = {
                slideInHorizontally(tween(300)) { it / 4 } + fadeIn(tween(300))
            },
            exitTransition = {
                slideOutHorizontally(tween(250)) { -it / 3 } + fadeOut(tween(250))
            },
            popEnterTransition = {
                slideInHorizontally(tween(250)) { -it / 3 } + fadeIn(tween(250))
            },
            popExitTransition = {
                slideOutHorizontally(tween(300)) { it / 4 } + fadeOut(tween(300))
            },
        ) { entry ->
            val route = entry.toRoute<Routes.EventDetail>()
            EventDetailScreen(
                eventId = route.eventId,
                onBack  = { rootNavController.popBackStack() },
                onEdit  = { rootNavController.popBackStack() },
            )
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
    val configuration = LocalConfiguration.current
    val windowSizeClass = remember(configuration.screenWidthDp) {
        Breakpoints.classify(configuration.screenWidthDp.dp)
    }
    val useNavRail = windowSizeClass != Breakpoints.WindowSizeClass.Compact

    Row(modifier = Modifier.fillMaxSize()) {
        if (useNavRail && showBottomBar) {
            PxNavRail(
                currentRoute = bottomNavCurrentRoute,
                onNavItemClick = onNavItemClick
            )
        }
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            modifier = Modifier.weight(1f),
            topBar = {
                if (showTopBar) {
                    Column {
                        TopAppBar(
                            title = {
                                Column {
                                    Text(
                                        text = stringResource(config.titleRes),
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    if (config.descriptionRes != null) {
                                        Text(
                                            text = stringResource(config.descriptionRes),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            },
                            actions = {
                                additionalActions?.invoke()
                                IconButton(onClick = onNavigateToProfile) {
                                    Icon(Icons.Outlined.Person, contentDescription = stringResource(R.string.nav_profile))
                                }
                                IconButton(onClick = onNavigateToSearch) {
                                    Icon(Icons.Outlined.Search, contentDescription = stringResource(R.string.cd_search))
                                }
                                val notificationState = LocalNotificationState.current
                                val badgeCount = notificationState.unreadCount
                                IconButton(onClick = { notificationState.showNotificationCenter = !notificationState.showNotificationCenter }) {
                                    Box {
                                        Icon(Icons.Outlined.Notifications, contentDescription = stringResource(R.string.cd_notifications))
                                        if (badgeCount > 0) {
                                            Box(
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .clip(CircleShape)
                                                    .background(PxColors.Error)
                                                    .align(Alignment.TopEnd),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                Text(
                                                    text = if (badgeCount > 9) "9+" else badgeCount.toString(),
                                                    fontSize = 9.sp,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    textAlign = TextAlign.Center,
                                                )
                                            }
                                        }
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = PxColors.Surface.copy(alpha = 0.45f),
                                titleContentColor = MaterialTheme.colorScheme.onBackground,
                            ),
                        )
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = PxColors.Outline.copy(alpha = 0.25f),
                        )
                    }
                }
            },
            bottomBar = {
                if (showBottomBar && !useNavRail) {
                    PxBottomNavBar(
                        currentRoute = bottomNavCurrentRoute,
                        onNavItemClick = onNavItemClick,
                        modifier = Modifier.navigationBarsPadding(),
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
}

@Composable
private fun PxNavRail(
    currentRoute: String?,
    onNavItemClick: (MainRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationRail(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        header = {
            Icon(
                imageVector = Icons.Outlined.AutoAwesome,
                contentDescription = null,
                tint = PxColors.Primary,
                modifier = Modifier.padding(vertical = 24.dp).size(32.dp)
            )
        }
    ) {
        val tabs = listOf(
            MainRoute.Home,
            MainRoute.Notes,
            MainRoute.Tasks,
            MainRoute.Calendar,
            MainRoute.Pomodoro
        )

        tabs.forEach { route ->
            val label = when (route) {
                MainRoute.Home -> "Home"
                MainRoute.Notes -> "Notes"
                MainRoute.Tasks -> "Tasks"
                MainRoute.Calendar -> "Calendar"
                MainRoute.Pomodoro -> "Focus"
                else -> ""
            }
            val icon = when (route) {
                MainRoute.Home -> Icons.Outlined.Person // Should use proper icons from PxBottomNavBar but demonstrating
                MainRoute.Notes -> Icons.Outlined.Add
                MainRoute.Tasks -> Icons.Outlined.CheckCircle
                MainRoute.Calendar -> Icons.Outlined.CalendarMonth
                MainRoute.Pomodoro -> Icons.Outlined.Notifications
                else -> Icons.Outlined.AutoAwesome
            }

            NavigationRailItem(
                selected = currentRoute?.contains(route::class.simpleName ?: "") == true,
                onClick = { onNavItemClick(route) },
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label) }
            )
        }
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
                Icon(Icons.Outlined.AutoAwesome, contentDescription = stringResource(R.string.nav_ai), tint = Color.White)
            }
            if (onFabAdd != null) {
                FloatingActionButton(
                    onClick      = onFabAdd,
                    containerColor = MaterialTheme.colorScheme.primary,
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.cd_add), tint = Color.White)
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
            onNavigateToAi        = { rootNavController.navigateToTab(MainRoute.Ai) },
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
                Icon(Icons.Outlined.DeleteOutline, contentDescription = stringResource(R.string.nav_trash), tint = Color(0xFFCCCCD8))
            }
        },
    ) { modifier ->
        NavHost(
            navController    = notesNavController,
            startDestination = NotesRoute.NotesList,
            modifier         = modifier,
        ) {
            composable<NotesRoute.NotesList>(
                enterTransition = { fadeIn(tween(200)) },
                popExitTransition = { fadeOut(tween(200)) },
            ) {
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

            composable<NotesRoute.NoteEditor>(
                enterTransition = {
                    slideInHorizontally(tween(300)) { it / 4 } + fadeIn(tween(300))
                },
                popExitTransition = {
                    slideOutHorizontally(tween(300)) { it / 4 } + fadeOut(tween(300))
                },
            ) { entry ->
                val route = entry.toRoute<NotesRoute.NoteEditor>()
                NoteEditorScreen(
                    noteId         = route.noteId,
                    onNavigateBack = { notesNavController.navigateUp() },
                )
            }

            composable<NotesRoute.Trash>(
                enterTransition = {
                    slideInVertically(tween(250)) { it } + fadeIn(tween(250))
                },
                popExitTransition = {
                    slideOutVertically(tween(250)) { it } + fadeOut(tween(250))
                },
            ) {
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
        onFabAdd            = { tasksNavController.navigate(TaskRoutes.ADD_TASK) },
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
                Icon(Icons.Outlined.History, contentDescription = stringResource(R.string.nav_history), tint = Color(0xFFCCCCD8))
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
