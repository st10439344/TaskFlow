package com.taskflow.app.data.repo

import android.content.Context
import androidx.work.WorkManager
import com.taskflow.app.data.SessionManager
import com.taskflow.app.data.local.AppDatabase
import com.taskflow.app.data.remote.ApiResult
import com.taskflow.app.data.remote.ApiService
import com.taskflow.app.data.remote.AuthResponse
import com.taskflow.app.data.remote.GoogleRequest
import com.taskflow.app.data.remote.LoginRequest
import com.taskflow.app.data.remote.PasswordRequest
import com.taskflow.app.data.remote.RegisterRequest
import com.taskflow.app.data.remote.TestPushResponse
import com.taskflow.app.data.remote.safeCall
import com.taskflow.app.util.AppLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Authentication, settings and push-test calls. Passwords are hashed with bcrypt on the API, never stored here. */
class UserRepository(
    private val ctx: Context,
    private val api: ApiService,
    private val session: SessionManager,
    private val db: AppDatabase
) {
    suspend fun register(name: String, email: String, password: String) =
        store("register", safeCall { api.register(RegisterRequest(name.trim(), email.trim(), password)) })

    suspend fun login(email: String, password: String) =
        store("login", safeCall { api.login(LoginRequest(email.trim(), password)) })

    suspend fun google(idToken: String) = store("google", safeCall { api.google(GoogleRequest(idToken)) })

    private suspend fun store(kind: String, result: ApiResult<AuthResponse>): ApiResult<AuthResponse> {
        when (result) {
            is ApiResult.Success -> {
                AppLog.i(TAG, "$kind succeeded")
                // Make sure no data from a previous account is left behind.
                withContext(Dispatchers.IO) { db.clearAllTables() }
                session.saveLogin(result.data.user, result.data.token)
            }
            is ApiResult.Error -> AppLog.w(TAG, "$kind failed (code ${result.code})")
        }
        return result
    }

    /** Settings are saved on the device instantly; this sends them to the server (retried later if it fails). */
    suspend fun pushSettingsIfDirty() {
        if (!session.settingsDirty || !session.isLoggedIn) return
        val body: Map<String, Any> = mapOf(
            "fullName" to session.fullName,
            "preferredLanguage" to session.language,
            "theme" to session.theme,
            "notificationsEnabled" to session.notifications
        )
        if (safeCall { api.updateMe(body) } is ApiResult.Success) {
            session.settingsDirty = false
            AppLog.d(TAG, "Settings synced to the API")
        } else {
            AppLog.d(TAG, "Settings not synced yet, will retry")
        }
    }

    suspend fun changePassword(current: String, new: String): ApiResult<Unit> =
        when (val r = safeCall { api.changePassword(PasswordRequest(current, new)) }) {
            is ApiResult.Success -> ApiResult.Success(Unit)
            is ApiResult.Error -> r
        }

    /** Permanently deletes the account on the server (Google Play requires an in-app way to do this). */
    suspend fun deleteAccount(): ApiResult<Unit> =
        when (val r = safeCall { api.deleteAccount() }) {
            is ApiResult.Success -> { AppLog.i(TAG, "Account deleted"); ApiResult.Success(Unit) }
            is ApiResult.Error -> r
        }

    /** Asks the API to push a test notification to this user's devices (used to demonstrate real-time push). */
    suspend fun sendTestPush(): ApiResult<TestPushResponse> = safeCall { api.sendTestPush() }

    suspend fun logout() {
        AppLog.i(TAG, "Logging out")
        WorkManager.getInstance(ctx).cancelAllWork()
        session.clearUser()
        withContext(Dispatchers.IO) { db.clearAllTables() }
    }

    private companion object {
        const val TAG = "UserRepo"
    }
}
