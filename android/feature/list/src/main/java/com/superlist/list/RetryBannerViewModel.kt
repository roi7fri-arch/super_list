package com.superlist.list

data class RetryBannerState(
    val visible: Boolean = false,
    val message: String = "",
)

class RetryBannerViewModel {
    var state: RetryBannerState = RetryBannerState()
        private set

    fun showPersistentFailure() {
        state = RetryBannerState(true, "שגיאת סנכרון מתמשכת, ננסה שוב")
    }

    fun clear() {
        state = RetryBannerState()
    }
}
