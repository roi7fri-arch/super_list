package com.superlist

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TranscriptSeparationTest {

    @Test
    fun separates_reported_phrase_two_milk_and_bread() {
        val items = parseTranscriptItems("שני חלב וגם לחם")

        assertEquals(2, items.size)
        assertEquals("חלב", items[0].name)
        assertEquals(2, items[0].quantity)
        assertEquals("לחם", items[1].name)
        assertEquals(1, items[1].quantity)
    }

    @Test
    fun separates_items_with_vegam_and_quantity() {
        val items = parseTranscriptItems("חלב וגם לחם וגם 3 עגבניות")

        assertEquals(3, items.size)
        assertEquals("חלב", items[0].name)
        assertEquals(1, items[0].quantity)
        assertEquals("לחם", items[1].name)
        assertEquals(1, items[1].quantity)
        assertEquals("עגבניות", items[2].name)
        assertEquals(3, items[2].quantity)
    }

    @Test
    fun separates_items_with_prefixed_vav_connector() {
        val items = parseTranscriptItems("חלב ולחם ועגבניות")

        assertEquals(3, items.size)
        assertEquals("חלב", items[0].name)
        assertEquals("לחם", items[1].name)
        assertEquals("עגבניות", items[2].name)
    }

    @Test
    fun separates_items_with_standalone_vav_connector_and_vegam() {
        val items = parseTranscriptItems("לחם ו חלב וגם שניצל")

        assertEquals(3, items.size)
        assertEquals("לחם", items[0].name)
        assertEquals(1, items[0].quantity)
        assertEquals("חלב", items[1].name)
        assertEquals(1, items[1].quantity)
        assertEquals("שניצל", items[2].name)
        assertEquals(1, items[2].quantity)
    }

    @Test
    fun separates_items_with_prefixed_vav_and_following_quantity_phrase() {
        val items = parseTranscriptItems("שני לחם וגבינה אחת ושלוש מוצרלה")

        assertEquals(3, items.size)
        assertEquals("לחם", items[0].name)
        assertEquals(2, items[0].quantity)
        assertEquals("גבינה", items[1].name)
        assertEquals(1, items[1].quantity)
        assertEquals("מוצרלה", items[2].name)
        assertEquals(3, items[2].quantity)
    }

    @Test
    fun separates_items_with_commas_and_az() {
        val items = parseTranscriptItems("ביצים, גבינה, ואז מלפפונים")

        assertEquals(3, items.size)
        assertEquals("ביצים", items[0].name)
        assertEquals("גבינה", items[1].name)
        assertEquals("מלפפונים", items[2].name)
        assertTrue(items.all { it.quantity >= 1 })
    }
}
