package com.superlist.voice

object HoldToTalkGuard {
    private const val ACCIDENTAL_TAP_THRESHOLD_MS: Long = 300

    fun isValidHold(durationMs: Long): Boolean = durationMs >= ACCIDENTAL_TAP_THRESHOLD_MS
}
