package com.oussama_chatri.productivityx.core.network

object ApiConstants {

    const val BASE_URL = "https://productivityx-backend.up.railway.app/"
    const val BASE_URL_DEV = "http://10.0.2.2:8080/"

    const val CONNECT_TIMEOUT_SEC = 30L
    const val READ_TIMEOUT_SEC = 60L
    const val WRITE_TIMEOUT_SEC = 30L

    const val HEADER_AUTHORIZATION = "Authorization"
    const val HEADER_BEARER_PREFIX = "Bearer "
    const val HEADER_CONTENT_TYPE = "Content-Type"
    const val HEADER_ACCEPT = "Accept"
    const val HEADER_APPLICATION_JSON = "application/json"

    const val MAX_RETRY_ATTEMPTS = 3

    object Auth {
        const val REGISTER = "api/v1/auth/register"
        const val LOGIN = "api/v1/auth/login"
        const val LOGOUT = "api/v1/auth/logout"
        const val REFRESH = "api/v1/auth/refresh"
        const val VERIFY_EMAIL = "api/v1/auth/verify-email"
        const val VERIFY_OTP = "api/v1/auth/verify-otp"
        const val RESEND_VERIFICATION = "api/v1/auth/resend-verification"
        const val FORGOT_PASSWORD = "api/v1/auth/forgot-password"
        const val RESET_PASSWORD = "api/v1/auth/reset-password"
        const val CHANGE_PASSWORD = "api/v1/auth/change-password"
        const val ME = "api/v1/auth/me"
        const val DELETE_ACCOUNT = "api/v1/auth/delete-account"
    }

    object Profile {
        const val BASE = "api/v1/profile"
        const val AVATAR = "api/v1/profile/avatar"
    }

    object Preferences {
        const val BASE = "api/v1/preferences"
    }

    object Notes {
        const val BASE = "api/v1/notes"
        const val TRASH = "api/v1/notes/trash"
        fun byId(id: String) = "api/v1/notes/$id"
        fun restore(id: String) = "api/v1/notes/$id/restore"
        fun pin(id: String) = "api/v1/notes/$id/pin"
        fun tags(id: String) = "api/v1/notes/$id/tags"
        fun tag(noteId: String, tagId: String) = "api/v1/notes/$noteId/tags/$tagId"
    }

    object Tags {
        const val BASE = "api/v1/tags"
        fun byId(id: String) = "api/v1/tags/$id"
    }

    object Tasks {
        const val BASE = "api/v1/tasks"
        const val REORDER = "api/v1/tasks/reorder"
        fun byId(id: String) = "api/v1/tasks/$id"
        fun status(id: String) = "api/v1/tasks/$id/status"
        fun restore(id: String) = "api/v1/tasks/$id/restore"
    }

    object Events {
        const val BASE = "api/v1/events"
        fun byId(id: String) = "api/v1/events/$id"
        fun restore(id: String) = "api/v1/events/$id/restore"
    }

    object Pomodoro {
        const val START = "api/v1/pomodoro/sessions/start"
        fun end(id: String) = "api/v1/pomodoro/sessions/$id/end"
        fun interrupt(id: String) = "api/v1/pomodoro/sessions/$id/interrupt"
        const val SESSIONS = "api/v1/pomodoro/sessions"
    }

    object Ai {
        const val CONVERSATIONS = "api/v1/ai/conversations"
        fun conversation(id: String) = "api/v1/ai/conversations/$id"
        fun messages(id: String) = "api/v1/ai/conversations/$id/messages"
    }

    object Search {
        const val BASE = "api/v1/search"
    }

    object Sync {
        const val DELTA = "api/v1/sync/delta"
    }

    object WebSocket {
        const val ENDPOINT = "/ws"
        const val TOPIC_NOTES = "/topic/notes"
        const val TOPIC_TASKS = "/topic/tasks"
        const val TOPIC_EVENTS = "/topic/events"
    }
}