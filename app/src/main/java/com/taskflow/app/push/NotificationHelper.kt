package com.taskflow.app.push

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.taskflow.app.R
import com.taskflow.app.util.AppLog

/** Builds and shows notifications for both local reminders and push alerts. */
object NotificationHelper {
    const val CHANNEL_REMINDERS = "task_reminders"
    const val CHANNEL_ALERTS = "task_alerts"

    @SuppressLint("MissingPermission") // Android 13+ permission is requested in MainActivity; without it notify() does nothing
    fun show(context: Context, channelId: String, notificationId: Int, title: String, body: String, tapIntent: Intent) {
        val pending = PendingIntent.getActivity(
            context, notificationId, tapIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(pending)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
            AppLog.d("Notify", "Notification shown on channel $channelId")
        } catch (e: SecurityException) {
            AppLog.w("Notify", "Notification permission not granted", e)
        }
    }
}
