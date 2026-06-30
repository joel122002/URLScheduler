package com.play.urlscheduler.core.privilege

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.play.urlscheduler.domain.privilege.SystemPrivileges
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject

class DeviceOwnerPrivileges @Inject constructor(
    @ApplicationContext private val context: Context,
    private val standardPrivileges: StandardPrivileges
) : SystemPrivileges {

    private val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    // In a real app, this should be an actual DeviceAdminReceiver ComponentName
    private val adminComponent = ComponentName(context, "com.play.urlscheduler.core.receiver.DeviceAdminReceiver")

    override suspend fun disableDoze() {
        standardPrivileges.disableDoze()
    }

    override suspend fun whitelistBattery() {
        standardPrivileges.whitelistBattery()
    }

    override suspend fun launchUrl(url: String, launchMode: com.play.urlscheduler.domain.model.LaunchMode) {
        Timber.d("Launching URL in Device Owner Mode: $url")
        standardPrivileges.launchUrl(url, launchMode)
    }

    override suspend fun restartScheduler() {
        standardPrivileges.restartScheduler()
    }

    override suspend fun keepAwake() {
        if (dpm.isDeviceOwnerApp(context.packageName)) {
            Timber.d("Device Owner active - background persistence is naturally managed by the OS.")
        }
    }
}
