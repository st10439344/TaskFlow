package com.taskflow.app

import android.content.Intent
import android.os.Bundle
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.taskflow.app.data.local.Subtask
import com.taskflow.app.data.local.TaskEntity
import com.taskflow.app.data.repo.SubtaskCodec
import com.taskflow.app.databinding.ActivityTaskDetailBinding
import kotlinx.coroutines.launch

class TaskDetailActivity : AppCompatActivity() {
    private lateinit var b: ActivityTaskDetailBinding
    private val app get() = application as TaskFlowApp
    private var listNames: Map<String, String> = emptyMap()
    private var current: TaskEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityTaskDetailBinding.inflate(layoutInflater)
        setContentView(b.root)
        b.toolbar.setNavigationOnClickListener { finish() }

        val id = intent.getStringExtra(EXTRA_TASK_ID)
        if (id == null) { finish(); return }

        b.btnEdit.setOnClickListener {
            current?.let { startActivity(Intent(this, TaskFormActivity::class.java).putExtra(TaskFormActivity.EXTRA_TASK_ID, it.localId)) }
        }
        b.btnDelete.setOnClickListener { confirmDelete() }
        b.btnComplete.setOnClickListener {
            current?.let { t -> lifecycleScope.launch { app.taskRepo.setComplete(t, !t.isComplete) } }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                listNames = app.taskRepo.getLists().associate { it.id to it.name }
                app.taskRepo.observeTask(id).collect { t ->
                    if (t == null || t.deleted) { finish(); return@collect }
                    current = t
                    render(t)
                }
            }
        }
    }

    private fun render(t: TaskEntity) {
        b.tvTitle.text = t.title
        b.tvList.text = getString(R.string.label_list, listNames[t.listId] ?: "-")
        b.tvDue.text = getString(R.string.label_due, listOfNotNull(t.dueDate, t.dueTime).joinToString(" ").ifEmpty { "-" })
        b.tvPriority.text = priorityLabel(this, t.priority)
        b.tvPriority.setTextColor(priorityColor(this, t.priority))
        b.tvSync.text = syncLabel(this, t)
        b.tvSync.setTextColor(getColor(if (t.syncStatus == "synced") R.color.priority_low else R.color.priority_med))
        b.tvDescription.text = t.description.ifBlank { getString(R.string.label_no_description) }
        b.btnComplete.setText(if (t.isComplete) R.string.btn_mark_incomplete else R.string.btn_mark_complete)

        val subs = SubtaskCodec.fromJson(t.subtasksJson)
        b.tvSubtasksLabel.text = getString(R.string.label_subtasks_progress, subs.count { it.isComplete }, subs.size)
        b.subtaskContainer.removeAllViews()
        subs.forEachIndexed { index, sub ->
            val cb = CheckBox(this).apply {
                text = sub.title
                isChecked = sub.isComplete
                setOnClickListener { toggleSubtask(t, subs, index, isChecked) }
            }
            b.subtaskContainer.addView(cb)
        }
    }

    private fun toggleSubtask(task: TaskEntity, subs: List<Subtask>, index: Int, done: Boolean) {
        val updated = subs.mapIndexed { i, s -> if (i == index) s.copy(isComplete = done) else s }
        lifecycleScope.launch { app.taskRepo.saveTask(task.copy(subtasksJson = SubtaskCodec.toJson(updated))) }
    }

    private fun confirmDelete() {
        val t = current ?: return
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_delete_task_title)
            .setMessage(R.string.dialog_delete_task_msg)
            .setNegativeButton(R.string.btn_cancel, null)
            .setPositiveButton(R.string.btn_delete) { _, _ -> lifecycleScope.launch { app.taskRepo.deleteTask(t) } }
            .show()
    }

    companion object {
        const val EXTRA_TASK_ID = "taskId"
    }
}
