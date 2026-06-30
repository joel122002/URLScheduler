package com.play.urlscheduler.domain.model

data class UrlEntry(
    val id: String,
    val jobId: String,
    val url: String,
    val orderIndex: Int
)
