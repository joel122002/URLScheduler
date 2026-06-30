package com.play.urlscheduler.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.play.urlscheduler.domain.model.LaunchMode

@Entity(tableName = "rotator_jobs")
data class RotatorJobEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val enabled: Boolean,
    val intervalSeconds: Long,
    val launchMode: LaunchMode,
    val currentIndex: Int,
    val createdAt: Long,
    val updatedAt: Long
)
