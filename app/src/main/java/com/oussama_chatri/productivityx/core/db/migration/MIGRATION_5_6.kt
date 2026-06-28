package com.oussama_chatri.productivityx.core.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `pomodoro_sessions_local` ADD COLUMN `interrupt_reason` TEXT DEFAULT NULL")
        db.execSQL("ALTER TABLE `pomodoro_sessions_local` ADD COLUMN `sync_status` TEXT NOT NULL DEFAULT 'PENDING'")
        db.execSQL("ALTER TABLE `pomodoro_sessions_local` ADD COLUMN `pending_operation` TEXT DEFAULT NULL")
    }
}
