package com.play.urlscheduler.domain.model

data class RotatorJob(
    val id: String,
    val name: String,
    val enabled: Boolean,
    val intervalSeconds: Long,
    val launchMode: LaunchMode,
    val currentIndex: Int,
    val createdAt: Long,
    val updatedAt: Long
)
