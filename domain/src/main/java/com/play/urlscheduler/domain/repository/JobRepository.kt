package com.play.urlscheduler.domain.repository

import com.play.urlscheduler.domain.model.RotatorJob
import com.play.urlscheduler.domain.model.UrlEntry
import kotlinx.coroutines.flow.Flow

interface JobRepository {
    fun getActiveJob(): Flow<RotatorJob?>
    suspend fun getJobById(jobId: String): RotatorJob?
    fun getUrlsForJob(jobId: String): Flow<List<UrlEntry>>
    suspend fun getUrlsListForJob(jobId: String): List<UrlEntry>
    suspend fun saveJob(job: RotatorJob)
    suspend fun saveUrls(urls: List<UrlEntry>)
    suspend fun deleteJob(jobId: String)
    suspend fun deleteUrlsForJob(jobId: String)
    suspend fun updateCurrentIndex(jobId: String, index: Int)
}
