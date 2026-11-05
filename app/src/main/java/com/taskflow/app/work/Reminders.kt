package com.taskflow.app.work

import android.content.Context
import android.content.Intent
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
import com.taskflow.app.push.NotificationHelper
import com.taskflow.app.util.AppLog
import com.taskflow.app.util.DateUtils
import com.taskflow.app.util.LocaleUtils
import java.util.concurrent.TimeUnit

/** Schedules a local reminder for tasks that have "Remind me" switched on. */
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
        AppLog.d("Reminder", "Reminder scheduled in ${delay / 60000} minute(s)")
    }

    fun cancel(context: Context, taskId: String) {
        WorkManager.getInstance(context).cancelUniqueWork(name(taskId))
    }
}

/** Posts a local notification when a task with "Remind me" reaches its due time. */
class ReminderWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as TaskFlowApp
        val id = inputData.getString("taskId") ?: return Result.success()
        val task = app.db.taskDao().get(id) ?: return Result.success()
        if (task.deleted || task.isComplete || !app.session.notifications) return Result.success()

        val localized = LocaleUtils.localized(applicationContext, app.session.language)
        val intent = Intent(applicationContext, TaskDetailActivity::class.java)
            .putExtra(TaskDetailActivity.EXTRA_TASK_ID, id)
        NotificationHelper.show(
            applicationContext, NotificationHelper.CHANNEL_REMINDERS, id.hashCode(),
            localized.getString(R.string.reminder_title), task.title, intent
        )
        return Result.success()
    }
}
