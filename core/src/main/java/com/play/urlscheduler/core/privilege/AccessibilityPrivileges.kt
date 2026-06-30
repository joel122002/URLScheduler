package com.play.urlscheduler.core.privilege

import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.play.urlscheduler.core.service.RotatorAccessibilityService
import com.play.urlscheduler.domain.privilege.SystemPrivileges
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject

class AccessibilityPrivileges @Inject constructor(
    @ApplicationContext private val context: Context,
    private val standardPrivileges: StandardPrivileges
) : SystemPrivileges {

    override suspend fun disableDoze() {
        standardPrivileges.disableDoze()
        // Here we could automate the settings clicks via accessibility
    }

    override suspend fun whitelistBattery() {
        standardPrivileges.whitelistBattery()
    }

    override suspend fun launchUrl(url: String, launchMode: com.play.urlscheduler.domain.model.LaunchMode) {
        Timber.d("Launching URL with Accessibility enhancements: $url")
        standardPrivileges.launchUrl(url, launchMode)
    }

    override suspend fun restartScheduler() {
        standardPrivileges.restartScheduler()
    }

    override suspend fun keepAwake() {
        if (!RotatorAccessibilityService.isServiceRunning) {
            Timber.w("RotatorAccessibilityService is not connected. Prompting user to enable it.")
            try {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                Timber.e(e, "Failed to launch Accessibility Settings")
            }
        } else {
            Timber.d("RotatorAccessibilityService is active and connected.")
        }
        standardPrivileges.keepAwake()
    }
}
