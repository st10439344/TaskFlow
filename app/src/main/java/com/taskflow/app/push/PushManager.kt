
package com.taskflow.app.push

import com.google.firebase.messaging.FirebaseMessaging
import com.taskflow.app.TaskFlowApp
import com.taskflow.app.data.remote.ApiResult
import com.taskflow.app.data.remote.DeviceTokenRequest
import com.taskflow.app.data.remote.safeCall
import com.taskflow.app.util.AppLog
import kotlinx.coroutines.launch

/**
 * Registers this Android device for Firebase Cloud Messaging.
 *
 * The backend may remove an expired or rejected FCM token.
 * Therefore, registration is checked again whenever TaskFlow
 * starts or resumes instead of relying on a previously saved
 * "synced" value.
 */
object PushManager {

    private const val TAG = "Push"

    /**
     * Get the current Firebase token and register it with
     * the TaskFlow backend.
     */
    fun registerDevice(app: TaskFlowApp) {
        if (!app.session.isLoggedIn) return

        val messaging = firebaseMessaging() ?: return

        messaging.token.addOnCompleteListener { task ->

            if (!task.isSuccessful) {
                AppLog.w(
                    TAG,
                    "Could not get an FCM token",
                    task.exception
                )
                return@addOnCompleteListener
            }

            val token = task.result

            if (token.isNullOrEmpty()) {
                AppLog.w(TAG, "FCM returned an empty token")
                return@addOnCompleteListener
            }

            app.session.fcmToken = token

            // Always confirm registration with the backend.
            // The server may have removed the token after
            // a failed Firebase delivery attempt.
            sendTokenToServer(app, token)
        }
    }

    /**
     * Called when Firebase creates or refreshes a token.
     */
    fun onNewToken(app: TaskFlowApp, token: String) {
        AppLog.i(TAG, "FCM token refreshed")

        app.session.fcmToken = token
        app.session.fcmTokenSynced = null

        sendTokenToServer(app, token)
    }

    /**
     * Send the current token to the backend.
     *
     * Do not skip this request just because the same token
     * was successfully registered previously.
     */
    private fun sendTokenToServer(
        app: TaskFlowApp,
        token: String
    ) {
        if (!app.session.isLoggedIn) return

        app.appScope.launch {
            when (
                val result = safeCall {
                    app.api.registerDevice(
                        DeviceTokenRequest(token)
                    )
                }
            ) {
                is ApiResult.Success -> {
                    app.session.fcmTokenSynced = token
                    AppLog.i(
                        TAG,
                        "Device registered for push notifications"
                    )
                }

                is ApiResult.Error -> {
                    app.session.fcmTokenSynced = null
                    AppLog.w(
                        TAG,
                        "Device registration failed (code ${result.code}), will retry"
                    )
                }
            }
        }
    }

    /**
     * Remove this device from the backend when logging out.
     */
    suspend fun unregisterDevice(app: TaskFlowApp) {
        val token = app.session.fcmToken ?: return

        safeCall {
            app.api.unregisterDevice(
                DeviceTokenRequest(token)
            )
        }

        app.session.fcmTokenSynced = null
        AppLog.d(TAG, "Device unregistered")
    }

    /**
     * Obtain Firebase Messaging only if Firebase
     * is correctly configured in the Android build.
     */
    private fun firebaseMessaging(): FirebaseMessaging? {
        return try {
            FirebaseMessaging.getInstance()
        } catch (e: IllegalStateException) {
            AppLog.w(
                TAG,
                "Firebase is not configured: push disabled"
            )
            null
        }
    }
}
