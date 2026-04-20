package com.oussama_chatri.productivityx.core.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
//import com.oussama_chatri.productivityx.features.pomodoro.service.PomodoroForegroundService

class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
//        val serviceIntent = Intent(context, PomodoroForegroundService::class.java)
//            .setAction(action)
//        context.startService(serviceIntent)
    }

    companion object {
        const val ACTION_PLAY_PAUSE = "px.action.PLAY_PAUSE"
        const val ACTION_SKIP = "px.action.SKIP"
        const val ACTION_STOP = "px.action.STOP"
    }
}