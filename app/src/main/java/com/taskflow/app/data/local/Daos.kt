package com.taskflow.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ListDao {
    @Query("SELECT * FROM task_lists ORDER BY name COLLATE NOCASE")
    abstract fun observeAll(): Flow<List<ListEntity>>

    @Query("SELECT * FROM task_lists ORDER BY name COLLATE NOCASE")
    abstract suspend fun getAll(): List<ListEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsert(item: ListEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertAll(items: List<ListEntity>)

    @Query("DELETE FROM task_lists WHERE id = :id")
    abstract suspend fun delete(id: String)

    @Query("DELETE FROM task_lists")
    abstract suspend fun clear()

    @Transaction
    open suspend fun replaceAll(items: List<ListEntity>) {
        clear()
        upsertAll(items)
    }
}

@Dao
interface TaskDao {
    @Query(
        "SELECT * FROM tasks WHERE deleted = 0 ORDER BY isComplete, " +
            "CASE WHEN dueDate IS NULL THEN 1 ELSE 0 END, dueDate, dueTime, updatedAt DESC"
    )
    fun observeAll(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE localId = :id")
    fun observe(id: String): Flow<TaskEntity?>

    @Query("SELECT * FROM tasks WHERE localId = :id")
    suspend fun get(id: String): TaskEntity?

    @Query("SELECT * FROM tasks")
    suspend fun getAll(): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE syncStatus = 'pending'")
    suspend fun getPending(): List<TaskEntity>

    @Query("SELECT COUNT(*) FROM tasks WHERE syncStatus = 'pending'")
    fun observePendingCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(tasks: List<TaskEntity>)

    @Query("DELETE FROM tasks WHERE localId = :id")
    suspend fun hardDelete(id: String)

    @Query("DELETE FROM tasks WHERE listId = :listId")
    suspend fun deleteByList(listId: String)
}
