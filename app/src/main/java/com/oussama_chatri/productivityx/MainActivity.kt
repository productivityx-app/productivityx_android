package com.oussama_chatri.productivityx

import android.app.LocaleManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Build
import android.os.LocaleList
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import com.oussama_chatri.productivityx.core.enums.AppTheme
import com.oussama_chatri.productivityx.core.storage.PreferencesDataStore
import com.oussama_chatri.productivityx.core.storage.dataStore
import com.oussama_chatri.productivityx.core.ui.navigation.AppNavGraph
import com.oussama_chatri.productivityx.core.ui.navigation.AuthRoute
import com.oussama_chatri.productivityx.core.ui.navigation.MainRoute
import com.oussama_chatri.productivityx.core.ui.navigation.Routes
import com.oussama_chatri.productivityx.core.ui.notifications.InAppNotificationToast
import com.oussama_chatri.productivityx.core.ui.notifications.LocalNotificationState
import com.oussama_chatri.productivityx.core.ui.notifications.NotificationCenter
import com.oussama_chatri.productivityx.core.ui.notifications.NotificationState
import com.oussama_chatri.productivityx.core.ui.components.ErrorBoundary
import com.oussama_chatri.productivityx.core.ui.components.OfflineIndicator
import com.oussama_chatri.productivityx.core.ui.theme.PxColors
import com.oussama_chatri.productivityx.core.ui.theme.ProductivityXTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var prefs: PreferencesDataStore
    @Inject lateinit var tokenStorage: com.oussama_chatri.productivityx.core.storage.TokenStorage

    private var navController: NavHostController? = null

    override fun attachBaseContext(newBase: Context) {
        val lang = runBlocking {
            runCatching {
                newBase.applicationContext.dataStore.data.first()[stringPreferencesKey("language")] ?: "en"
            }.getOrDefault("en")
        }
        val locale = if (lang.contains("-")) Locale.forLanguageTag(lang) else Locale(lang)
        Locale.setDefault(locale)
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT)
        )

        lifecycleScope.launch {
            prefs.language.collect { newLang ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val localeManager = getSystemService(LocaleManager::class.java)
                    localeManager.applicationLocales = LocaleList.forLanguageTags(newLang)
                } else if (newLang != Locale.getDefault().language) {
                    startActivity(Intent(this@MainActivity, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                    finish()
                }
            }
        }

        lifecycleScope.launch {
            tokenStorage.sessionExpiredEvents.collect {
                navController?.let { nav ->
                    try {
                        nav.navigate(AuthRoute.Login) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    } catch (_: Exception) { }
                }
            }
        }

        setContent {
            val themeName by prefs.appTheme.collectAsStateWithLifecycle(initialValue = "DARK")
            val appTheme = try { AppTheme.valueOf(themeName) } catch (_: Exception) { AppTheme.DARK }
            val notificationState = remember { NotificationState() }

            ProductivityXTheme(appTheme = appTheme) {
                CompositionLocalProvider(LocalNotificationState provides notificationState) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(PxColors.Background),
                    ) {
                        AppNavGraph(
                            onNavControllerReady = { navController = it },
                            modifier = Modifier.fillMaxSize(),
                        )

                        OfflineIndicator(modifier = Modifier.align(Alignment.TopCenter))

                        InAppNotificationToast(
                            notification = notificationState.currentToast,
                            onDismiss = { notificationState.dismissToast() },
                            onClick = { notif ->
                                notificationState.dismissToast()
                                notificationState.markRead(notif.id)
                                handleNotificationClick(notif, navController)
                            },
                            modifier = Modifier.align(Alignment.TopCenter),
                        )

                        NotificationCenter(
                            visible = notificationState.showNotificationCenter,
                            notifications = notificationState.notifications.toList(),
                            onDismiss = { notificationState.showNotificationCenter = false },
                            onMarkAllRead = { notificationState.markAllRead() },
                            onNotificationClick = { notif ->
                                notificationState.showNotificationCenter = false
                                notificationState.markRead(notif.id)
                                handleNotificationClick(notif, navController)
                            },
                        )
                    }
                }
            }
        }
        intent?.let { handleIntent(it, navController) }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent, navController)
    }

    private fun handleIntent(intent: Intent, nav: NavHostController?) {
        if (intent.hasExtra("open_pomodoro") && intent.getBooleanExtra("open_pomodoro", false)) {
            nav?.navigate(MainRoute.Pomodoro)
            return
        }
        if (intent.hasExtra("quick_note") && intent.getBooleanExtra("quick_note", false)) {
            nav?.navigate(MainRoute.Notes)
            return
        }
        if (intent.hasExtra("toggle_focus_mode") && intent.getBooleanExtra("toggle_focus_mode", false)) {
            nav?.navigate(MainRoute.Home)
            return
        }
        if (intent.hasExtra("voice_command")) {
            val command = intent.getStringExtra("voice_command") ?: ""
            val action = com.oussama_chatri.productivityx.core.ui.voice.VoiceCommandHandler.parseCommand(command)
            if (action != null) {
                val route = com.oussama_chatri.productivityx.core.ui.voice.VoiceCommandHandler.getNavigationRoute(action)
                nav?.navigate(route)
            } else {
                nav?.navigate(MainRoute.Home)
            }
            return
        }
        intent.data?.let { handleDeepLink(it, nav) }
    }

    private fun handleDeepLink(uri: Uri, nav: NavHostController?) {
        val uri = intent.data ?: return
        val path = uri.path ?: return
        val token = uri.getQueryParameter("token")
        val email = uri.getQueryParameter("email")
        val noteId = uri.getQueryParameter("noteId")
        val taskId = uri.getQueryParameter("taskId")
        val eventId = uri.getQueryParameter("eventId")

        nav?.let { controller ->
            when {
                path.contains("verify-email") && token != null -> {
                    controller.navigate(AuthRoute.VerifyEmail(email ?: ""))
                }
                path.contains("reset-password") && token != null -> {
                    controller.navigate(AuthRoute.ResetPassword(token))
                }
                path.contains("notes") && noteId != null -> {
                    controller.navigate(Routes.NoteEditor(noteId))
                }
                path.contains("tasks") && taskId != null -> {
                    controller.navigate(Routes.TaskDetail(taskId))
                }
                path.contains("events") && eventId != null -> {
                    controller.navigate(Routes.EventDetail(eventId))
                }
                path.contains("home") -> {
                    controller.navigate(MainRoute.Home)
                }
                path.contains("ai") -> {
                    controller.navigate(MainRoute.Ai)
                }
                path.contains("profile") -> {
                    controller.navigate(MainRoute.Profile)
                }
            }
        }
    }

    companion object {
        fun handleNotificationClick(
            notification: com.oussama_chatri.productivityx.core.ui.notifications.InAppNotification,
            navController: NavHostController?,
        ) {
            val deepLink = notification.deepLink
            if (deepLink != null && navController != null) {
                try {
                    val uri = Uri.parse(deepLink)
                    val path = uri.path ?: return
                    val noteId = uri.getQueryParameter("noteId")
                    val taskId = uri.getQueryParameter("taskId")
                    val eventId = uri.getQueryParameter("eventId")
                    when {
                        path.contains("notes") && noteId != null ->
                            navController.navigate(Routes.NoteEditor(noteId))
                        path.contains("tasks") && taskId != null ->
                            navController.navigate(Routes.TaskDetail(taskId))
                        path.contains("events") && eventId != null ->
                            navController.navigate(Routes.EventDetail(eventId))
                        path.contains("home") -> navController.navigate(MainRoute.Home)
                        path.contains("ai") -> navController.navigate(MainRoute.Ai)
                        path.contains("profile") -> navController.navigate(MainRoute.Profile)
                    }
                } catch (_: Exception) { }
            }
        }
    }
}