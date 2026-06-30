package com.play.urlscheduler.feature.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.play.urlscheduler.core.service.SchedulerService
import com.play.urlscheduler.core.service.WatchdogService
import com.play.urlscheduler.core.ui.components.RotatorTopAppBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.net.toUri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshPermissions(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            RotatorTopAppBar(title = "Dashboard")
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!uiState.permissionState.isIgnoringBattery) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Hibernation Risk",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Android may pause the scheduler when the screen is off. Disable battery optimizations to ensure infinite background execution.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Button(
                            onClick = {
                                val intent = Intent(
                                    Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                                    "package:${context.packageName}".toUri()
                                )
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary,
                                contentColor = MaterialTheme.colorScheme.onTertiary
                            )
                        ) {
                            Text("Disable Optimizations")
                        }
                    }
                }
            }

            if (!uiState.permissionState.hasNotificationsEnabled) {
                WarningCard(
                    title = "Notifications Disabled",
                    message = "To guarantee survival against all OEM battery killers, the persistent notification must be visible. Please allow notifications.",
                    buttonText = "Allow Notifications",
                    onClick = {
                        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        }
                        context.startActivity(intent)
                    }
                )
            }

            when (uiState.privilegeMode) {
                com.play.urlscheduler.domain.model.PrivilegeMode.STANDARD -> {
                    if (!uiState.permissionState.hasOverlay) {
                        WarningCard(
                            title = "Background Execution Paralyzed",
                            message = "To allow URL rotation when the app is closed, you must grant the 'Display over other apps' permission.",
                            buttonText = "Grant Permission",
                            onClick = {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    "package:${context.packageName}".toUri()
                                )
                                context.startActivity(intent)
                            }
                        )
                    }
                }
                com.play.urlscheduler.domain.model.PrivilegeMode.ACCESSIBILITY -> {
                    if (!uiState.permissionState.isAccessibilityEnabled) {
                        WarningCard(
                            title = "Accessibility Service Disabled",
                            message = "To bypass activity launch constraints, you must enable the URL Rotator Accessibility Service in System Settings.",
                            buttonText = "Open Settings",
                            onClick = {
                                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                context.startActivity(intent)
                            }
                        )
                    }
                }
                com.play.urlscheduler.domain.model.PrivilegeMode.DEVICE_OWNER -> {
                    if (!uiState.permissionState.isDeviceOwner) {
                        WarningCard(
                            title = "Device Owner Missing",
                            message = "This mode requires ADB provisioning. Run this command on your computer:\\nadb shell dpm set-device-owner ${context.packageName}/.core.receiver.DeviceAdminReceiver",
                            buttonText = "Acknowledge",
                            onClick = { /* Could copy to clipboard */ }
                        )
                    }
                }
                com.play.urlscheduler.domain.model.PrivilegeMode.ROOT -> {
                    if (!uiState.permissionState.isRootGranted) {
                        WarningCard(
                            title = "Root Access Denied",
                            message = "The app failed to acquire a root shell. Please open your Root Manager (Magisk/KernelSU) and grant superuser access.",
                            buttonText = "Re-check Root",
                            onClick = { viewModel.refreshPermissions(context) }
                        )
                    }
                }
            }

            StatusCard(uiState = uiState)
            
            Button(
                onClick = { 
                    if (uiState.isServiceRunning) {
                        viewModel.setServiceEnabled(false)
                        SchedulerService.stop(context)
                        context.stopService(Intent(context, WatchdogService::class.java))
                    } else {
                        viewModel.setServiceEnabled(true)
                        SchedulerService.start(context)
                        context.startService(Intent(context, WatchdogService::class.java))
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (uiState.isServiceRunning) 
                        MaterialTheme.colorScheme.error 
                    else 
                        MaterialTheme.colorScheme.primary
                )
            ) {
                Text(if (uiState.isServiceRunning) "Stop Service" else "Start Service")
            }
        }
    }
}

@Composable
fun StatusCard(uiState: HomeUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Service Status", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            
            Text("Active Job: ${uiState.activeJob?.name ?: "None"}")
            Text("Privilege Mode: ${uiState.privilegeMode}")
            
            val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val timeString = if (uiState.lastExecutionTime > 0) formatter.format(Date(uiState.lastExecutionTime)) else "Never"
            Text("Last Executed: $timeString")
            Text("Last URL: ${uiState.lastOpenedUrl ?: "None"}")
        }
    }
}

@Composable
fun WarningCard(
    title: String,
    message: String,
    buttonText: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium
            )
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text(buttonText)
            }
        }
    }
}
