package com.play.urlscheduler.core.service

import android.app.ActivityManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import com.play.urlscheduler.domain.repository.StateRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * A secondary service that periodically checks if SchedulerService is running.
 * If it's not running, it restarts it.
 */
@AndroidEntryPoint
class WatchdogService : Service() {

    @Inject lateinit var stateRepository: StateRepository

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Default + serviceJob)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.i("WatchdogService started")
        startMonitoring()
        return START_STICKY
    }

    private fun startMonitoring() {
        serviceScope.launch {
            while (isActive) {
                delay(60_000) // Check every 60 seconds

                val isEnabled = stateRepository.isServiceEnabled.first()
                if (!isEnabled) {
                    Timber.i("Watchdog: Service is disabled by user. Self-destructing...")
                    stopSelf()
                    return@launch
                }

                if (!isServiceRunning(SchedulerService::class.java)) {
                    Timber.w("SchedulerService is NOT running! Restarting...")
                    try {
                        SchedulerService.start(applicationContext)
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to restart SchedulerService from Watchdog")
                    }
                } else {
                    Timber.d("Watchdog: SchedulerService is alive.")
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
