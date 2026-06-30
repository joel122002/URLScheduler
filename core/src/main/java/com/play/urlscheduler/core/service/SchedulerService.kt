package com.play.urlscheduler.core.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.play.urlscheduler.core.privilege.PrivilegeManager
import com.play.urlscheduler.domain.repository.JobRepository
import com.play.urlscheduler.domain.repository.StateRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class SchedulerService : Service() {

    @Inject lateinit var jobRepository: JobRepository
    @Inject lateinit var stateRepository: StateRepository
    @Inject lateinit var privilegeManager: PrivilegeManager

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Default + serviceJob)

    private var wakeLock: PowerManager.WakeLock? = null

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "scheduler_channel"
        private const val WAKE_LOCK_TAG = "URLScheduler::SchedulerWakeLock"

        private val _isRunning = MutableStateFlow(false)
        val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

        fun start(context: Context) {
            val intent = Intent(context, SchedulerService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, SchedulerService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Timber.i("SchedulerService onCreate")
        _isRunning.value = true
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification("Starting scheduler..."))
        acquireWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.i("SchedulerService onStartCommand")
        startSchedulingLoop()
        return START_STICKY
    }

    private fun startSchedulingLoop() {
        serviceScope.launch {
            try {
                while (isActive) {
                    val job = jobRepository.getActiveJob().first()
                    
                    if (job == null) {
                        Timber.d("No active job found. Waiting 5s...")
                        updateNotification("Idle. No active job.")
                        delay(5000)
                        continue
                    }

                    // Dynamically resolve the active privilege mode right before execution
                    val privileges = privilegeManager.getActivePrivileges()
                    privileges.keepAwake()

                    val urls = jobRepository.getUrlsForJob(job.id).first()
                    
                    if (urls.isEmpty()) {
                        Timber.w("Active job has no URLs. Waiting 5s...")
                        updateNotification("Idle. Job has no URLs.")
                        delay(5000)
                        continue
                    }

                    // Execute current URL
                    val currentIndex = if (job.currentIndex in urls.indices) job.currentIndex else 0
                    val currentUrl = urls[currentIndex]

                    Timber.i("Opening URL [${currentIndex + 1}/${urls.size}]: ${currentUrl.url}")
                    updateNotification("Opening: ${currentUrl.url}")
                    
                    privileges.launchUrl(currentUrl.url, job.launchMode)
                    
                    // Refresh WakeLock to prevent CPU sleep during long intervals
                    refreshWakeLock(job.intervalSeconds * 1000L + 5000L) // Interval + 5s buffer
                    
                    // Persist execution state
                    val nextIndex = (currentIndex + 1) % urls.size
                    jobRepository.updateCurrentIndex(job.id, nextIndex)
                    stateRepository.setLastExecutionTime(System.currentTimeMillis())
                    stateRepository.setLastOpenedUrl(currentUrl.url)

                    // Delay for interval
                    Timber.d("Waiting for ${job.intervalSeconds} seconds...")
                    delay(job.intervalSeconds * 1000L)
                }
            } catch (e: Exception) {
                Timber.e(e, "Error in scheduling loop")
                stateRepository.setLastFailureReason(e.message)
            }
        }
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG)
        refreshWakeLock(10 * 60 * 1000L) // Initial 10 minute lock
    }

    private fun refreshWakeLock(timeoutMs: Long) {
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
            it.acquire(timeoutMs)
            Timber.d("WakeLock refreshed for \${timeoutMs}ms")
        }
    }

    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "URL Scheduler Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun createNotification(contentText: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("URL Scheduler Running")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(text: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, createNotification(text))
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        Timber.i("SchedulerService onDestroy")
        _isRunning.value = false
        serviceScope.cancel()
        releaseWakeLock()
        // Here WatchdogService or BootCompletedReceiver could restart it
    }
}
