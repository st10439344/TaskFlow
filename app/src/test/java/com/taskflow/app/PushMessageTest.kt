package com.taskflow.app

import com.taskflow.app.push.PushMessage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PushMessageTest {
    @Test fun streakMessageCarriesTheDayCount() {
        assertEquals(PushMessage.Streak(7), PushMessage.parse(mapOf("type" to "streak", "count" to "7")))
    }

    @Test fun badStreakCountFallsBackToZero() {
        assertEquals(PushMessage.Streak(0), PushMessage.parse(mapOf("type" to "streak", "count" to "abc")))
    }

    @Test fun otherTypesAreRecognised() {
        assertTrue(PushMessage.parse(mapOf("type" to "all_done")) === PushMessage.AllDone)
        assertTrue(PushMessage.parse(mapOf("type" to "test")) === PushMessage.Test)
        assertTrue(PushMessage.parse(mapOf("type" to "sync")) === PushMessage.Sync)
    }

    @Test fun unknownOrMissingTypeIsIgnored() {
        assertTrue(PushMessage.parse(mapOf("type" to "hello")) === PushMessage.Unknown)
        assertTrue(PushMessage.parse(emptyMap()) === PushMessage.Unknown)
    }
}
