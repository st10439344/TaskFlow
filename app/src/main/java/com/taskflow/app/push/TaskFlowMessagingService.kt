package com.taskflow.app.push

import android.content.Intent
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.taskflow.app.MainActivity
import com.taskflow.app.R
import com.taskflow.app.TaskFlowApp
import com.taskflow.app.util.AppLog
import com.taskflow.app.util.LocaleUtils
import com.taskflow.app.work.SyncWorker

/**
 * Receives push messages from Firebase while the app is in the foreground OR background.
 * The API sends data-only messages, so this class decides what to show, in the user's language.
 */
class TaskFlowMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        PushManager.onNewToken(application as TaskFlowApp, token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val app = application as TaskFlowApp
        val parsed = PushMessage.parse(message.data)
        AppLog.i(TAG, "Push received: ${parsed::class.simpleName}")

        when (parsed) {
            PushMessage.Sync -> SyncWorker.enqueue(applicationContext)   // another device changed data: pull it now
            PushMessage.Unknown -> AppLog.w(TAG, "Ignoring unknown push type")
            else -> showAlert(app, parsed)
        }
    }

    private fun showAlert(app: TaskFlowApp, message: PushMessage) {
        if (!app.session.isLoggedIn) return
        // A test notification is always shown so the feature can be demonstrated; real alerts obey the Settings switch.
        if (message !is PushMessage.Test && !app.session.notifications) {
            AppLog.d(TAG, "Alert skipped: notifications are switched off in Settings")
            return
        }
        val ctx = LocaleUtils.localized(applicationContext, app.session.language)
        val (title, body) = when (message) {
            is PushMessage.Streak -> ctx.getString(R.string.push_streak_title) to ctx.getString(R.string.push_streak_body, message.days)
            PushMessage.AllDone -> ctx.getString(R.string.push_all_done_title) to ctx.getString(R.string.push_all_done_body)
            PushMessage.Test -> ctx.getString(R.string.push_test_title) to ctx.getString(R.string.push_test_body)
            else -> return
        }
        val intent = Intent(applicationContext, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        NotificationHelper.show(applicationContext, NotificationHelper.CHANNEL_ALERTS, (System.currentTimeMillis() % Int.MAX_VALUE).toInt(), title, body, intent)
    }

    private companion object {
        const val TAG = "PushService"
    }
}
