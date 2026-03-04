package com.superlist.voice

enum class MicPermissionState {
    GRANTED,
    DENIED,
    UNKNOWN,
}

object MicPermissionCoordinator {
    fun canStartCapture(state: MicPermissionState): Boolean = state == MicPermissionState.GRANTED

    fun guidanceMessage(state: MicPermissionState): String = when (state) {
        MicPermissionState.GRANTED -> ""
        MicPermissionState.DENIED -> "נא לאפשר גישה למיקרופון בהגדרות"
        MicPermissionState.UNKNOWN -> "נדרשת הרשאת מיקרופון"
    }
}
