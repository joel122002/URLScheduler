package com.play.urlscheduler.data.repository

import com.play.urlscheduler.data.local.entity.RotatorJobEntity
import com.play.urlscheduler.data.local.entity.UrlEntity
import com.play.urlscheduler.domain.model.RotatorJob
import com.play.urlscheduler.domain.model.UrlEntry

fun RotatorJobEntity.toDomain() = RotatorJob(
    id = id,
    name = name,
    enabled = enabled,
    intervalSeconds = intervalSeconds,
    launchMode = launchMode,
    currentIndex = currentIndex,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun RotatorJob.toEntity() = RotatorJobEntity(
    id = id,
    name = name,
    enabled = enabled,
    intervalSeconds = intervalSeconds,
    launchMode = launchMode,
    currentIndex = currentIndex,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun UrlEntity.toDomain() = UrlEntry(
    id = id,
    jobId = jobId,
    url = url,
    orderIndex = orderIndex
)

fun UrlEntry.toEntity() = UrlEntity(
    id = id,
    jobId = jobId,
    url = url,
    orderIndex = orderIndex
)
