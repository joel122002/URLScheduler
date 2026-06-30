package com.play.urlscheduler.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.play.urlscheduler.data.local.entity.UrlEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UrlDao {
    @Query("SELECT * FROM urls WHERE jobId = :jobId ORDER BY orderIndex ASC")
    fun getUrlsForJob(jobId: String): Flow<List<UrlEntity>>

    @Query("SELECT * FROM urls WHERE jobId = :jobId ORDER BY orderIndex ASC")
    suspend fun getUrlsListForJob(jobId: String): List<UrlEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUrls(urls: List<UrlEntity>)

    @Query("DELETE FROM urls WHERE jobId = :jobId")
    suspend fun deleteUrlsForJob(jobId: String)
}
