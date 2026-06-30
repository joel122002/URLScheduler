package com.play.urlscheduler

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class RotatorApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Timber for logging
        Timber.plant(Timber.DebugTree())
    }
}
