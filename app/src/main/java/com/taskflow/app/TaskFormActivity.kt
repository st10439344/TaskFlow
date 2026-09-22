package com.taskflow.app

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.taskflow.app.data.local.ListEntity
import com.taskflow.app.data.local.Subtask
import com.taskflow.app.data.local.TaskEntity
import com.taskflow.app.data.repo.SubtaskCodec
import com.taskflow.app.databinding.ActivityTaskFormBinding
import com.taskflow.app.databinding.ItemSubtaskFormBinding
import com.taskflow.app.util.DateUtils
import com.taskflow.app.util.Validators
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID

/** Add / Edit task screen. Pass EXTRA_TASK_ID to edit. Saves to Room first, so it works offline. */
class TaskFormActivity : AppCompatActivity() {
    private lateinit var b: ActivityTaskFormBinding
    private val app get() = application as TaskFlowApp

    private val priorityCodes = listOf("low", "med", "high")
    private var lists: List<ListEntity> = emptyList()
    private var existing: TaskEntity? = null
    private var selectedListId: String? = null
    private var priorityIdx = 1
    private var date: String? = null
    private var time: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityTaskFormBinding.inflate(layoutInflater)
        setContentView(b.root)
        b.toolbar.setNavigationOnClickListener { finish() }

        val editingId = intent.getStringExtra(EXTRA_TASK_ID)
        lifecycleScope.launch {
            lists = app.taskRepo.getLists()
            existing = editingId?.let { app.taskRepo.getTask(it) }
            if (lists.isEmpty()) { toast(R.string.err_generic); finish(); return@launch }
            setup(true)
        }
    }

    private fun setup(firstTime: Boolean) {
        val task = existing
        if (task != null) b.toolbar.setTitle(R.string.title_edit_task)

        // List dropdown
        selectedListId = task?.listId ?: lists.first().id
        b.acList.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, lists.map { it.name }))
        b.acList.setText(lists.firstOrNull { it.id == selectedListId }?.name ?: lists.first().name, false)
        b.acList.setOnItemClickListener { _, _, pos, _ -> selectedListId = lists[pos].id }

        // Priority dropdown
        val labels = priorityCodes.map { priorityLabel(this, it) }
        priorityIdx = priorityCodes.indexOf(task?.priority ?: "med").coerceAtLeast(0)
        b.acPriority.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, labels))
        b.acPriority.setText(labels[priorityIdx], false)
        b.acPriority.setOnItemClickListener { _, _, pos, _ -> priorityIdx = pos }

        if (firstTime) {
            b.etTitle.setText(task?.title ?: "")
            b.etDescription.setText(task?.description ?: "")
            b.switchRemind.isChecked = task?.remind ?: false
            date = task?.dueDate
            time = task?.dueTime
            SubtaskCodec.fromJson(task?.subtasksJson).forEach { addSubtaskRow(it.title, it.isComplete) }
        }
        showDateTime()

        b.etDate.setOnClickListener { pickDate() }
        b.etTime.setOnClickListener { pickTime() }
        b.btnClearDate.setOnClickListener { date = null; time = null; showDateTime() }
        b.btnAddSubtask.setOnClickListener { addSubtaskRow() }
        b.btnSave.setOnClickListener { save() }
    }

    private fun showDateTime() {
        b.etDate.setText(date ?: "")
        b.etTime.setText(time ?: "")
    }

    private fun pickDate() {
        val cal = Calendar.getInstance()
        DatePickerDialog(this, { _, y, m, d ->
            date = DateUtils.formatDate(y, m, d)
            showDateTime()
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun pickTime() {
        if (date == null) { pickDate(); return } // a time needs a date first
        TimePickerDialog(this, { _, h, min ->
            time = DateUtils.formatTime(h, min)
            showDateTime()
        }, 9, 0, true).show()
    }

    private fun addSubtaskRow(title: String = "", done: Boolean = false) {
        val row = ItemSubtaskFormBinding.inflate(layoutInflater, b.subtaskContainer, false)
        row.etSub.setText(title)
        row.cbSub.isChecked = done
        row.btnRemove.setOnClickListener { b.subtaskContainer.removeView(row.root) }
        b.subtaskContainer.addView(row.root)
    }

    private fun collectSubtasks(): List<Subtask> =
        (0 until b.subtaskContainer.childCount).mapNotNull { i ->
            val row = ItemSubtaskFormBinding.bind(b.subtaskContainer.getChildAt(i))
            val text = row.etSub.text.toString().trim()
            if (text.isEmpty()) null else Subtask(text, row.cbSub.isChecked)
        }

    private fun save() {
        b.tilTitle.error = null
        val title = b.etTitle.text.toString().trim()
        if (!Validators.isValidTitle(title)) { b.tilTitle.error = getString(R.string.err_title); return }
        val listId = selectedListId ?: return

        val base = existing ?: TaskEntity(localId = UUID.randomUUID().toString(), listId = listId, title = title)
        val task = base.copy(
            title = title,
            description = b.etDescription.text.toString().trim(),
            listId = listId,
            dueDate = date,
            dueTime = if (date == null) null else time,
            priority = priorityCodes[priorityIdx],
            remind = b.switchRemind.isChecked,
            subtasksJson = SubtaskCodec.toJson(collectSubtasks())
        )
        b.btnSave.isEnabled = false
        lifecycleScope.launch {
            app.taskRepo.saveTask(task)
            finish()
        }
    }

    companion object {
        const val EXTRA_TASK_ID = "taskId"
    }
}
