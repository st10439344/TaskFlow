package com.taskflow.app.util

import com.taskflow.app.data.local.TaskEntity
import java.util.TimeZone

/** Smart views, inspired by "My Day" in Microsoft To Do and Today/Upcoming in Todoist (see Part 1 research). */
enum class SmartFilter { ALL, TODAY, OVERDUE, UPCOMING, DONE }

enum class SortOrder { DUE, PRIORITY, RECENT, TITLE }

/** Search, smart filters and sorting for the Home screen. Pure logic, so it is unit-tested. */
object TaskFilter {

    fun apply(
        tasks: List<TaskEntity>,
        listId: String?,
        filter: SmartFilter,
        query: String,
        sort: SortOrder,
        today: String = DateUtils.todayString(),
        now: Long = System.currentTimeMillis(),
        zone: TimeZone = TimeZone.getDefault()
    ): List<TaskEntity> {
        val q = query.trim().lowercase()
        val matches = tasks.filter { t ->
            (listId == null || t.listId == listId) &&
                matchesFilter(t, filter, today, now, zone) &&
                (q.isEmpty() || t.title.lowercase().contains(q) || t.description.lowercase().contains(q))
        }
        return sorted(matches, sort)
    }

    private fun matchesFilter(t: TaskEntity, filter: SmartFilter, today: String, now: Long, zone: TimeZone) = when (filter) {
        SmartFilter.ALL -> true
        SmartFilter.TODAY -> !t.isComplete && t.dueDate == today
        SmartFilter.OVERDUE -> !t.isComplete && DateUtils.isOverdue(t.dueDate, t.dueTime, now, zone)
        SmartFilter.UPCOMING -> !t.isComplete && t.dueDate != null && t.dueDate > today
        SmartFilter.DONE -> t.isComplete
    }

    private fun priorityRank(p: String) = when (p) { "high" -> 0; "med" -> 1; else -> 2 }

    // Open tasks first; tasks without a due date go after dated ones; ties broken by most recently updated.
    private val byDue = compareBy<TaskEntity>({ it.isComplete }, { it.dueDate == null }, { it.dueDate ?: "" }, { it.dueTime ?: "99:99" })
        .thenByDescending { it.updatedAt }

    private fun sorted(list: List<TaskEntity>, sort: SortOrder): List<TaskEntity> = when (sort) {
        SortOrder.DUE -> list.sortedWith(byDue)
        SortOrder.PRIORITY -> list.sortedWith(compareBy<TaskEntity>({ it.isComplete }, { priorityRank(it.priority) }).then(byDue))
        SortOrder.RECENT -> list.sortedByDescending { it.updatedAt }
        SortOrder.TITLE -> list.sortedBy { it.title.lowercase() }
    }
}
