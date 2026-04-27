package com.oussama_chatri.productivityx.core.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `conversations` (
                `id`            TEXT    NOT NULL,
                `title`         TEXT,
                `is_archived`   INTEGER NOT NULL DEFAULT 0,
                `last_message`  TEXT,
                `message_count` INTEGER NOT NULL DEFAULT 0,
                `created_at`    INTEGER NOT NULL,
                `updated_at`    INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `messages` (
                `id`               TEXT    NOT NULL,
                `conversation_id`  TEXT    NOT NULL,
                `role`             TEXT    NOT NULL,
                `content`          TEXT    NOT NULL,
                `action_block_json` TEXT,
                `token_count`      INTEGER,
                `created_at`       INTEGER NOT NULL,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`conversation_id`) REFERENCES `conversations`(`id`) ON DELETE CASCADE
            )
            """.trimIndent()
        )

        db.execSQL("CREATE INDEX IF NOT EXISTS `index_messages_conversation_id` ON `messages`(`conversation_id`)")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `pomodoro_sessions_local` (
                `id`                        TEXT    NOT NULL,
                `user_id`                   TEXT    NOT NULL,
                `task_id`                   TEXT,
                `task_title`                TEXT,
                `type`                      TEXT    NOT NULL,
                `planned_duration_seconds`  INTEGER NOT NULL,
                `actual_duration_seconds`   INTEGER,
                `completed`                 INTEGER NOT NULL DEFAULT 0,
                `interrupted`               INTEGER NOT NULL DEFAULT 0,
                `started_at`                INTEGER NOT NULL,
                `ended_at`                  INTEGER,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )

        db.execSQL("CREATE INDEX IF NOT EXISTS `index_pomodoro_sessions_local_user_id` ON `pomodoro_sessions_local`(`user_id`)")
    }
}