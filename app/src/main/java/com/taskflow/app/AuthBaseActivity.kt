package com.taskflow.app

import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.taskflow.app.data.remote.ApiResult
import com.taskflow.app.data.remote.AuthResponse
import com.taskflow.app.util.AppLog
import kotlinx.coroutines.launch

/** Shared behaviour for the Login and Register screens (Google sign-in, error display, navigation). */
abstract class AuthBaseActivity : AppCompatActivity() {
    protected val app get() = application as TaskFlowApp

    protected abstract fun setBusy(busy: Boolean)

    /** Lets each screen say which wording a 401 should get (wrong login vs. expired session). */
    protected open val errorContext: ErrorContext = ErrorContext.GENERAL

    protected fun startGoogleSignIn() {
        lifecycleScope.launch {
            setBusy(true)
            try {
                val idToken = GoogleAuth.getIdToken(this@AuthBaseActivity)
                handleResult(app.userRepo.google(idToken))
            } catch (e: GetCredentialCancellationException) {
                setBusy(false) // user closed the picker
            } catch (e: Exception) {
                setBusy(false)
                AppLog.w("Auth", "Google sign-in failed: ${e.javaClass.simpleName}")
                toast(R.string.google_failed)
            }
        }
    }

    protected suspend fun handleResult(result: ApiResult<AuthResponse>) {
        when (result) {
            is ApiResult.Success -> {
                app.taskRepo.sync() // pull lists and tasks so Home is ready
                goToMain()
            }
            is ApiResult.Error -> {
                setBusy(false)
                showError(result)
            }
        }
    }

    protected open fun showError(error: ApiResult.Error) {
        val text = error.localizedMessage(this, errorContext)
        Snackbar.make(findViewById(android.R.id.content), text, Snackbar.LENGTH_LONG).show()
    }
}
