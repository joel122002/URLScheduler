package com.play.urlscheduler.core.privilege

import android.content.Context
import com.play.urlscheduler.domain.privilege.ShellExecutor
import com.play.urlscheduler.domain.privilege.SystemPrivileges
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject

class RootPrivileges @Inject constructor(
    @ApplicationContext private val context: Context,
    private val shellExecutor: ShellExecutor,
    private val standardPrivileges: StandardPrivileges
) : SystemPrivileges {

    override suspend fun disableDoze() {
        Timber.d("Disabling doze via root")
        shellExecutor.execute("dumpsys deviceidle whitelist +\${context.packageName}")
    }

    override suspend fun whitelistBattery() {
        disableDoze()
    }

    override suspend fun launchUrl(url: String, launchMode: com.play.urlscheduler.domain.model.LaunchMode) {
        Timber.d("Launching URL via root intent: \$url")
        
        // Save the URL to shared memory to bypass destructive shell string parsing
        PayloadHolder.currentUrl = url
        
        val command = when (launchMode) {
            com.play.urlscheduler.domain.model.LaunchMode.CUSTOM_TAB -> {
                "am start --user current -n com.play.urlscheduler/.CustomTabActivity -f 0x14000000"
            }
            com.play.urlscheduler.domain.model.LaunchMode.WEBVIEW -> {
                "am start --user current -n com.play.urlscheduler/.WebViewActivity -f 0x30000000"
            }
            com.play.urlscheduler.domain.model.LaunchMode.EXTERNAL_BROWSER -> {
                "am start --user current -a android.intent.action.VIEW -d \"\$url\" --es com.android.browser.application_id com.play.urlscheduler -f 0x10000000"
            }
        }
        
        val result = shellExecutor.execute(command)
        if (!result.isSuccess) {
            Timber.w("Root am start failed, falling back to standard intent")
            standardPrivileges.launchUrl(url, launchMode)
        }
    }

    override suspend fun restartScheduler() {
        Timber.d("Restarting app via root")
        shellExecutor.execute("am force-stop \${context.packageName}")
        shellExecutor.execute("am start -n \${context.packageName}/.MainActivity")
    }

    override suspend fun keepAwake() {
        val pid = android.os.Process.myPid()
        val packageName = context.packageName
        Timber.d("Optimizing process persistence via root. PID: $pid, Package: $packageName")
        
        try {
            // Write system-level OOM score adjustment (-1000 means never kill)
            val oomResult = shellExecutor.execute("echo -1000 > /proc/$pid/oom_score_adj")
            if (oomResult.isSuccess) {
                Timber.i("Successfully set oom_score_adj to -1000 for PID $pid")
            } else {
                Timber.w("Failed to set oom_score_adj (exit code ${oomResult.exitCode}): ${oomResult.stderr}")
            }
            
            // Set standby bucket to ACTIVE
            val standbyResult = shellExecutor.execute("am set-standby-bucket $packageName active")
            if (standbyResult.isSuccess) {
                Timber.i("Successfully set standby bucket to active for $packageName")
            } else {
                Timber.w("Failed to set standby bucket: ${standbyResult.stderr}")
            }

            // Grant RUN_IN_BACKGROUND and RUN_ANY_IN_BACKGROUND appops permissions
            shellExecutor.execute("cmd appops set $packageName RUN_IN_BACKGROUND allow")
            shellExecutor.execute("cmd appops set $packageName RUN_ANY_IN_BACKGROUND allow")
        } catch (e: Exception) {
            Timber.e(e, "Error executing root keepAwake persistence commands")
        }
    }
}
