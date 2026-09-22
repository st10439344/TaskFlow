package com.taskflow.app.data.repo

import com.taskflow.app.data.local.TaskEntity
import com.taskflow.app.data.remote.SyncResultDto
import com.taskflow.app.data.remote.TaskDto

sealed interface SyncAction {
    data class MarkSynced(val serverId: String?) : SyncAction
    data class KeepPending(val serverId: String?) : SyncAction
    object HardDelete : SyncAction
    data class ReplaceWithServer(val task: TaskDto) : SyncAction
    object None : SyncAction
}

/**
 * Decides what to do with one local row after the server answered a sync request.
 * [pushed] is the row as it was sent, [current] is the row as it is in Room now
 * (the user may have edited it while the request was in flight).
 */
object SyncReconciler {
    fun decide(pushed: TaskEntity, current: TaskEntity?, result: SyncResultDto): SyncAction {
        if (current == null) return SyncAction.None
        val unchanged = current.updatedAt == pushed.updatedAt
        return when (result.status) {
            "created", "updated" ->
                if (unchanged) SyncAction.MarkSynced(result.serverId ?: current.serverId)
                else SyncAction.KeepPending(result.serverId ?: current.serverId)
            "deleted" -> if (current.deleted) SyncAction.HardDelete else SyncAction.None
            "conflict" ->
                if (unchanged && result.task != null) SyncAction.ReplaceWithServer(result.task) else SyncAction.None
            else -> SyncAction.None
        }
    }
}
