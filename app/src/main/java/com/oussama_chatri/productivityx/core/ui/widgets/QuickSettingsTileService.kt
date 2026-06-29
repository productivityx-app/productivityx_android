package com.oussama_chatri.productivityx.core.ui.widgets

import android.content.Intent
import android.service.quicksettings.TileService
import com.oussama_chatri.productivityx.MainActivity

class QuickSettingsTileService : TileService() {
    override fun onClick() {
        super.onClick()
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("open_pomodoro", true)
        }
        startActivity(intent)
    }
}

class QuickNoteTileService : TileService() {
    override fun onClick() {
        super.onClick()
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("quick_note", true)
        }
        startActivity(intent)
    }
}

class FocusModeTileService : TileService() {
    override fun onClick() {
        super.onClick()
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("toggle_focus_mode", true)
        }
        startActivity(intent)
    }
}
