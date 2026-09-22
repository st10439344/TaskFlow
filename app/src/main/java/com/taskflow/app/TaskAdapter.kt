package com.taskflow.app

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.taskflow.app.data.local.SYNC_SYNCED
import com.taskflow.app.data.local.TaskEntity
import com.taskflow.app.data.repo.SubtaskCodec
import com.taskflow.app.databinding.ItemTaskBinding
import com.taskflow.app.util.DateUtils

class TaskAdapter(
    private val onClick: (TaskEntity) -> Unit,
    private val onToggle: (TaskEntity) -> Unit
) : ListAdapter<TaskEntity, TaskAdapter.VH>(DIFF) {

    class VH(val b: ItemTaskBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val t = getItem(position)
        val ctx = holder.b.root.context
        val b = holder.b

        b.tvTitle.text = t.title
        b.tvTitle.paintFlags =
            if (t.isComplete) b.tvTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            else b.tvTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        b.tvTitle.alpha = if (t.isComplete) 0.5f else 1f
        b.viewPriority.setBackgroundColor(priorityColor(ctx, t.priority))

        val parts = mutableListOf<String>()
        if (t.dueDate != null) parts += listOfNotNull(t.dueDate, t.dueTime).joinToString(" ")
        parts += priorityLabel(ctx, t.priority)
        val subs = SubtaskCodec.fromJson(t.subtasksJson)
        if (subs.isNotEmpty()) parts += "${subs.count { it.isComplete }}/${subs.size}"
        b.tvMeta.text = parts.joinToString(" • ")
        val overdue = !t.isComplete && DateUtils.isOverdue(t.dueDate, t.dueTime)
        b.tvMeta.setTextColor(ctx.getColor(if (overdue) R.color.danger else R.color.text_secondary))

        b.tvSync.text = syncLabel(ctx, t)
        b.tvSync.setTextColor(ctx.getColor(if (t.syncStatus == SYNC_SYNCED) R.color.priority_low else R.color.priority_med))

        b.cbDone.setOnCheckedChangeListener(null)
        b.cbDone.isChecked = t.isComplete
        b.cbDone.setOnClickListener { onToggle(t) }
        b.root.setOnClickListener { onClick(t) }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<TaskEntity>() {
            override fun areItemsTheSame(a: TaskEntity, b: TaskEntity) = a.localId == b.localId
            override fun areContentsTheSame(a: TaskEntity, b: TaskEntity) = a == b
        }
    }
}
