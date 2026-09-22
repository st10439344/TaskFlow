package com.taskflow.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.taskflow.app.databinding.ActivitySplashBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Checks for a saved session: goes to Home if logged in, otherwise Login. */
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(ActivitySplashBinding.inflate(layoutInflater).root)
        lifecycleScope.launch {
            delay(900)
            if ((application as TaskFlowApp).session.isLoggedIn) goToMain() else goToLogin()
        }
    }
}
