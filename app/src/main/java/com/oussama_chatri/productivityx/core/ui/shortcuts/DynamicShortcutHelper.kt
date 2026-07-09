package com.oussama_chatri.productivityx.core.ui.shortcuts

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.oussama_chatri.productivityx.MainActivity
import com.oussama_chatri.productivityx.features.home.presentation.state.QuickAction

object DynamicShortcutHelper {

    fun pushDynamicShortcut(
        context: Context,
        action: QuickAction,
        label: String,
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            this.action = Intent.ACTION_VIEW
            data = when (action) {
                QuickAction.NEW_NOTE -> android.net.Uri.parse("productivityx://notes/new")
                QuickAction.NEW_TASK -> android.net.Uri.parse("productivityx://tasks/new")
                QuickAction.START_TIMER -> android.net.Uri.parse("productivityx://pomodoro/start")
                QuickAction.AI_CHAT -> android.net.Uri.parse("productivityx://ai/chat")
                QuickAction.CALCULATOR -> android.net.Uri.parse("productivityx://calculator")
            }
        }

        val shortcut = ShortcutInfoCompat.Builder(context, action.name)
            .setShortLabel(label)
            .setLongLabel(label)
            .setIcon(IconCompat.createWithResource(context, android.R.drawable.ic_menu_edit))
            .setIntent(intent)
            .build()

        ShortcutManagerCompat.pushDynamicShortcut(context, shortcut)
    }

    fun reportShortcutUsed(context: Context, action: QuickAction) {
        ShortcutManagerCompat.reportShortcutUsed(context, action.name)
    }
}
