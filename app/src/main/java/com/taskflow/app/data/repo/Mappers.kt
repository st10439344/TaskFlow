package com.taskflow.app.data.repo

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.taskflow.app.data.local.ListEntity
import com.taskflow.app.data.local.SYNC_SYNCED
import com.taskflow.app.data.local.Subtask
import com.taskflow.app.data.local.TaskEntity
import com.taskflow.app.data.remote.ListDto
import com.taskflow.app.data.remote.SubtaskDto
import com.taskflow.app.data.remote.SyncChange
import com.taskflow.app.data.remote.TaskDto
import com.taskflow.app.util.DateUtils

object SubtaskCodec {
    private val gson = Gson()
    private val type = object : TypeToken<List<Subtask>>() {}.type

    fun toJson(list: List<Subtask>): String = gson.toJson(list)

    fun fromJson(json: String?): List<Subtask> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            gson.fromJson<List<Subtask>>(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}

fun ListDto.toEntity() = ListEntity(id = id, name = name, colorTag = colorTag)

fun TaskEntity.toChange() = SyncChange(
    clientId = localId,
    listId = listId,
    title = title,
    description = description,
    dueDate = dueDate,
    dueTime = dueTime,
    priority = priority,
    repeatRule = repeatRule,
    isComplete = isComplete,
    subtasks = SubtaskCodec.fromJson(subtasksJson).map { SubtaskDto(null, it.title, it.isComplete) },
    updatedAt = DateUtils.toIso(updatedAt),
    deleted = deleted
)

fun TaskDto.toEntity(localId: String, remind: Boolean, now: Long = System.currentTimeMillis()) = TaskEntity(
    localId = localId,
    serverId = id,
    listId = listId,
    title = title,
    description = description ?: "",
    dueDate = dueDate?.take(10),
    dueTime = dueTime,
    priority = priority ?: "med",
    repeatRule = repeatRule ?: "none",
    isComplete = isComplete,
    remind = remind,
    subtasksJson = SubtaskCodec.toJson((subtasks ?: emptyList()).map { Subtask(it.title, it.isComplete) }),
    updatedAt = now,
    syncStatus = SYNC_SYNCED,
    deleted = false
)
