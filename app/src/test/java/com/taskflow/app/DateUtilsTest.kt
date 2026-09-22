package com.taskflow.app

import com.taskflow.app.util.DateUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.TimeZone

class DateUtilsTest {
    private val utc = TimeZone.getTimeZone("UTC")

    @Test fun isoFormatIsUtc() {
        assertEquals("1970-01-01T00:00:00.000Z", DateUtils.toIso(0L))
        assertEquals("2026-08-24T16:00:00.000Z", DateUtils.toIso(1787587200000L))
    }

    @Test fun formatsDateAndTimeWithPadding() {
        assertEquals("2026-08-05", DateUtils.formatDate(2026, 7, 5)) // month is zero-based
        assertEquals("09:05", DateUtils.formatTime(9, 5))
    }

    @Test fun dueMillisUsesGivenTimeOrDefaultsToNine() {
        assertEquals(1787587200000L, DateUtils.dueMillis("2026-08-24", "16:00", utc))
        assertEquals(1787587200000L - 7 * 3600_000L, DateUtils.dueMillis("2026-08-24", null, utc))
    }

    @Test fun invalidDatesReturnNull() {
        assertNull(DateUtils.dueMillis("2026-13-45", "10:00", utc))
        assertNull(DateUtils.dueMillis("not a date", null, utc))
    }

    @Test fun overdueLogic() {
        val now = 1787587200000L // 2026-08-24 16:00 UTC
        assertTrue(DateUtils.isOverdue("2026-08-23", null, now, utc))
        assertTrue(DateUtils.isOverdue("2026-08-24", "15:00", now, utc))
        assertFalse(DateUtils.isOverdue("2026-08-24", "17:00", now, utc))
        assertFalse(DateUtils.isOverdue("2026-08-24", null, now, utc)) // date only: overdue after 23:59
        assertFalse(DateUtils.isOverdue(null, null, now, utc))
    }
}
