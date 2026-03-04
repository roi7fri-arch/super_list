package com.superlist.sync

class FallbackPollingCoordinator {
    var enabled: Boolean = false
        private set

    fun enable() {
        enabled = true
    }

    fun disable() {
        enabled = false
    }
}
