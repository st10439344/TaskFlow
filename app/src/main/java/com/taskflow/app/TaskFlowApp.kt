package com.taskflow.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import com.taskflow.app.data.SessionManager
import com.taskflow.app.data.local.AppDatabase
import com.taskflow.app.data.remote.ApiClient
import com.taskflow.app.data.remote.ApiService
import com.taskflow.app.data.repo.TaskRepository
import com.taskflow.app.data.repo.UserRepository
import com.taskflow.app.util.NetworkMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/** Simple manual dependency container: one instance of everything for the whole app. */
class TaskFlowApp : Application() {

    lateinit var session: SessionManager
    lateinit var db: AppDatabase
    lateinit var api: ApiService
    lateinit var network: NetworkMonitor
    lateinit var taskRepo: TaskRepository
    lateinit var userRepo: UserRepository

    /** Survives activity recreation (e.g. when the language or theme changes). */
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        session = SessionManager(this)
        db = AppDatabase.build(this)
        api = ApiClient.create(session)
        network = NetworkMonitor(this)
        taskRepo = TaskRepository(this, db, api, session)
        userRepo = UserRepository(this, api, session, db)

        AppCompatDelegate.setDefaultNightMode(
            if (session.theme == "dark") {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )

        // Notification channels only exist on Android 8.0 (API 26) and newer.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notif_channel),
                NotificationManager.IMPORTANCE_HIGH
            )

            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "task_reminders"
    }
}