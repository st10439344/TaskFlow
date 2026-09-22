package com.taskflow.app

import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.taskflow.app.data.remote.ApiResult
import com.taskflow.app.data.remote.AuthResponse
import kotlinx.coroutines.launch

/** Shared behaviour for the Login and Register screens (Google sign-in, error display, navigation). */
abstract class AuthBaseActivity : AppCompatActivity() {
    protected val app get() = application as TaskFlowApp

    protected abstract fun setBusy(busy: Boolean)

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
        val text = if (error.isNetwork) getString(R.string.err_no_internet) else error.message
        Snackbar.make(findViewById(android.R.id.content), text, Snackbar.LENGTH_LONG).show()
    }
}
