package com.oussama_chatri.productivityx.features.pomodoro.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.oussama_chatri.productivityx.core.enums.PomodoroType
import com.oussama_chatri.productivityx.features.pomodoro.domain.model.TimerState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PomodoroForegroundService : Service() {

    companion object {
        const val ACTION_START    = "com.productivityx.pomodoro.START"
        const val ACTION_PAUSE    = "com.productivityx.pomodoro.PAUSE"
        const val ACTION_RESUME   = "com.productivityx.pomodoro.RESUME"
        const val ACTION_SKIP     = "com.productivityx.pomodoro.SKIP"
        const val ACTION_STOP     = "com.productivityx.pomodoro.STOP"

        const val EXTRA_SESSION_ID    = "sessionId"
        const val EXTRA_TYPE          = "type"
        const val EXTRA_TOTAL_SECONDS = "totalSeconds"
        const val EXTRA_TASK_ID       = "taskId"
        const val EXTRA_TASK_TITLE    = "taskTitle"
        const val EXTRA_CYCLE_INDEX   = "cycleIndex"

        private const val NOTIFICATION_ID  = 7001
        private const val CHANNEL_ID       = "pomodoro_timer"
        private const val CHANNEL_NAME     = "Pomodoro Timer"

        fun startIntent(
            context: Context,
            sessionId: String,
            type: PomodoroType,
            totalSeconds: Int,
            taskId: String?,
            taskTitle: String?,
            cycleIndex: Int
        ) = Intent(context, PomodoroForegroundService::class.java).apply {
            action = ACTION_START
            putExtra(EXTRA_SESSION_ID,    sessionId)
            putExtra(EXTRA_TYPE,          type.name)
            putExtra(EXTRA_TOTAL_SECONDS, totalSeconds)
            putExtra(EXTRA_TASK_ID,       taskId)
            putExtra(EXTRA_TASK_TITLE,    taskTitle)
            putExtra(EXTRA_CYCLE_INDEX,   cycleIndex)
        }

        fun pauseIntent(context: Context)  = Intent(context, PomodoroForegroundService::class.java).apply { action = ACTION_PAUSE }
        fun resumeIntent(context: Context) = Intent(context, PomodoroForegroundService::class.java).apply { action = ACTION_RESUME }
        fun skipIntent(context: Context)   = Intent(context, PomodoroForegroundService::class.java).apply { action = ACTION_SKIP }
        fun stopIntent(context: Context)   = Intent(context, PomodoroForegroundService::class.java).apply { action = ACTION_STOP }
    }

    inner class PomodoroServiceBinder : Binder() {
        fun getService(): PomodoroForegroundService = this@PomodoroForegroundService
    }

    private val binder       = PomodoroServiceBinder()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _timerState = MutableStateFlow<TimerState>(TimerState.Idle)
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    private var tickJob: Job? = null
    private var tickStartEpochMs: Long = 0L

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val sessionId    = intent.getStringExtra(EXTRA_SESSION_ID)    ?: return START_REDELIVER_INTENT
                val typeName     = intent.getStringExtra(EXTRA_TYPE)          ?: return START_REDELIVER_INTENT
                val totalSeconds = intent.getIntExtra(EXTRA_TOTAL_SECONDS, 0)
                val taskId       = intent.getStringExtra(EXTRA_TASK_ID)
                val taskTitle    = intent.getStringExtra(EXTRA_TASK_TITLE)
                val cycleIndex   = intent.getIntExtra(EXTRA_CYCLE_INDEX, 0)
                val type         = PomodoroType.valueOf(typeName)
                startTimer(sessionId, type, totalSeconds, taskId, taskTitle, cycleIndex)
                return START_REDELIVER_INTENT
            }
            ACTION_PAUSE  -> pauseTimer()
            ACTION_RESUME -> resumeTimer()
            ACTION_SKIP   -> skipTimer()
            ACTION_STOP   -> stopSelf()
        }
        return START_NOT_STICKY
    }

    private fun startTimer(
        sessionId: String,
        type: PomodoroType,
        totalSeconds: Int,
        taskId: String?,
        taskTitle: String?,
        cycleIndex: Int
    ) {
        tickJob?.cancel()
        tickStartEpochMs = System.currentTimeMillis()

        val runningState = TimerState.Running(
            sessionId        = sessionId,
            type             = type,
            totalSeconds     = totalSeconds,
            remainingSeconds = totalSeconds,
            taskId           = taskId,
            taskTitle        = taskTitle,
            cycleIndex       = cycleIndex
        )

        _timerState.value = runningState
        startForeground(NOTIFICATION_ID, buildNotification(runningState))

        tickJob = serviceScope.launch {
            while (true) {
                val elapsedMs = System.currentTimeMillis() - tickStartEpochMs
                val remaining = maxOf(0, totalSeconds - (elapsedMs / 1000).toInt())
                val current = _timerState.value

                when (current) {
                    is TimerState.Paused -> {
                        delay(200)
                        continue
                    }
                    is TimerState.Running -> {
                        val updated = current.copy(remainingSeconds = remaining)
                        _timerState.value = updated
                        if (remaining > 0) {
                            updateNotification(updated)
                        } else {
                            _timerState.value = TimerState.Completed(current.type, current.cycleIndex)
                            showCompletionNotification(current.type)
                            stopForeground(STOP_FOREGROUND_REMOVE)
                            break
                        }
                        val nextBoundaryMs = ((elapsedMs / 1000) + 1) * 1000
                        val sleepMs = nextBoundaryMs - elapsedMs
                        if (sleepMs > 0) delay(sleepMs)
                    }
                    else -> break
                }
            }
        }
    }

    private fun pauseTimer() {
        val current = _timerState.value as? TimerState.Running ?: return
        _timerState.value = TimerState.Paused(
            sessionId        = current.sessionId,
            type             = current.type,
            totalSeconds     = current.totalSeconds,
            remainingSeconds = current.remainingSeconds,
            taskId           = current.taskId,
            taskTitle        = current.taskTitle,
            cycleIndex       = current.cycleIndex
        )
    }

    private fun resumeTimer() {
        val current = _timerState.value as? TimerState.Paused ?: return
        tickStartEpochMs = System.currentTimeMillis() - (current.totalSeconds - current.remainingSeconds) * 1000L
        _timerState.value = TimerState.Running(
            sessionId        = current.sessionId,
            type             = current.type,
            totalSeconds     = current.totalSeconds,
            remainingSeconds = current.remainingSeconds,
            taskId           = current.taskId,
            taskTitle        = current.taskTitle,
            cycleIndex       = current.cycleIndex
        )
    }

    private fun skipTimer() {
        val current = _timerState.value
        val (type, cycle) = when (current) {
            is TimerState.Running -> current.type to current.cycleIndex
            is TimerState.Paused  -> current.type to current.cycleIndex
            else                  -> return
        }
        tickJob?.cancel()
        _timerState.value = TimerState.Completed(type, cycle)
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    fun resetToIdle() {
        tickJob?.cancel()
        _timerState.value = TimerState.Idle
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    fun getRemainingSeconds(): Int = when (val s = _timerState.value) {
        is TimerState.Running -> s.remainingSeconds
        is TimerState.Paused  -> s.remainingSeconds
        else                  -> 0
    }

    private fun buildNotification(state: TimerState.Running): Notification {
        val minutes = state.remainingSeconds / 60
        val seconds = state.remainingSeconds % 60
        val timeStr = "%02d:%02d".format(minutes, seconds)

        val label = when (state.type) {
            PomodoroType.FOCUS       -> "Focus"
            PomodoroType.SHORT_BREAK -> "Short Break"
            PomodoroType.LONG_BREAK  -> "Long Break"
        }

        val taskLine = state.taskTitle?.let { " — $it" } ?: ""

        return buildBaseNotification(
            title    = "$label $timeStr$taskLine",
            ongoing  = true,
            isPaused = false
        )
    }

    private fun updateNotification(state: TimerState.Running) {
        getSystemService(NotificationManager::class.java)
            .notify(NOTIFICATION_ID, buildNotification(state))
    }

    private fun showCompletionNotification(type: PomodoroType) {
        val title = when (type) {
            PomodoroType.FOCUS       -> "Focus session complete!"
            PomodoroType.SHORT_BREAK -> "Break over — ready to focus?"
            PomodoroType.LONG_BREAK  -> "Long break done — great job!"
        }
        getSystemService(NotificationManager::class.java)
            .notify(NOTIFICATION_ID + 1, buildBaseNotification(title, ongoing = false, isPaused = false))
    }

    private fun buildBaseNotification(
        title: String,
        ongoing: Boolean,
        isPaused: Boolean
    ): Notification {
        val pausePi  = PendingIntent.getService(this, 0, pauseIntent(this),  PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val resumePi = PendingIntent.getService(this, 1, resumeIntent(this), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val skipPi   = PendingIntent.getService(this, 2, skipIntent(this),   PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(ongoing)
            .setSilent(true)
            .addAction(
                if (isPaused) android.R.drawable.ic_media_play else android.R.drawable.ic_media_pause,
                if (isPaused) "Resume" else "Pause",
                if (isPaused) resumePi else pausePi
            )
            .addAction(android.R.drawable.ic_media_next, "Skip", skipPi)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Pomodoro timer countdown"
            setShowBadge(false)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
