package com.oussama_chatri.productivityx.core.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration 4 → 5
 */
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {

        // 1. Copy existing rows into a temp table
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `pomodoro_sessions_local_backup` (
                `id`                       TEXT    NOT NULL,
                `user_id`                  TEXT    NOT NULL,
                `task_id`                  TEXT,
                `task_title`               TEXT,
                `type`                     TEXT    NOT NULL,
                `planned_duration_seconds` INTEGER NOT NULL,
                `actual_duration_seconds`  INTEGER,
                `completed`                INTEGER NOT NULL,
                `interrupted`              INTEGER NOT NULL,
                `started_at`               INTEGER NOT NULL,
                `ended_at`                 INTEGER,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            INSERT INTO `pomodoro_sessions_local_backup`
            SELECT `id`, `user_id`, `task_id`, `task_title`, `type`,
                   `planned_duration_seconds`, `actual_duration_seconds`,
                   `completed`, `interrupted`, `started_at`, `ended_at`
            FROM `pomodoro_sessions_local`
            """.trimIndent()
        )

        // 2. Drop the old table (and its index along with it)
        db.execSQL("DROP TABLE `pomodoro_sessions_local`")

        // 3. Recreate exactly as Room generates it from the entity —
        //    no DEFAULT values, no extra indices
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `pomodoro_sessions_local` (
                `id`                       TEXT    NOT NULL,
                `user_id`                  TEXT    NOT NULL,
                `task_id`                  TEXT,
                `task_title`               TEXT,
                `type`                     TEXT    NOT NULL,
                `planned_duration_seconds` INTEGER NOT NULL,
                `actual_duration_seconds`  INTEGER,
                `completed`                INTEGER NOT NULL,
                `interrupted`              INTEGER NOT NULL,
                `started_at`               INTEGER NOT NULL,
                `ended_at`                 INTEGER,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )

        // 4. Restore the data
        db.execSQL(
            """
            INSERT INTO `pomodoro_sessions_local`
            SELECT `id`, `user_id`, `task_id`, `task_title`, `type`,
                   `planned_duration_seconds`, `actual_duration_seconds`,
                   `completed`, `interrupted`, `started_at`, `ended_at`
            FROM `pomodoro_sessions_local_backup`
            """.trimIndent()
        )

        // 5. Drop the temp table
        db.execSQL("DROP TABLE `pomodoro_sessions_local_backup`")
    }
}