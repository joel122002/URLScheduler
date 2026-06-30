package com.play.urlscheduler.domain.privilege

interface SystemPrivileges {
    suspend fun disableDoze()
    suspend fun whitelistBattery()
    suspend fun launchUrl(url: String, launchMode: com.play.urlscheduler.domain.model.LaunchMode)
    suspend fun restartScheduler()
    suspend fun keepAwake()
}
