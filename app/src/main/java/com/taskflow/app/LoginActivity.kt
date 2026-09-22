package com.taskflow.app

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.taskflow.app.databinding.ActivityLoginBinding
import com.taskflow.app.util.Validators
import kotlinx.coroutines.launch

class LoginActivity : AuthBaseActivity() {
    private lateinit var b: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.btnLogin.setOnClickListener { login() }
        b.btnGoogle.setOnClickListener { startGoogleSignIn() }
        b.tvRegister.setOnClickListener { startActivity(Intent(this, RegisterActivity::class.java)) }
    }

    private fun login() {
        b.tilEmail.error = null
        b.tilPassword.error = null
        val email = b.etEmail.text.toString().trim()
        val password = b.etPassword.text.toString()

        var ok = true
        if (!Validators.isValidEmail(email)) { b.tilEmail.error = getString(R.string.err_email); ok = false }
        if (password.isEmpty()) { b.tilPassword.error = getString(R.string.err_password_required); ok = false }
        if (!ok) return

        lifecycleScope.launch {
            setBusy(true)
            handleResult(app.userRepo.login(email, password))
        }
    }

    override fun setBusy(busy: Boolean) {
        b.progress.visibility = if (busy) android.view.View.VISIBLE else android.view.View.GONE
        b.btnLogin.isEnabled = !busy
        b.btnGoogle.isEnabled = !busy
    }
}
