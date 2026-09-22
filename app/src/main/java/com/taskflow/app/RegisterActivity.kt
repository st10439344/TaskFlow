package com.taskflow.app

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.taskflow.app.data.remote.ApiResult
import com.taskflow.app.databinding.ActivityRegisterBinding
import com.taskflow.app.util.Validators
import kotlinx.coroutines.launch

class RegisterActivity : AuthBaseActivity() {
    private lateinit var b: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.toolbar.setNavigationOnClickListener { finish() }
        b.btnRegister.setOnClickListener { register() }
        b.btnGoogle.setOnClickListener { startGoogleSignIn() }
        b.tvLogin.setOnClickListener { finish() }
    }

    private fun register() {
        listOf(b.tilName, b.tilEmail, b.tilPassword, b.tilConfirm).forEach { it.error = null }
        val name = b.etName.text.toString().trim()
        val email = b.etEmail.text.toString().trim()
        val password = b.etPassword.text.toString()
        val confirm = b.etConfirm.text.toString()

        // Validated on the device first; the API validates again. confirmPassword is never sent or stored.
        var ok = true
        if (!Validators.isValidName(name)) { b.tilName.error = getString(R.string.err_name); ok = false }
        if (!Validators.isValidEmail(email)) { b.tilEmail.error = getString(R.string.err_email); ok = false }
        if (!Validators.isValidPassword(password)) { b.tilPassword.error = getString(R.string.err_password_rules); ok = false }
        if (!Validators.passwordsMatch(password, confirm)) { b.tilConfirm.error = getString(R.string.err_password_match); ok = false }
        if (!ok) return

        lifecycleScope.launch {
            setBusy(true)
            handleResult(app.userRepo.register(name, email, password))
        }
    }

    override fun showError(error: ApiResult.Error) {
        // Show server-side field errors next to the fields when there are any.
        error.fieldErrors["fullName"]?.let { b.tilName.error = it }
        error.fieldErrors["email"]?.let { b.tilEmail.error = it }
        error.fieldErrors["password"]?.let { b.tilPassword.error = it }
        if (error.fieldErrors.isEmpty()) {
            if (error.code == 409) b.tilEmail.error = error.message
            super.showError(error)
        }
    }

    override fun setBusy(busy: Boolean) {
        b.progress.visibility = if (busy) android.view.View.VISIBLE else android.view.View.GONE
        b.btnRegister.isEnabled = !busy
        b.btnGoogle.isEnabled = !busy
    }
}
