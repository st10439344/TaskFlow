package com.taskflow.app

import com.taskflow.app.data.local.TaskEntity
import com.taskflow.app.data.remote.SyncResultDto
import com.taskflow.app.data.remote.TaskDto
import com.taskflow.app.data.repo.SyncAction
import com.taskflow.app.data.repo.SyncReconciler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SyncReconcilerTest {
    private val sent = TaskEntity(localId = "a", listId = "l", title = "t", updatedAt = 100L)
    private fun result(status: String, serverId: String? = null, task: TaskDto? = null) =
        SyncResultDto(clientId = "a", status = status, serverId = serverId, task = task, message = null)

    @Test fun createdTaskIsMarkedSyncedWithServerId() {
        assertEquals(SyncAction.MarkSynced("srv"), SyncReconciler.decide(sent, sent, result("created", "srv")))
    }

    @Test fun editMadeWhileSyncingStaysPending() {
        val edited = sent.copy(updatedAt = 200L)
        assertEquals(SyncAction.KeepPending("srv"), SyncReconciler.decide(sent, edited, result("updated", "srv")))
    }

    @Test fun deletedTaskIsRemovedLocally() {
        val deleted = sent.copy(deleted = true)
        assertTrue(SyncReconciler.decide(deleted, deleted, result("deleted")) === SyncAction.HardDelete)
    }

    @Test fun conflictTakesTheServerCopy() {
        val dto = TaskDto("srv", "l", "a", "Server title", null, null, null, null, null, false, null)
        assertEquals(SyncAction.ReplaceWithServer(dto), SyncReconciler.decide(sent, sent, result("conflict", task = dto)))
    }

    @Test fun errorsAndMissingRowsChangeNothing() {
        assertTrue(SyncReconciler.decide(sent, sent, result("error")) === SyncAction.None)
        assertTrue(SyncReconciler.decide(sent, null, result("created", "srv")) === SyncAction.None)
    }
}
