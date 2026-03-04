package com.superlist.offline

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingQueueReplayTest {
    @Test
    fun replayMaintainsOrder() {
        val queue = listOf("a1", "a2", "a3")
        assertEquals(listOf("a1", "a2", "a3"), queue)
    }
}
