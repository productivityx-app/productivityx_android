# productivityx-android

**Repository description:** Offline-first native Android client for ProductivityX built with Jetpack Compose. Feature-sliced clean architecture — full auth lifecycle, profile management (first/last name, avatar, preferences), notes with Markdown and tagging, tasks with kanban and subtasks, calendar events with recurrence, Pomodoro foreground service with notifications, persisted AI conversations with SSE streaming, unified search with local FTS4 fallback, and a WorkManager-powered outbox sync engine. Targets API 26+.

---

## Stack

| Layer | Technology | Version |
| --- | --- | --- |
| UI  | Jetpack Compose BOM | **2026.03.00** |
| Navigation | Navigation Compose | **2.9.7** |
| DI  | Hilt | **2.59** |
| DI (Compose) | Hilt Navigation Compose | **1.3.0** |
| Network | Retrofit2 | **2.11.x** |
| HTTP client | OkHttp3 | **4.12.x** |
| JSON | Kotlinx Serialization | **1.10.0** |
| Async | Coroutines + Flow | **1.10.x** |
| Local DB | Room | **2.7.x** |
| Background sync | WorkManager | **2.10.x** |
| Real-time | OkHttp WebSocket | included |
| Images | Coil 3 | **3.x** |
| Secure storage | DataStore Preferences | **1.1.x** |
| Encrypted storage | EncryptedSharedPreferences | **1.1.x** |
| Markdown rendering | Compose-Markdown (Mikepenz) | latest stable |
| Lifecycle | Lifecycle ViewModel Compose | **2.9.x** |
| Build | AGP | **9.0.x** |
| Language | Kotlin | **2.3.20** |
| Min SDK | Android 8.0 | **26** |
| Target SDK | Android 15 | **35** |

> **Coil 3:** Import from `io.coil-kt.coil3:coil-compose`. Use `AsyncImage()` — not the deprecated `rememberImagePainter` from v2.
> **AGP 9:** `org.jetbrains.kotlin.android` is built into AGP 9. Hilt 2.59+ is required for AGP 9 compatibility. Use KSP instead of KAPT for all annotation processing.

---

## Architecture

Clean architecture with unidirectional data flow and feature-sliced modules. **Local DB is the single source of truth.** The UI never reads directly from the network.

```
Composable / Screen
    ↕ StateFlow / collectAsStateWithLifecycle
ViewModel  →  UiState sealed class (Loading | Success<T> | Error)
    ↕ suspend / Flow
UseCase  (single public operator fun, one responsibility)
    ↕
Repository interface  (domain layer)
    ↕                       ↕
RemoteDataSource           LocalDataSource
(Retrofit)                 (Room)
         ↑
    SyncWorker drains sync_queue, posts to remote, updates sync_status
```

No cross-feature imports. Features share only domain models declared in `core`.

---

## Offline-First Contract

Every write in `RepositoryImpl` follows this exact pattern:

```kotlin
suspend fun createNote(note: Note): Note {
    val entity = note.toEntity().copy(
        syncStatus = PENDING,
        pendingOperation = CREATE
    )
    noteDao.insert(entity)
    syncQueueDao.insert(
        SyncQueueEntity(entityType = "NOTE", entityId = note.id, operation = CREATE, payload = note.toJson())
    )
    syncWorker.triggerImmediate()
    return note // UI updates from the Room Flow immediately
}
```

`SyncWorker` runs on `Constraints(NetworkType.CONNECTED)`. It drains the outbox periodically (15 min) and reactively (on network reconnect). On conflicts, `ConflictResolver` applies last-write-wins by `updatedAt`. Unresolvable conflicts are surfaced to the user.

---

## Auth Screens

| Screen | Route | Notes |
| --- | --- | --- |
| Splash | `splash` | Checks token validity, routes to onboarding or home |
| Onboarding | `onboarding` | Shown once on first install, skippable |
| Login | `auth/login` |     |
| Register | `auth/register` | Multi-step: identifier → personal info → password → verify |
| Verify email | `auth/verify-email` | Deep link target from email, 6-digit OTP |
| Forgot password | `auth/forgot-password` |     |
| Reset password | `auth/reset-password` | Deep link target from email |

---

## Key Behaviours

**Silent token refresh:** `TokenRefreshInterceptor` (OkHttp `Authenticator`) catches 401, calls `/auth/refresh` synchronously, updates `TokenStorage`, retries the original request. Completely transparent to all call sites.

**Auto-save notes:** `NoteEditorViewModel` debounces content changes 1.5s before dispatching `UpdateNoteUseCase`. Local Room entity updates immediately; outbox handles remote sync asynchronously.

