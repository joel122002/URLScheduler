package com.play.urlscheduler.core.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.play.urlscheduler.core.service.SchedulerService
import com.play.urlscheduler.core.service.WatchdogService
import com.play.urlscheduler.domain.repository.StateRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {

    @Inject lateinit var stateRepository: StateRepository

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val action = intent.action
        Timber.i("Received broadcast: \$action")

        if (action == Intent.ACTION_BOOT_COMPLETED || 
            action == Intent.ACTION_LOCKED_BOOT_COMPLETED || 
            action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            
            Timber.i("System event detected, checking if service should start...")
            
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val isEnabled = stateRepository.isServiceEnabled.first()
                    if (isEnabled) {
                        Timber.i("Service was enabled before boot. Starting services.")
                        SchedulerService.start(context)
                        context.startService(Intent(context, WatchdogService::class.java))
                    } else {
                        Timber.i("Service was disabled by user before boot. Remaining asleep.")
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Failed to start services from broadcast receiver")
                } finally {
                    pendingResult.finish()
                }
            }
        } else {
            pendingResult.finish()
        }
    }
}
