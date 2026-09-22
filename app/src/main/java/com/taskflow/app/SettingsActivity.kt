package com.taskflow.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.taskflow.app.data.remote.ApiResult
import com.taskflow.app.databinding.ActivitySettingsBinding
import com.taskflow.app.util.Validators
import kotlinx.coroutines.launch

/** Name, language, notifications, theme, password and logout. Every change is saved instantly on the device. */
class SettingsActivity : AppCompatActivity() {
    private lateinit var b: ActivitySettingsBinding
    private val app get() = application as TaskFlowApp
    private val session get() = app.session

    private val languageCodes = listOf("en", "zu", "af")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(b.root)
        b.toolbar.setNavigationOnClickListener { finish() }

        renderProfile()
        b.switchNotifications.isChecked = session.notifications
        b.switchDark.isChecked = session.theme == "dark"

        b.switchNotifications.setOnCheckedChangeListener { _, checked ->
            session.notifications = checked
            saveAndPush()
        }
        b.switchDark.setOnCheckedChangeListener { _, checked ->
            session.theme = if (checked) "dark" else "light"
            saveAndPush()
            AppCompatDelegate.setDefaultNightMode(if (checked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)
        }
        b.rowName.setOnClickListener { editName() }
        b.rowLanguage.setOnClickListener { chooseLanguage() }
        b.rowPassword.setOnClickListener { changePassword() }
        b.btnLogout.setOnClickListener { logout() }
    }

    private fun renderProfile() {
        val name = session.fullName
        b.tvName.text = name
        b.tvNameValue.text = name
        b.tvEmail.text = session.email
        b.tvInitials.text = name.trim().split(" ").filter { it.isNotEmpty() }.take(2)
            .joinToString("") { it.first().uppercase() }
        b.tvLanguageValue.text = languageLabel(session.language)
    }

    private fun languageLabel(code: String) = getString(
        when (code) {
            "zu" -> R.string.lang_zu
            "af" -> R.string.lang_af
            else -> R.string.lang_en
        }
    )

    /** Saved on the device first, then sent to the server in the app-wide scope (survives the screen being recreated). */
    private fun saveAndPush() {
        session.settingsDirty = true
        app.appScope.launch { app.userRepo.pushSettingsIfDirty() }
    }

    private fun editName() {
        Dialogs.input(this, getString(R.string.setting_name), getString(R.string.hint_full_name), session.fullName) { value, done ->
            if (!Validators.isValidName(value)) { done(getString(R.string.err_name)); return@input }
            session.fullName = value
            saveAndPush()
            renderProfile()
            done(null)
            Snackbar.make(b.root, R.string.msg_saved, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun chooseLanguage() {
        val labels = languageCodes.map { languageLabel(it) }.toTypedArray()
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.setting_language)
            .setSingleChoiceItems(labels, languageCodes.indexOf(session.language).coerceAtLeast(0)) { dialog, which ->
                dialog.dismiss()
                val code = languageCodes[which]
                if (code != session.language) {
                    session.language = code
                    saveAndPush()
                    // Applies immediately: Android recreates the visible screens in the new language.
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(code))
                }
            }
            .show()
    }

    private fun changePassword() {
        if (session.authProvider == "google") { toast(R.string.msg_password_google); return }
        Dialogs.password(this, getString(R.string.setting_password)) { current, new, done ->
            when {
                current.isEmpty() -> done(getString(R.string.err_password_required))
                !Validators.isValidPassword(new) -> done(getString(R.string.err_password_rules))
                else -> lifecycleScope.launch {
                    when (val r = app.userRepo.changePassword(current, new)) {
                        is ApiResult.Success -> {
                            done(null)
                            Snackbar.make(b.root, R.string.msg_password_updated, Snackbar.LENGTH_SHORT).show()
                        }
                        is ApiResult.Error -> done(if (r.isNetwork) getString(R.string.err_no_internet) else r.message)
                    }
                }
            }
        }
    }

    private fun logout() {
        lifecycleScope.launch {
            app.userRepo.logout()
            goToLogin()
        }
    }
}
