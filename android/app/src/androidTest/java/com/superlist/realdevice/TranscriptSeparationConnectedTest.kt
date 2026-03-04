package com.superlist.realdevice

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.superlist.parseTranscriptItems
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TranscriptSeparationConnectedTest {
    @Test
    fun separates_reported_phrase_on_connected_device() {
        val items = parseTranscriptItems("שני חלב וגם לחם")

        assertEquals(2, items.size)
        assertEquals("חלב", items[0].name)
        assertEquals(2, items[0].quantity)
        assertEquals("לחם", items[1].name)
        assertEquals(1, items[1].quantity)
    }

    @Test
    fun separates_standalone_vav_and_vegam_on_connected_device() {
        val items = parseTranscriptItems("לחם ו חלב וגם שניצל")

        assertEquals(3, items.size)
        assertEquals("לחם", items[0].name)
        assertEquals("חלב", items[1].name)
        assertEquals("שניצל", items[2].name)
    }

    @Test
    fun separates_prefixed_vav_with_quantity_phrase_on_connected_device() {
        val items = parseTranscriptItems("שני לחם וגבינה אחת ושלוש מוצרלה")

        assertEquals(3, items.size)
        assertEquals("לחם", items[0].name)
        assertEquals(2, items[0].quantity)
        assertEquals("גבינה", items[1].name)
        assertEquals(1, items[1].quantity)
        assertEquals("מוצרלה", items[2].name)
        assertEquals(3, items[2].quantity)
    }
}
