package com.oussama_chatri.productivityx.features.settings.presentation.preferences

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.oussama_chatri.productivityx.core.ui.theme.PxColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicensesScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        containerColor = PxColors.Background,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = { Text("Open Source Licenses") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PxColors.Background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "ProductivityX uses the following open source libraries:",
                style = MaterialTheme.typography.bodyMedium,
                color = PxColors.OnSurfaceDim
            )
            Spacer(Modifier.height(16.dp))

            LicenseItem("AndroidX", "Apache 2.0", "https://developer.android.com/jetpack")
            LicenseItem("Jetpack Compose", "Apache 2.0", "https://developer.android.com/jetpack/compose")
            LicenseItem("Material 3", "Apache 2.0", "https://m3.material.io")
            LicenseItem("Dagger Hilt", "Apache 2.0", "https://dagger.dev/hilt")
            LicenseItem("Retrofit", "Apache 2.0", "https://square.github.io/retrofit")
            LicenseItem("Moshi", "Apache 2.0", "https://github.com/square/moshi")
            LicenseItem("OkHttp", "Apache 2.0", "https://square.github.io/okhttp")
            LicenseItem("Kotlinx Serialization", "Apache 2.0", "https://kotlinlang.org/docs/serialization.html")
            LicenseItem("Room Database", "Apache 2.0", "https://developer.android.com/training/data-storage/room")
            LicenseItem("Kotlin Coroutines", "Apache 2.0", "https://kotlinlang.org/docs/coroutines-overview.html")

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun LicenseItem(name: String, license: String, url: String) {
    Column(modifier = Modifier.padding(vertical = 10.dp)) {
        Text(
            text = name,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = PxColors.OnBackground
        )
        Text(
            text = "$license — $url",
            style = MaterialTheme.typography.bodySmall,
            color = PxColors.OnSurfaceDim
        )
    }
    HorizontalDivider(color = PxColors.Outline.copy(alpha = 0.3f))
}
