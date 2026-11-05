package com.taskflow.app.push

/**
 * What the API sends through Firebase Cloud Messaging (data-only messages; see backend/src/services/push.js).
 * The app turns each one into a notification in the user's own language.
 */
sealed interface PushMessage {
    data class Streak(val days: Int) : PushMessage
    object AllDone : PushMessage
    object Test : PushMessage
    /** Silent: another device changed data, so pull the latest (real-time sync). */
    object Sync : PushMessage
    object Unknown : PushMessage

    companion object {
        fun parse(data: Map<String, String>): PushMessage = when (data["type"]) {
            "streak" -> Streak(data["count"]?.toIntOrNull() ?: 0)
            "all_done" -> AllDone
            "test" -> Test
            "sync" -> Sync
            else -> Unknown
        }
    }
}
