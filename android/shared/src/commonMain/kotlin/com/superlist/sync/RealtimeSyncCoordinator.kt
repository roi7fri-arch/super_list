package com.superlist.sync

class RealtimeSyncCoordinator {
    private val listeners = mutableListOf<(String) -> Unit>()

    fun subscribe(listener: (String) -> Unit) {
        listeners += listener
    }

    fun onEvent(payload: String) {
        listeners.forEach { it(payload) }
    }
}
