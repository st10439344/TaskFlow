package com.taskflow.app.data.repo

import android.content.Context
import com.taskflow.app.data.SessionManager
import com.taskflow.app.data.local.AppDatabase
import com.taskflow.app.data.local.SYNC_PENDING
import com.taskflow.app.data.local.SYNC_SYNCED
import com.taskflow.app.data.local.TaskEntity
import com.taskflow.app.data.remote.ApiResult
import com.taskflow.app.data.remote.ApiService
import com.taskflow.app.data.remote.ListRequest
import com.taskflow.app.data.remote.SyncRequest
import com.taskflow.app.data.remote.SyncResponse
import com.taskflow.app.data.remote.safeCall
import com.taskflow.app.work.ReminderScheduler
import com.taskflow.app.work.SyncWorker
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class TaskRepository(
    private val ctx: Context,
    private val db: AppDatabase,
    private val api: ApiService,
    private val session: SessionManager
) {
    private val taskDao = db.taskDao()
    private val listDao = db.listDao()
    private val mutex = Mutex()

    fun observeTasks() = taskDao.observeAll()
    fun observeTask(id: String) = taskDao.observe(id)
    fun observeLists() = listDao.observeAll()
    fun observePendingCount() = taskDao.observePendingCount()

    suspend fun getTask(id: String) = taskDao.get(id)
    suspend fun getLists() = listDao.getAll()

    // ---------- Tasks: always written to Room first, then synced ----------

    suspend fun saveTask(task: TaskEntity) {
        val t = task.copy(updatedAt = System.currentTimeMillis(), syncStatus = SYNC_PENDING, deleted = false)
        taskDao.upsert(t)
        ReminderScheduler.schedule(ctx, t, session.notifications)
        SyncWorker.enqueue(ctx)
    }

    suspend fun setComplete(task: TaskEntity, done: Boolean) {
        if (task.isComplete != done) saveTask(task.copy(isComplete = done))
    }

    suspend fun deleteTask(task: TaskEntity) {
        taskDao.upsert(task.copy(deleted = true, syncStatus = SYNC_PENDING, updatedAt = System.currentTimeMillis()))
        ReminderScheduler.cancel(ctx, task.localId)
        SyncWorker.enqueue(ctx)
    }

    // ---------- Lists: need the server (they are the parents of synced tasks) ----------

    suspend fun createList(name: String): ApiResult<Unit> =
        when (val r = safeCall { api.createList(ListRequest(name.trim())) }) {
            is ApiResult.Success -> { listDao.upsert(r.data.list.toEntity()); ApiResult.Success(Unit) }
            is ApiResult.Error -> r
        }

    suspend fun renameList(id: String, name: String): ApiResult<Unit> =
        when (val r = safeCall { api.renameList(id, ListRequest(name.trim())) }) {
            is ApiResult.Success -> { listDao.upsert(r.data.list.toEntity()); ApiResult.Success(Unit) }
            is ApiResult.Error -> r
        }

    suspend fun deleteList(id: String): ApiResult<Unit> =
        when (val r = safeCall { api.deleteList(id) }) {
            is ApiResult.Success -> { taskDao.deleteByList(id); listDao.delete(id); ApiResult.Success(Unit) }
            is ApiResult.Error -> r
        }

    // ---------- Sync: push pending changes, then pull the server state ----------

    /** Returns true when there is nothing left to retry (success, or an error retrying cannot fix). */
    suspend fun sync(): Boolean = mutex.withLock {
        if (!session.isLoggedIn) return@withLock true

        val pending = taskDao.getPending()
        if (pending.isNotEmpty()) {
            when (val r = safeCall { api.sync(SyncRequest(pending.map { it.toChange() })) }) {
                is ApiResult.Success -> {
                    applyResults(pending, r.data)
                    r.data.stats?.let { session.saveStats(it.streakCount, it.weeklyCompletedCount) }
                }
                is ApiResult.Error -> return@withLock r.code == 401
            }
        }

        when (val r = safeCall { api.getLists() }) {
            is ApiResult.Success -> listDao.replaceAll(r.data.lists.map { it.toEntity() })
            is ApiResult.Error -> return@withLock r.code == 401
        }

        when (val r = safeCall { api.getTasks() }) {
            is ApiResult.Success -> {
                mergeServerTasks(r.data.tasks)
                r.data.stats?.let { session.saveStats(it.streakCount, it.weeklyCompletedCount) }
            }
            is ApiResult.Error -> return@withLock r.code == 401
        }
        true
    }

    private suspend fun applyResults(pushed: List<TaskEntity>, response: SyncResponse) {
        val byId = pushed.associateBy { it.localId }
        for (result in response.results) {
            val sent = byId[result.clientId ?: continue] ?: continue
            val current = taskDao.get(sent.localId)
            when (val action = SyncReconciler.decide(sent, current, result)) {
                is SyncAction.MarkSynced ->
                    current?.let { taskDao.upsert(it.copy(serverId = action.serverId ?: it.serverId, syncStatus = SYNC_SYNCED)) }
                is SyncAction.KeepPending ->
                    current?.let { taskDao.upsert(it.copy(serverId = action.serverId ?: it.serverId)) }
                SyncAction.HardDelete -> taskDao.hardDelete(sent.localId)
                is SyncAction.ReplaceWithServer ->
                    taskDao.upsert(action.task.toEntity(sent.localId, current?.remind ?: false))
                SyncAction.None -> Unit
            }
        }
    }

    private suspend fun mergeServerTasks(serverTasks: List<com.taskflow.app.data.remote.TaskDto>) {
        val local = taskDao.getAll()
        val byLocalId = local.associateBy { it.localId }
        val byServerId = local.filter { it.serverId != null }.associateBy { it.serverId }
        val serverIds = serverTasks.map { it.id }.toSet()

        val toUpsert = mutableListOf<TaskEntity>()
        for (st in serverTasks) {
            val existing = st.clientId?.let { byLocalId[it] } ?: byServerId[st.id]
            if (existing != null && existing.syncStatus == SYNC_PENDING) continue // local edits win until pushed
            toUpsert += st.toEntity(existing?.localId ?: st.clientId ?: st.id, existing?.remind ?: false)
        }
        taskDao.upsertAll(toUpsert)

        // Tasks that were synced before but no longer exist on the server were deleted elsewhere.
        local.filter { it.syncStatus == SYNC_SYNCED && it.serverId != null && it.serverId !in serverIds }
            .forEach { taskDao.hardDelete(it.localId) }
    }
}
