package com.taskflow.app.work

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.taskflow.app.R
import com.taskflow.app.TaskDetailActivity
import com.taskflow.app.TaskFlowApp
import com.taskflow.app.data.local.TaskEntity
import com.taskflow.app.util.DateUtils
import java.util.concurrent.TimeUnit

object ReminderScheduler {
    private fun name(id: String) = "reminder-$id"

    fun schedule(context: Context, task: TaskEntity, notificationsEnabled: Boolean) {
        val wm = WorkManager.getInstance(context)
        val due = if (task.remind && notificationsEnabled && !task.isComplete && !task.deleted && task.dueDate != null) {
            DateUtils.dueMillis(task.dueDate, task.dueTime)
        } else null
        val delay = due?.minus(System.currentTimeMillis())
        if (delay == null || delay <= 0) {
            wm.cancelUniqueWork(name(task.localId))
            return
        }
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf("taskId" to task.localId))
            .build()
        wm.enqueueUniqueWork(name(task.localId), ExistingWorkPolicy.REPLACE, request)
    }

    fun cancel(context: Context, taskId: String) {
        WorkManager.getInstance(context).cancelUniqueWork(name(taskId))
    }
}

/** Posts a local notification when a task with "Remind me" reaches its due time. */
class ReminderWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    @SuppressLint("MissingPermission")
    override suspend fun doWork(): Result {
        val app = applicationContext as TaskFlowApp
        val id = inputData.getString("taskId") ?: return Result.success()
        val task = app.db.taskDao().get(id) ?: return Result.success()
        if (task.deleted || task.isComplete || !app.session.notifications) return Result.success()

        val intent = Intent(applicationContext, TaskDetailActivity::class.java)
            .putExtra(TaskDetailActivity.EXTRA_TASK_ID, id)
        val pending = PendingIntent.getActivity(
            applicationContext, id.hashCode(), intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = NotificationCompat.Builder(applicationContext, TaskFlowApp.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(applicationContext.getString(R.string.reminder_title))
            .setContentText(task.title)
            .setContentIntent(pending)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        try {
            NotificationManagerCompat.from(applicationContext).notify(id.hashCode(), notification)
        } catch (e: SecurityException) {
            // Notification permission not granted; nothing to do.
        }
        return Result.success()
    }
}
