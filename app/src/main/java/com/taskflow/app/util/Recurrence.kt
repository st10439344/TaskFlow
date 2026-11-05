package com.taskflow.app.util

import java.time.LocalDate
import java.time.format.DateTimeParseException

/** Repeating tasks (daily / weekly / monthly). Pure logic, so it is unit-tested. */
object Recurrence {
    val RULES = listOf("none", "daily", "weekly", "monthly")
    private const val MAX_STEPS = 3660L

    /**
     * Due date (yyyy-MM-dd) of the next occurrence, always strictly AFTER [today].
     * Completing a daily task that was due last week therefore gives tomorrow, not a date in the past.
     * Each date is calculated from the original due date (not from the previous result), so a monthly task
     * due on the 31st returns to the 31st after a short month instead of drifting to the 28th.
     * Returns null when the task does not repeat or the date is invalid.
     */
    fun nextDueDate(dueDate: String, rule: String, today: LocalDate = LocalDate.now()): String? {
        val start = try { LocalDate.parse(dueDate) } catch (e: DateTimeParseException) { return null }
        fun step(n: Long): LocalDate? = when (rule) {
            "daily" -> start.plusDays(n)
            "weekly" -> start.plusWeeks(n)
            "monthly" -> start.plusMonths(n)   // plusMonths clamps to the last day of short months
            else -> null
        }
        var n = 1L
        var next = step(n) ?: return null
        while (!next.isAfter(today) && n < MAX_STEPS) {
            n++
            next = step(n) ?: return null
        }
        return if (next.isAfter(today)) next.toString() else today.plusDays(1).toString()
    }
}
