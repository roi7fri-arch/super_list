package com.superlist.offline

class ReconnectReplayCoordinator(
    private val queueStore: PendingActionQueueStore,
) {
    fun replayOnReconnect(send: (PendingAction) -> Unit) {
        queueStore.dequeueAll().forEach(send)
    }
}
