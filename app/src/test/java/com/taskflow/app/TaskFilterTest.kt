package com.taskflow.app

import com.taskflow.app.data.local.TaskEntity
import com.taskflow.app.util.SmartFilter
import com.taskflow.app.util.SortOrder
import com.taskflow.app.util.TaskFilter
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.util.TimeZone

class TaskFilterTest {
    private val utc = TimeZone.getTimeZone("UTC")
    private val today = "2026-10-31"
    private val now = Instant.parse("2026-10-31T10:00:00Z").toEpochMilli()

    private fun task(id: String, title: String, due: String?, done: Boolean = false, priority: String = "med",
                     listId: String = "work", desc: String = "", updated: Long = 0L, time: String? = null) =
        TaskEntity(localId = id, listId = listId, title = title, description = desc, dueDate = due, dueTime = time,
            priority = priority, isComplete = done, updatedAt = updated)

    private val tasks = listOf(
        task("a", "Submit assignment", "2026-10-31", priority = "high", updated = 5),
        task("b", "Pay rent", "2026-10-30", updated = 4),                         // overdue
        task("c", "Buy milk", "2026-11-02", priority = "low", listId = "home", updated = 3),
        task("d", "Read chapter 4", null, desc = "for the exam", updated = 2),
        task("e", "Old task", "2026-10-31", done = true, updated = 1)
    )

    private fun ids(list: List<TaskEntity>) = list.map { it.localId }
    private fun run(filter: SmartFilter = SmartFilter.ALL, q: String = "", sort: SortOrder = SortOrder.DUE, list: String? = null) =
        ids(TaskFilter.apply(tasks, list, filter, q, sort, today, now, utc))

    @Test fun smartFilters() {
        assertEquals(listOf("a"), run(SmartFilter.TODAY))
        assertEquals(listOf("b"), run(SmartFilter.OVERDUE))
        assertEquals(listOf("c"), run(SmartFilter.UPCOMING))
        assertEquals(listOf("e"), run(SmartFilter.DONE))
        assertEquals(5, run(SmartFilter.ALL).size)
    }

    @Test fun searchIsCaseInsensitiveAndChecksTitleAndDescription() {
        assertEquals(listOf("c"), run(q = "MILK"))
        assertEquals(listOf("d"), run(q = "exam"))
        assertEquals(emptyList<String>(), run(q = "zzz"))
        assertEquals(5, run(q = "   ").size)
    }

    @Test fun listFilterAndSearchCombine() {
        assertEquals(listOf("c"), run(list = "home"))
        assertEquals(emptyList<String>(), run(q = "rent", list = "home"))
    }

    @Test fun dueSortPutsDatedTasksFirstThenUndatedThenCompleted() {
        assertEquals(listOf("b", "a", "c", "d", "e"), run(sort = SortOrder.DUE))
    }

    @Test fun prioritySortPutsHighFirstAndCompletedLast() {
        assertEquals(listOf("a", "b", "d", "c", "e"), run(sort = SortOrder.PRIORITY))
    }

    @Test fun recentAndTitleSorts() {
        assertEquals(listOf("a", "b", "c", "d", "e"), run(sort = SortOrder.RECENT))
        // Buy milk, Old task, Pay rent, Read chapter 4, Submit assignment
        assertEquals(listOf("c", "e", "b", "d", "a"), run(sort = SortOrder.TITLE))
    }
}
