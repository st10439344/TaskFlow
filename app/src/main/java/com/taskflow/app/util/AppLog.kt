package com.taskflow.app.util

import android.util.Log

/**
 * Small wrapper around android.util.Log (Android Developers, n.d.).
 * Every message carries the "TaskFlow/" prefix so the whole app can be followed in Logcat with one filter:
 *     tag:TaskFlow
 * Rule used throughout the app: never log passwords, JWTs, FCM tokens or e-mail addresses.
 */
object AppLog {
    private const val PREFIX = "TaskFlow/"

    fun d(tag: String, msg: String) { Log.d(PREFIX + tag, msg) }
    fun i(tag: String, msg: String) { Log.i(PREFIX + tag, msg) }
    fun w(tag: String, msg: String, t: Throwable? = null) { Log.w(PREFIX + tag, msg, t) }
    fun e(tag: String, msg: String, t: Throwable? = null) { Log.e(PREFIX + tag, msg, t) }
}