**Pomodoro foreground service:** Timer state lives in `PomodoroForegroundService` as a `StateFlow<TimerState>`. The UI composable binds to this flow via `ServiceConnection`. On session end, a completion notification fires on a dedicated `NotificationChannel`. Persistent notification exposes Play/Pause/Skip actions via `PendingIntent`.

**AI streaming:** SSE tokens collected from `EventSource` and appended to a `StateFlow<String>` in `AiViewModel`. Chat bubble reads this flow and re-renders incrementally as each token arrives. On stream completion, full message is persisted locally and synced.

**Tasks swipe gestures:** `SwipeToDismissBox` — swipe right reveals green complete action, swipe left reveals red delete action. Both trigger their respective use cases.

**Sync status indicator:** Small dot in the top bar reflects `PENDING` (amber), `SYNCING` (blue pulse), `SYNCED` (green), `CONFLICT` (red). Tapping it opens a sync status sheet.

**Deep links:** Verify email and reset password flows are reachable via `productivityx://auth/verify-email?token=<token>` and `productivityx://auth/reset-password?token=<token>`. Declared in `AndroidManifest.xml`.

---

## Design System

Defined in `core/ui/theme/`. Identical tokens to the Desktop client — consistency enforced at the token level.

| Token | Value |
| --- | --- |
| Primary | `#6366F1` (Indigo-500) |
| Secondary | `#8B5CF6` (Violet-500) |
| Background | `#0F0F14` |
| Surface | `#1A1A24` |
| Surface Variant | `#252533` |
| Error | `#EF4444` |
| Success | `#22C55E` |
| Warning | `#F59E0B` |
| Corner radius — cards | 12dp |
| Corner radius — buttons | 8dp |
| Font | Nunito (Regular, Medium, SemiBold, Bold) |
| Min touch target | 48×48dp |

---

## Configuration

Add to `local.properties` (excluded from git):

```properties
BASE_URL=https://your-railway-backend.up.railway.app/
WS_URL=wss://your-railway-backend.up.railway.app/ws
```

Inject via `buildConfigField` in `app/build.gradle.kts`:

```kotlin
val localProps = Properties().apply {
    load(rootProject.file("local.properties").inputStream())
}

android {
    buildFeatures { buildConfig = true }
    defaultConfig {
        buildConfigField("String", "BASE_URL", "\"${localProps["BASE_URL"]}\"")
        buildConfigField("String", "WS_URL",   "\"${localProps["WS_URL"]}\"")
    }
}
```

---

## Building

**Debug APK:**

```bash
./gradlew assembleDebug
```

**Release APK:**

Configure signing in `app/build.gradle.kts`:

```kotlin
signingConfigs {
    create("release") {
        storeFile     = file(localProps["KEYSTORE_PATH"] as String)
        storePassword = localProps["KEYSTORE_PASSWORD"] as String
        keyAlias      = localProps["KEY_ALIAS"] as String
        keyPassword   = localProps["KEY_PASSWORD"] as String
    }
}
```

```bash
./gradlew assembleRelease
```

Output: `app/build/outputs/apk/release/app-release.apk`

---

## Dependencies — `gradle/libs.versions.toml`

