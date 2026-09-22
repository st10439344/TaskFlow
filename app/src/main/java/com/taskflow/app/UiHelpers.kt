package com.taskflow.app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.text.InputType
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.taskflow.app.data.local.TaskEntity

fun Context.toast(@StringRes res: Int) = Toast.makeText(this, res, Toast.LENGTH_LONG).show()
fun Context.toast(text: String) = Toast.makeText(this, text, Toast.LENGTH_LONG).show()

fun Activity.goToLogin() {
    startActivity(Intent(this, LoginActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
    finish()
}

fun Activity.goToMain() {
    startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
    finish()
}

fun priorityLabel(context: Context, priority: String): String = context.getString(
    when (priority) {
        "low" -> R.string.priority_low
        "high" -> R.string.priority_high
        else -> R.string.priority_med
    }
)

fun priorityColor(context: Context, priority: String): Int = context.getColor(
    when (priority) {
        "low" -> R.color.priority_low
        "high" -> R.color.priority_high
        else -> R.color.priority_med
    }
)

fun syncLabel(context: Context, task: TaskEntity): String =
    context.getString(if (task.syncStatus == "synced") R.string.sync_synced else R.string.sync_pending)

/** Google Sign-In through Credential Manager. Returns the Google ID token, which our API verifies. */
object GoogleAuth {
    suspend fun getIdToken(activity: Activity): String {
        val option = GetGoogleIdOption.Builder()
            .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
            .setFilterByAuthorizedAccounts(false)
            .build()
        val request = GetCredentialRequest.Builder().addCredentialOption(option).build()
        val result = CredentialManager.create(activity).getCredential(activity, request)
        val credential = result.credential
        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            return GoogleIdTokenCredential.createFrom(credential.data).idToken
        }
        throw IllegalStateException("Unexpected credential type")
    }
}

object Dialogs {
    private fun padding(context: Context) = (20 * context.resources.displayMetrics.density).toInt()

    private fun field(context: Context, hint: String, password: Boolean): Pair<TextInputLayout, TextInputEditText> {
        val til = TextInputLayout(context).apply {
            this.hint = hint
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            if (password) endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
        }
        val et = TextInputEditText(til.context).apply {
            setSingleLine()
            inputType = if (password) InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            else InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        }
        til.addView(et)
        return til to et
    }

    private fun show(context: Context, title: String, content: android.view.View, onSubmit: () -> Unit) {
        val dialog: AlertDialog = MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setView(content)
            .setNegativeButton(R.string.btn_cancel, null)
            .setPositiveButton(R.string.btn_save, null)
            .create()
        // Override the click so the dialog stays open when validation fails.
        dialog.setOnShowListener {
            dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE).setOnClickListener { onSubmit() }
        }
        dialog.show()
        currentDialog = dialog
    }

    private var currentDialog: AlertDialog? = null

    /** [onSubmit] gets the text and a `done` callback: call done(null) to close, or done("message") to show an error. */
    fun input(context: Context, title: String, hint: String, initial: String, onSubmit: (String, (String?) -> Unit) -> Unit) {
        val (til, et) = field(context, hint, false)
        et.setText(initial)
        val frame = FrameLayout(context).apply { setPadding(padding(context), padding(context) / 2, padding(context), 0); addView(til) }
        show(context, title, frame) {
            til.error = null
            onSubmit(et.text.toString().trim()) { err -> if (err == null) currentDialog?.dismiss() else til.error = err }
        }
    }

    fun password(context: Context, title: String, onSubmit: (String, String, (String?) -> Unit) -> Unit) {
        val (tilCur, etCur) = field(context, context.getString(R.string.hint_current_password), true)
        val (tilNew, etNew) = field(context, context.getString(R.string.hint_new_password), true)
        (tilNew.layoutParams as LinearLayout.LayoutParams).topMargin = padding(context) / 2
        val box = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(padding(context), padding(context) / 2, padding(context), 0)
            addView(tilCur)
            addView(tilNew)
        }
        show(context, title, box) {
            tilCur.error = null
            tilNew.error = null
            onSubmit(etCur.text.toString(), etNew.text.toString()) { err -> if (err == null) currentDialog?.dismiss() else tilNew.error = err }
        }
    }
}
