package com.taskflow.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

const val SYNC_PENDING = "pending"
const val SYNC_SYNCED = "synced"

/** Lists are created online, so [id] is always the server id. */
@Entity(tableName = "task_lists")
data class ListEntity(
    @PrimaryKey val id: String,
    val name: String,
    val colorTag: String?
)

/**
 * Room is the source of truth (offline-first). [localId] is a UUID created on the device and is
 * sent to the API as `clientId`, which makes syncing idempotent.
 */
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val localId: String,
    val serverId: String? = null,
    val listId: String,
    val title: String,
    val description: String = "",
    val dueDate: String? = null,      // yyyy-MM-dd
    val dueTime: String? = null,      // HH:mm
    val priority: String = "med",     // low | med | high
    val repeatRule: String = "none",
    val isComplete: Boolean = false,
    val remind: Boolean = false,      // local only
    val subtasksJson: String = "[]",
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: String = SYNC_PENDING,
    val deleted: Boolean = false
)

data class Subtask(val title: String, val isComplete: Boolean)
