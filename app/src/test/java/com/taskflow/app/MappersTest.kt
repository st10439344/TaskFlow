package com.taskflow.app

import com.taskflow.app.data.local.SYNC_SYNCED
import com.taskflow.app.data.local.Subtask
import com.taskflow.app.data.local.TaskEntity
import com.taskflow.app.data.remote.SubtaskDto
import com.taskflow.app.data.remote.TaskDto
import com.taskflow.app.data.repo.SubtaskCodec
import com.taskflow.app.data.repo.toChange
import com.taskflow.app.data.repo.toEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MappersTest {
    @Test fun subtaskCodecRoundTrips() {
        val list = listOf(Subtask("Print document", true), Subtask("Bring student card", false))
        assertEquals(list, SubtaskCodec.fromJson(SubtaskCodec.toJson(list)))
    }

    @Test fun subtaskCodecSurvivesBadJson() {
        assertTrue(SubtaskCodec.fromJson(null).isEmpty())
        assertTrue(SubtaskCodec.fromJson("").isEmpty())
        assertTrue(SubtaskCodec.fromJson("{not json").isEmpty())
    }

    @Test fun localTaskBecomesSyncChange() {
        val task = TaskEntity(
            localId = "uuid-1", listId = "list-1", title = "Submit assignment", dueDate = "2026-08-24",
            dueTime = "16:00", priority = "high", updatedAt = 0L,
            subtasksJson = SubtaskCodec.toJson(listOf(Subtask("Print", true)))
        )
        val change = task.toChange()
        assertEquals("uuid-1", change.clientId)
        assertEquals("list-1", change.listId)
        assertEquals("1970-01-01T00:00:00.000Z", change.updatedAt)
        assertEquals(listOf(SubtaskDto(null, "Print", true)), change.subtasks)
        assertFalse(change.deleted)
        assertTrue(task.copy(deleted = true).toChange().deleted)
    }

    @Test fun clearedDueDateIsSentAsNull() {
        assertNull(TaskEntity(localId = "a", listId = "l", title = "t").toChange().dueDate)
    }

    @Test fun serverTaskBecomesSyncedEntity() {
        val dto = TaskDto(
            id = "srv-1", listId = "list-1", clientId = "uuid-1", title = "Buy groceries", description = null,
            dueDate = "2026-08-23T00:00:00.000Z", dueTime = null, priority = null, repeatRule = null, isComplete = true,
            subtasks = listOf(SubtaskDto("s1", "Milk", false))
        )
        val e = dto.toEntity("uuid-1", remind = true, now = 5L)
        assertEquals("uuid-1", e.localId)
        assertEquals("srv-1", e.serverId)
        assertEquals("2026-08-23", e.dueDate)
        assertEquals("med", e.priority)
        assertEquals("", e.description)
        assertTrue(e.remind)
        assertTrue(e.isComplete)
        assertEquals(SYNC_SYNCED, e.syncStatus)
        assertEquals(listOf(Subtask("Milk", false)), SubtaskCodec.fromJson(e.subtasksJson))
    }
}
