package com.play.urlscheduler.core.privilege

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import com.play.urlscheduler.domain.model.PrivilegeMode
import com.play.urlscheduler.domain.privilege.ShellExecutor
import com.play.urlscheduler.domain.privilege.SystemPrivileges
import com.play.urlscheduler.domain.repository.StateRepository
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject

class PrivilegeManager @Inject constructor(
    private val context: Context,
    private val stateRepository: StateRepository,
    private val shellExecutor: ShellExecutor,
    private val standardPrivileges: StandardPrivileges,
    private val accessibilityPrivileges: AccessibilityPrivileges,
    private val deviceOwnerPrivileges: DeviceOwnerPrivileges,
    private val rootPrivileges: RootPrivileges
) {

    private var hasGrantedAppOps = false

    suspend fun getActivePrivileges(): SystemPrivileges {
        val mode = stateRepository.privilegeMode.first()
        Timber.d("Active Privilege Mode: \$mode")
        
        return when (mode) {
            PrivilegeMode.ROOT -> {
                if (isRootAvailable()) rootPrivileges else fallback(mode)
            }
            PrivilegeMode.DEVICE_OWNER -> {
                if (isDeviceOwner()) deviceOwnerPrivileges else fallback(mode)
            }
            PrivilegeMode.ACCESSIBILITY -> {
                accessibilityPrivileges
            }
            PrivilegeMode.STANDARD -> standardPrivileges
        }
    }

    private suspend fun fallback(failedMode: PrivilegeMode): SystemPrivileges {
        Timber.w("Requested mode \$failedMode is not available. Falling back to STANDARD for this execution.")
        // REMOVED: stateRepository.setPrivilegeMode(PrivilegeMode.STANDARD)
        // We no longer overwrite the user's saved preference just because a check failed.
        return standardPrivileges
    }

    suspend fun isRootAvailable(): Boolean {
        return try {
            val result = shellExecutor.execute("id")
            if (result.isSuccess && !hasGrantedAppOps) {
                // Auto-grant SYSTEM_ALERT_WINDOW as a failsafe for background starts
                shellExecutor.execute("appops set \${context.packageName} SYSTEM_ALERT_WINDOW allow")
                hasGrantedAppOps = true
            }
            result.isSuccess
        } catch (e: Exception) {
            false
        }
    }

    fun isDeviceOwner(): Boolean {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        return dpm.isDeviceOwnerApp(context.packageName)
    }

    fun isAccessibilityEnabled(): Boolean {
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        return enabledServices?.contains(context.packageName) == true
    }
}
