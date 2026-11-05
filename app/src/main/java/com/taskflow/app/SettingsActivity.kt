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
import com.taskflow.app.push.PushManager
import com.taskflow.app.util.AppLog
import com.taskflow.app.util.Validators
import kotlinx.coroutines.launch

/**
 * Name, language, notifications, theme, password and logout.
 * Every change is saved instantly on the device and sent to the API in the background.
 */
class SettingsActivity : AppCompatActivity() {
    private lateinit var b: ActivitySettingsBinding
    private val app get() = application as TaskFlowApp
    private val session get() = app.session

    private val languageCodes = listOf("en", "zu", "af")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        useEdgeToEdge()
        b = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(b.root)
        applyInsets(b.root, header = b.toolbar, bottomView = b.scroll)
        b.toolbar.setNavigationOnClickListener { finish() }

        renderProfile()
        b.switchNotifications.isChecked = session.notifications
        b.switchDark.isChecked = session.theme == "dark"

        b.switchNotifications.setOnCheckedChangeListener { _, checked ->
            AppLog.i(TAG, "Notifications switched ${if (checked) "on" else "off"}")
            session.notifications = checked
            saveAndPush()
        }
        b.switchDark.setOnCheckedChangeListener { _, checked ->
            AppLog.i(TAG, "Dark theme ${if (checked) "on" else "off"}")
            session.theme = if (checked) "dark" else "light"
            saveAndPush()
            AppCompatDelegate.setDefaultNightMode(if (checked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)
        }
        b.rowName.setOnClickListener { editName() }
        b.rowLanguage.setOnClickListener { chooseLanguage() }
        b.rowTestPush.setOnClickListener { sendTestNotification() }
        b.rowPassword.setOnClickListener { changePassword() }
        b.rowDeleteAccount.setOnClickListener { confirmDeleteAccount() }
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
                    AppLog.i(TAG, "Language changed to $code")
                    session.language = code
                    saveAndPush()
                    app.ensureNotificationChannels() // channel names follow the new language
                    // Applies immediately: Android recreates the visible screens in the new language.
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(code))
                }
            }
            .show()
    }

    /** Asks the API to push a notification to this phone, which proves the whole Firebase pipeline works. */
    private fun sendTestNotification() {
        if (!app.network.isOnlineNow()) {
            Snackbar.make(b.root, R.string.err_no_internet, Snackbar.LENGTH_LONG).show()
            return
        }
        lifecycleScope.launch {
            PushManager.registerDevice(app)
            val message = when (val r = app.userRepo.sendTestPush()) {
                is ApiResult.Success -> getString(R.string.push_test_sent)
                is ApiResult.Error -> when (r.code) {
                    503 -> getString(R.string.push_not_configured)
                    400 -> getString(R.string.push_no_device)
                    else -> r.localizedMessage(this@SettingsActivity)
                }
            }
            Snackbar.make(b.root, message, Snackbar.LENGTH_LONG).show()
        }
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
                        is ApiResult.Error -> done(r.localizedMessage(this@SettingsActivity, ErrorContext.PASSWORD))
                    }
                }
            }
        }
    }

    /** Account deletion (required by Google Play): removes the account, lists and tasks from the server and this phone. */
    private fun confirmDeleteAccount() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_delete_account_title)
            .setMessage(R.string.dialog_delete_account_msg)
            .setNegativeButton(R.string.btn_cancel, null)
            .setPositiveButton(R.string.btn_delete) { _, _ ->
                lifecycleScope.launch {
                    when (val r = app.userRepo.deleteAccount()) {
                        is ApiResult.Success -> {
                            app.userRepo.logout()
                            toast(R.string.msg_account_deleted)
                            goToLogin()
                        }
                        is ApiResult.Error -> Snackbar.make(b.root, r.localizedMessage(this@SettingsActivity), Snackbar.LENGTH_LONG).show()
                    }
                }
            }
            .show()
    }

    private fun logout() {
        lifecycleScope.launch {
            PushManager.unregisterDevice(app) // stop alerts for this account on this phone (needs the token, so do it first)
            app.userRepo.logout()
            goToLogin()
        }
    }

    private companion object {
        const val TAG = "Settings"
    }
}
