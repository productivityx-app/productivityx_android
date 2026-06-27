package com.oussama_chatri.productivityx

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
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

    private var navController: NavHostController? = null

    override fun attachBaseContext(newBase: Context) {
        val lang = runBlocking {
            runCatching {
                newBase.applicationContext.dataStore.data.first()[stringPreferencesKey("language")] ?: "en"
            }.getOrDefault("en")
        }
        val locale = Locale.Builder().setLanguage(lang).build()
        Locale.setDefault(locale)
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        lifecycleScope.launch {
            prefs.language.collect { newLang ->
                if (newLang != Locale.getDefault().language) {
                    startActivity(Intent(this@MainActivity, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                    finish()
                }
            }
        }

        setContent {
            val themeName by prefs.appTheme.collectAsStateWithLifecycle(initialValue = "DARK")
            val appTheme = try { AppTheme.valueOf(themeName) } catch (_: Exception) { AppTheme.DARK }
            ProductivityXTheme(appTheme = appTheme) {
                AppNavGraph(
                    onNavControllerReady = { navController = it },
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                )
            }
        }
        intent?.let { handleDeepLink(it) }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent) {
        val uri = intent.data ?: return
        val path = uri.path ?: return
        val token = uri.getQueryParameter("token")
        val email = uri.getQueryParameter("email")

        navController?.let { nav ->
            when {
                path.contains("verify-email") && token != null -> {
                    nav.navigate(AuthRoute.VerifyEmail(email ?: ""))
                }
                path.contains("reset-password") && token != null -> {
                    nav.navigate(AuthRoute.ResetPassword(token))
                }
            }
        }
    }
}