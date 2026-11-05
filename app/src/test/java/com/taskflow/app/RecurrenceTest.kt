package com.taskflow.app

import com.taskflow.app.util.Recurrence
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class RecurrenceTest {
    private val today = LocalDate.of(2026, 10, 31)

    @Test fun dailyTaskDueTodayRepeatsTomorrow() {
        assertEquals("2026-11-01", Recurrence.nextDueDate("2026-10-31", "daily", today))
    }

    @Test fun lateDailyTaskSkipsToTheFirstFutureDay() {
        assertEquals("2026-11-01", Recurrence.nextDueDate("2026-10-20", "daily", today))
    }

    @Test fun weeklyTaskKeepsItsWeekday() {
        // Due Monday 26 Oct, completed Saturday 31 Oct -> next Monday 2 Nov
        assertEquals("2026-11-02", Recurrence.nextDueDate("2026-10-26", "weekly", today))
    }

    @Test fun taskCompletedEarlyRepeatsOneStepAfterItsDueDate() {
        assertEquals("2026-11-12", Recurrence.nextDueDate("2026-11-05", "weekly", today))
    }

    @Test fun monthlyClampsToTheEndOfShortMonths() {
        assertEquals("2026-02-28", Recurrence.nextDueDate("2026-01-31", "monthly", LocalDate.of(2026, 1, 31)))
        assertEquals("2028-02-29", Recurrence.nextDueDate("2028-01-31", "monthly", LocalDate.of(2028, 1, 31))) // leap year
    }

    @Test fun monthlyDoesNotDriftAfterAShortMonth() {
        // Jan 31 -> Feb 28 is not after 1 Mar, so the next one is Mar 31 (not Mar 28)
        assertEquals("2026-03-31", Recurrence.nextDueDate("2026-01-31", "monthly", LocalDate.of(2026, 3, 1)))
    }

    @Test fun nonRepeatingOrInvalidInputGivesNull() {
        assertNull(Recurrence.nextDueDate("2026-10-31", "none", today))
        assertNull(Recurrence.nextDueDate("2026-10-31", "yearly", today))
        assertNull(Recurrence.nextDueDate("not-a-date", "daily", today))
    }
}