```toml
[versions]
kotlin = "2.3.20"
agp = "9.0.0"
compose-bom = "2026.03.00"
navigation-compose = "2.9.7"
hilt = "2.59"
hilt-navigation-compose = "1.3.0"
retrofit = "2.11.0"
okhttp = "4.12.0"
kotlinx-serialization = "1.10.0"
kotlinx-coroutines = "1.10.0"
room = "2.7.0"
workmanager = "2.10.0"
coil = "3.1.0"
datastore = "1.1.2"
security-crypto = "1.1.0-alpha06"
compose-markdown = "0.5.7"
lifecycle = "2.9.0"
ksp = "2.3.20-1.0.32"

[libraries]
# Compose
compose-bom                 = { module = "androidx.compose:compose-bom",              version.ref = "compose-bom" }
compose-ui                  = { module = "androidx.compose.ui:ui" }
compose-ui-graphics         = { module = "androidx.compose.ui:ui-graphics" }
compose-ui-tooling          = { module = "androidx.compose.ui:ui-tooling" }
compose-ui-tooling-preview  = { module = "androidx.compose.ui:ui-tooling-preview" }
compose-ui-test-junit4      = { module = "androidx.compose.ui:ui-test-junit4" }
compose-ui-test-manifest    = { module = "androidx.compose.ui:ui-test-manifest" }
compose-material3           = { module = "androidx.compose.material3:material3" }
compose-material-icons      = { module = "androidx.compose.material:material-icons-extended" }
compose-animation           = { module = "androidx.compose.animation:animation" }
compose-foundation          = { module = "androidx.compose.foundation:foundation" }
activity-compose            = { module = "androidx.activity:activity-compose" }
navigation-compose          = { module = "androidx.navigation:navigation-compose",    version.ref = "navigation-compose" }

# DI — Hilt
hilt-android                = { module = "com.google.dagger:hilt-android",            version.ref = "hilt" }
hilt-compiler               = { module = "com.google.dagger:hilt-android-compiler",   version.ref = "hilt" }
hilt-navigation-compose     = { module = "androidx.hilt:hilt-navigation-compose",    version.ref = "hilt-navigation-compose" }
hilt-work                   = { module = "androidx.hilt:hilt-work",                   version = "1.3.0" }
hilt-work-compiler          = { module = "androidx.hilt:hilt-compiler",               version = "1.3.0" }

# Networking
retrofit                    = { module = "com.squareup.retrofit2:retrofit",            version.ref = "retrofit" }
retrofit-converter-gson     = { module = "com.squareup.retrofit2:converter-gson",     version.ref = "retrofit" }
okhttp                      = { module = "com.squareup.okhttp3:okhttp",               version.ref = "okhttp" }
okhttp-logging              = { module = "com.squareup.okhttp3:logging-interceptor",  version.ref = "okhttp" }
okhttp-sse                  = { module = "com.squareup.okhttp3:okhttp-sse",           version.ref = "okhttp" }

# Serialization & Async
kotlinx-serialization-json  = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "kotlinx-serialization" }
kotlinx-coroutines-android  = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-android",  version.ref = "kotlinx-coroutines" }
kotlinx-coroutines-test     = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test",     version.ref = "kotlinx-coroutines" }

# Room
room-runtime                = { module = "androidx.room:room-runtime",  version.ref = "room" }
room-compiler               = { module = "androidx.room:room-compiler", version.ref = "room" }
room-ktx                    = { module = "androidx.room:room-ktx",      version.ref = "room" }
room-testing                = { module = "androidx.room:room-testing",  version.ref = "room" }

# WorkManager
workmanager                 = { module = "androidx.work:work-runtime-ktx", version.ref = "workmanager" }

# Images
coil-compose                = { module = "io.coil-kt.coil3:coil-compose",         version.ref = "coil" }
coil-network-okhttp         = { module = "io.coil-kt.coil3:coil-network-okhttp",  version.ref = "coil" }

# Storage
datastore-preferences       = { module = "androidx.datastore:datastore-preferences", version.ref = "datastore" }
security-crypto             = { module = "androidx.security:security-crypto",        version.ref = "security-crypto" }

# Markdown
compose-markdown            = { module = "com.mikepenz:multiplatform-markdown-renderer-m3", version.ref = "compose-markdown" }

# Lifecycle
lifecycle-viewmodel-compose = { module = "androidx.lifecycle:lifecycle-viewmodel-compose", version.ref = "lifecycle" }
lifecycle-runtime-compose   = { module = "androidx.lifecycle:lifecycle-runtime-compose",   version.ref = "lifecycle" }
lifecycle-runtime-ktx       = { module = "androidx.lifecycle:lifecycle-runtime-ktx",       version.ref = "lifecycle" }

# Core
core-ktx                    = { module = "androidx.core:core-ktx",         version = "1.16.0" }
splashscreen                = { module = "androidx.core:core-splashscreen", version = "1.0.1" }

# Testing
junit                       = { module = "junit:junit",                         version = "4.13.2" }
junit-android               = { module = "androidx.test.ext:junit",             version = "1.2.1" }
espresso                    = { module = "androidx.test.espresso:espresso-core", version = "3.6.1" }
mockk                       = { module = "io.mockk:mockk",                      version = "1.13.13" }

[plugins]
android-application  = { id = "com.android.application",                        version.ref = "agp" }
kotlin-android       = { id = "org.jetbrains.kotlin.android",                    version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization",       version.ref = "kotlin" }
kotlin-compose       = { id = "org.jetbrains.kotlin.plugin.compose",             version.ref = "kotlin" }
hilt                 = { id = "com.google.dagger.hilt.android",                  version.ref = "hilt" }
ksp                  = { id = "com.google.devtools.ksp",                         version.ref = "ksp" }
```

---

## Testing

```bash
# Unit tests (JVM, fast)
./gradlew test

# Instrumented tests (device or emulator required)
./gradlew connectedAndroidTest
```

ViewModels tested with `kotlinx-coroutines-test` and MockK. Repository tests use an in-memory Room database. UI tests use Compose's `ComposeTestRule`.