package com.play.urlscheduler.data.repository

import com.play.urlscheduler.data.local.dao.JobDao
import com.play.urlscheduler.data.local.dao.UrlDao
import com.play.urlscheduler.domain.model.RotatorJob
import com.play.urlscheduler.domain.model.UrlEntry
import com.play.urlscheduler.domain.repository.JobRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class JobRepositoryImpl @Inject constructor(
    private val jobDao: JobDao,
    private val urlDao: UrlDao
) : JobRepository {

    override fun getActiveJob(): Flow<RotatorJob?> {
        return jobDao.getActiveJob().map { it?.toDomain() }
    }

    override suspend fun getJobById(jobId: String): RotatorJob? {
        return jobDao.getJobById(jobId)?.toDomain()
    }

    override fun getUrlsForJob(jobId: String): Flow<List<UrlEntry>> {
        return urlDao.getUrlsForJob(jobId).map { urls -> urls.map { it.toDomain() } }
    }

    override suspend fun getUrlsListForJob(jobId: String): List<UrlEntry> {
        return urlDao.getUrlsListForJob(jobId).map { it.toDomain() }
    }

    override suspend fun saveJob(job: RotatorJob) {
        jobDao.saveJob(job.toEntity())
    }

    override suspend fun saveUrls(urls: List<UrlEntry>) {
        urlDao.saveUrls(urls.map { it.toEntity() })
    }

    override suspend fun deleteJob(jobId: String) {
        jobDao.deleteJob(jobId)
        urlDao.deleteUrlsForJob(jobId)
    }

    override suspend fun deleteUrlsForJob(jobId: String) {
        urlDao.deleteUrlsForJob(jobId)
    }

    override suspend fun updateCurrentIndex(jobId: String, index: Int) {
        jobDao.updateCurrentIndex(jobId, index)
    }
}
