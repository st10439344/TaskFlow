package com.taskflow.app.work

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.taskflow.app.TaskFlowApp
import com.taskflow.app.util.AppLog
import java.util.concurrent.TimeUnit

/**
 * Runs as soon as the device has a connection: pushes queued offline changes, then pulls the server state.
 * WorkManager guarantees the work runs even if the app is closed (Android Developers, n.d. "Schedule tasks with WorkManager").
 */
class SyncWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as TaskFlowApp
        app.userRepo.pushSettingsIfDirty()
        val done = app.taskRepo.sync()
        AppLog.d("SyncWorker", if (done) "Work finished" else "Work will be retried with backoff")
        return if (done) Result.success() else Result.retry()
    }

    companion object {
        private const val NAME = "taskflow-sync"

        fun enqueue(context: Context) {
            val request = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(NAME, ExistingWorkPolicy.APPEND_OR_REPLACE, request)
        }
    }
}
