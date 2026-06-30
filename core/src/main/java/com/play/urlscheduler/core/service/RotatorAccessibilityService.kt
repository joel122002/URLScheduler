package com.play.urlscheduler.core.service

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import timber.log.Timber

/**
 * Accessibility Service used to bypass background start restrictions
 * and interact with the UI if necessary.
 */
class RotatorAccessibilityService : AccessibilityService() {

    companion object {
        @Volatile
        var isServiceRunning: Boolean = false
            private set
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        isServiceRunning = true
        Timber.i("RotatorAccessibilityService connected!")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // We only use this service for its background execution privileges.
        // We do not need to actively intercept UI events for URL rotation.
    }

    override fun onInterrupt() {
        Timber.w("RotatorAccessibilityService interrupted!")
    }

    override fun onUnbind(intent: android.content.Intent?): Boolean {
        isServiceRunning = false
        Timber.i("RotatorAccessibilityService unbinding!")
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        isServiceRunning = false
        Timber.i("RotatorAccessibilityService destroyed!")
        super.onDestroy()
    }
}
