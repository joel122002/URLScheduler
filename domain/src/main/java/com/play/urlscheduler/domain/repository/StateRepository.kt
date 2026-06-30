package com.play.urlscheduler.domain.repository

import com.play.urlscheduler.domain.model.PrivilegeMode
import kotlinx.coroutines.flow.Flow

interface StateRepository {
    val privilegeMode: Flow<PrivilegeMode>
    val lastExecutionTime: Flow<Long>
    val lastOpenedUrl: Flow<String?>
    val lastFailureReason: Flow<String?>
    val isServiceEnabled: Flow<Boolean>

    suspend fun setPrivilegeMode(mode: PrivilegeMode)
    suspend fun setServiceEnabled(enabled: Boolean)
    suspend fun setLastExecutionTime(timeMillis: Long)
    suspend fun setLastOpenedUrl(url: String)
    suspend fun setLastFailureReason(reason: String?)
}
