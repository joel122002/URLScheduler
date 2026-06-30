package com.play.urlscheduler.core.receiver

import android.content.Context
import android.content.Intent
import timber.log.Timber

class DeviceAdminReceiver : android.app.admin.DeviceAdminReceiver() {
    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Timber.i("Device Admin Enabled")
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Timber.i("Device Admin Disabled")
    }
}
