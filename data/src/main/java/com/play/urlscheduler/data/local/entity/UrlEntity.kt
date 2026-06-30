package com.play.urlscheduler.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "urls")
data class UrlEntity(
    @PrimaryKey
    val id: String,
    val jobId: String,
    val url: String,
    val orderIndex: Int
)
