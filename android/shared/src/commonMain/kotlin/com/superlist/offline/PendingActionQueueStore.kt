package com.superlist.offline

data class PendingAction(
    val clientActionId: String,
    val payload: String,
)

class PendingActionQueueStore {
    private val queue = mutableListOf<PendingAction>()

    fun enqueue(action: PendingAction) {
        queue.add(action)
    }

    fun dequeueAll(): List<PendingAction> = queue.toList().also { queue.clear() }
}
