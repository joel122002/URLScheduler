package com.play.urlscheduler.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.play.urlscheduler.domain.model.PrivilegeMode
import com.play.urlscheduler.domain.model.RotatorJob
import com.play.urlscheduler.domain.repository.JobRepository
import com.play.urlscheduler.domain.repository.StateRepository
import com.play.urlscheduler.core.service.SchedulerService
import com.play.urlscheduler.core.privilege.PrivilegeManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PermissionState(
    val hasOverlay: Boolean = false,
    val isIgnoringBattery: Boolean = false,
    val isRootGranted: Boolean = false,
    val isDeviceOwner: Boolean = false,
    val isAccessibilityEnabled: Boolean = false,
    val hasNotificationsEnabled: Boolean = false
)

data class HomeUiState(
    val activeJob: RotatorJob? = null,
    val privilegeMode: PrivilegeMode = PrivilegeMode.STANDARD,
    val lastOpenedUrl: String? = null,
    val lastExecutionTime: Long = 0L,
    val isServiceRunning: Boolean = false,
    val permissionState: PermissionState = PermissionState()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val jobRepository: JobRepository,
    private val stateRepository: StateRepository,
    private val privilegeManager: PrivilegeManager
) : ViewModel() {

    private val _permissionState = MutableStateFlow(PermissionState())

    @Suppress("UNCHECKED_CAST")
    val uiState: StateFlow<HomeUiState> = combine(
        jobRepository.getActiveJob(),
        stateRepository.privilegeMode,
        stateRepository.lastOpenedUrl,
        stateRepository.lastExecutionTime,
        SchedulerService.isRunning,
        _permissionState
    ) { array ->
        HomeUiState(
            activeJob = array[0] as RotatorJob?,
            privilegeMode = array[1] as PrivilegeMode,
            lastOpenedUrl = array[2] as String?,
            lastExecutionTime = array[3] as Long,
            isServiceRunning = array[4] as Boolean,
            permissionState = array[5] as PermissionState
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    fun setServiceEnabled(enabled: Boolean) {
        viewModelScope.launch {
            stateRepository.setServiceEnabled(enabled)
        }
    }

    fun refreshPermissions(context: android.content.Context) {
        viewModelScope.launch {
            val hasOverlay = android.provider.Settings.canDrawOverlays(context)
            val powerManager = context.getSystemService(android.content.Context.POWER_SERVICE) as android.os.PowerManager
            val isIgnoringBattery = powerManager.isIgnoringBatteryOptimizations(context.packageName)
            
            val isDeviceOwner = privilegeManager.isDeviceOwner()
            val isAccessibilityEnabled = privilegeManager.isAccessibilityEnabled()
            val isRootGranted = privilegeManager.isRootAvailable()
            val hasNotificationsEnabled = androidx.core.app.NotificationManagerCompat.from(context).areNotificationsEnabled()

            _permissionState.value = PermissionState(
                hasOverlay = hasOverlay,
                isIgnoringBattery = isIgnoringBattery,
                isRootGranted = isRootGranted,
                isDeviceOwner = isDeviceOwner,
                isAccessibilityEnabled = isAccessibilityEnabled,
                hasNotificationsEnabled = hasNotificationsEnabled
            )
        }
    }
}
