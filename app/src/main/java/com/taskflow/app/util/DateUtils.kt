package com.taskflow.app.util

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateUtils {
    private const val DEFAULT_TIME = "09:00"

    /** ISO-8601 UTC timestamp, the format the API expects for `updatedAt`. */
    fun toIso(millis: Long): String {
        val f = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        f.timeZone = TimeZone.getTimeZone("UTC")
        return f.format(Date(millis))
    }

    fun formatDate(year: Int, monthZeroBased: Int, day: Int): String =
        String.format(Locale.US, "%04d-%02d-%02d", year, monthZeroBased + 1, day)

    fun formatTime(hour: Int, minute: Int): String = String.format(Locale.US, "%02d:%02d", hour, minute)

    /** Due moment in millis (device time zone), or null if the date cannot be parsed. */
    fun dueMillis(date: String, time: String?, zone: TimeZone = TimeZone.getDefault()): Long? {
        return try {
            val f = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
            f.timeZone = zone
            f.isLenient = false
            f.parse("$date ${time ?: DEFAULT_TIME}")?.time
        } catch (e: ParseException) {
            null
        }
    }

    /** A task with a date but no time counts as overdue only after the end of that day. */
    fun isOverdue(
        date: String?,
        time: String?,
        now: Long = System.currentTimeMillis(),
        zone: TimeZone = TimeZone.getDefault()
    ): Boolean {
        if (date == null) return false
        val due = dueMillis(date, time ?: "23:59", zone) ?: return false
        return due < now
    }
}
