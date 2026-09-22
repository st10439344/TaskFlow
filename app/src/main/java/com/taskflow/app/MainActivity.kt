package com.taskflow.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.taskflow.app.data.local.ListEntity
import com.taskflow.app.data.remote.ApiResult
import com.taskflow.app.databinding.ActivityMainBinding
import com.taskflow.app.work.SyncWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/** Home / task list screen. */
class MainActivity : AppCompatActivity() {
    private lateinit var b: ActivityMainBinding
    private val app get() = application as TaskFlowApp
    private val selectedList = MutableStateFlow<String?>(null) // null = All
    private var lists: List<ListEntity> = emptyList()

    private lateinit var adapter: TaskAdapter

    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* reminders simply won't show if denied */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!app.session.isLoggedIn) { goToLogin(); return }
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        restoreLanguageIfNeeded()
        askNotificationPermission()

        b.toolbar.inflateMenu(R.menu.main_menu)
        b.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_settings -> startActivity(Intent(this, SettingsActivity::class.java))
                R.id.action_new_list -> promptNewList()
            }
            true
        }

        adapter = TaskAdapter(
            onClick = { startActivity(Intent(this, TaskDetailActivity::class.java).putExtra(TaskDetailActivity.EXTRA_TASK_ID, it.localId)) },
            onToggle = { task -> lifecycleScope.launch { app.taskRepo.setComplete(task, !task.isComplete) } }
        )
        b.rvTasks.layoutManager = LinearLayoutManager(this)
        b.rvTasks.adapter = adapter

        b.fabAdd.setOnClickListener {
            if (lists.isEmpty()) toast(R.string.err_generic) else startActivity(Intent(this, TaskFormActivity::class.java))
        }

        observe()
    }

    override fun onResume() {
        super.onResume()
        if (!::b.isInitialized) return
        b.toolbar.subtitle = getString(R.string.home_greeting, app.session.fullName)
        b.toolbar.setSubtitleTextColor(getColor(R.color.white))
        SyncWorker.enqueue(this) // refresh whenever the screen comes back
    }

    private fun observe() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    app.taskRepo.observeLists().collect { lists = it; rebuildChips() }
                }
                launch {
                    combine(app.taskRepo.observeTasks(), selectedList) { tasks, sel ->
                        if (sel == null) tasks else tasks.filter { it.listId == sel }
                    }.collect {
                        adapter.submitList(it)
                        b.tvEmpty.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
                launch {
                    combine(app.network.online, app.taskRepo.observePendingCount()) { online, pending -> online to pending }
                        .collect { (online, pending) ->
                            when {
                                !online -> { b.tvBanner.setText(R.string.offline_banner); b.tvBanner.visibility = View.VISIBLE }
                                pending > 0 -> { b.tvBanner.setText(R.string.syncing_banner); b.tvBanner.visibility = View.VISIBLE }
                                else -> b.tvBanner.visibility = View.GONE
                            }
                        }
                }
                launch {
                    app.network.online.collect { if (it) SyncWorker.enqueue(this@MainActivity) } // sync on reconnect
                }
                launch {
                    app.session.stats.collect { b.tvStats.text = getString(R.string.home_stats, it.streak, it.weekly) }
                }
                launch {
                    app.session.sessionExpired.collect {
                        app.userRepo.logout()
                        toast(R.string.msg_session_expired)
                        goToLogin()
                    }
                }
            }
        }
    }

    private fun rebuildChips() {
        b.chipGroup.removeAllViews()
        addChip(getString(R.string.chip_all), null, null)
        lists.forEach { addChip(it.name, it.id, it) }
        val sel = selectedList.value
        val target = (0 until b.chipGroup.childCount).map { b.chipGroup.getChildAt(it) as Chip }
            .firstOrNull { it.tag == sel } ?: (b.chipGroup.getChildAt(0) as Chip).also { selectedList.value = null }
        target.isChecked = true
    }

    private fun addChip(text: String, id: String?, list: ListEntity?) {
        val chip = layoutInflater.inflate(R.layout.chip_filter, b.chipGroup, false) as Chip
        chip.text = text
        chip.tag = id
        chip.id = View.generateViewId()
        chip.setOnClickListener { selectedList.value = id }
        if (list != null) chip.setOnLongClickListener { showListOptions(list); true }
        b.chipGroup.addView(chip)
    }

    private fun showListOptions(list: ListEntity) {
        MaterialAlertDialogBuilder(this)
            .setTitle(list.name)
            .setItems(arrayOf(getString(R.string.dialog_rename_list), getString(R.string.btn_delete))) { _, which ->
                if (!app.network.isOnlineNow()) { toast(R.string.msg_lists_need_internet); return@setItems }
                if (which == 0) promptRename(list) else confirmDeleteList(list)
            }
            .show()
    }

    private fun promptNewList() {
        if (!app.network.isOnlineNow()) { toast(R.string.msg_lists_need_internet); return }
        Dialogs.input(this, getString(R.string.dialog_new_list), getString(R.string.hint_list_name), "") { name, done ->
            if (name.isBlank()) { done(getString(R.string.err_required)); return@input }
            lifecycleScope.launch {
                when (val r = app.taskRepo.createList(name)) {
                    is ApiResult.Success -> done(null)
                    is ApiResult.Error -> done(if (r.isNetwork) getString(R.string.err_no_internet) else r.message)
                }
            }
        }
    }

    private fun promptRename(list: ListEntity) {
        Dialogs.input(this, getString(R.string.dialog_rename_list), getString(R.string.hint_list_name), list.name) { name, done ->
            if (name.isBlank()) { done(getString(R.string.err_required)); return@input }
            lifecycleScope.launch {
                when (val r = app.taskRepo.renameList(list.id, name)) {
                    is ApiResult.Success -> done(null)
                    is ApiResult.Error -> done(if (r.isNetwork) getString(R.string.err_no_internet) else r.message)
                }
            }
        }
    }

    private fun confirmDeleteList(list: ListEntity) {
        MaterialAlertDialogBuilder(this)
            .setTitle(list.name)
            .setMessage(R.string.dialog_delete_list_msg)
            .setNegativeButton(R.string.btn_cancel, null)
            .setPositiveButton(R.string.btn_delete) { _, _ ->
                lifecycleScope.launch {
                    when (val r = app.taskRepo.deleteList(list.id)) {
                        is ApiResult.Success -> { if (selectedList.value == list.id) selectedList.value = null }
                        is ApiResult.Error -> toast(if (r.isNetwork) getString(R.string.err_no_internet) else r.message)
                    }
                }
            }
            .show()
    }

    /** On a fresh install/new device the saved language comes from the server; apply it once. */
    private fun restoreLanguageIfNeeded() {
        val saved = app.session.language
        val current = AppCompatDelegate.getApplicationLocales().toLanguageTags()
        if (saved != "en" && current.isEmpty()) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(saved))
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
