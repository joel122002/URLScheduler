package com.play.urlscheduler.core.privilege

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.browser.customtabs.CustomTabsIntent
import com.play.urlscheduler.domain.privilege.SystemPrivileges
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject

class StandardPrivileges @Inject constructor(
    @ApplicationContext private val context: Context
) : SystemPrivileges {

    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

    override suspend fun disableDoze() {
        if (!powerManager.isIgnoringBatteryOptimizations(context.packageName)) {
            Timber.i("Requesting user to disable battery optimizations")
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    override suspend fun whitelistBattery() {
        disableDoze()
    }

    override suspend fun launchUrl(url: String, launchMode: com.play.urlscheduler.domain.model.LaunchMode) {
        Timber.d("Launching URL using $launchMode: $url")
        try {
            when (launchMode) {
                com.play.urlscheduler.domain.model.LaunchMode.CUSTOM_TAB -> {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setClassName(context.packageName, "com.play.urlscheduler.CustomTabActivity")
                        data = Uri.parse(url)
                        putExtra("url", url) // Keep for backward compatibility
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    }
                    context.startActivity(intent)
                }
                com.play.urlscheduler.domain.model.LaunchMode.EXTERNAL_BROWSER -> {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        putExtra(android.provider.Browser.EXTRA_APPLICATION_ID, context.packageName)
                    }
                    context.startActivity(intent)
                }
                com.play.urlscheduler.domain.model.LaunchMode.WEBVIEW -> {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setClassName(context.packageName, "com.play.urlscheduler.WebViewActivity")
                        data = Uri.parse(url)
                        putExtra("url", url) // Keep for backward compatibility
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    }
                    context.startActivity(intent)
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to launch URL, falling back to Intent")
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    override suspend fun restartScheduler() {
        // Handled by Watchdog or BootCompletedReceiver
        Timber.d("restartScheduler not fully supported in StandardPrivileges without user interaction")
    }

    override suspend fun keepAwake() {
        Timber.d("keepAwake in standard privileges: relying on SchedulerService's active foreground service and WakeLock.")
    }
}
