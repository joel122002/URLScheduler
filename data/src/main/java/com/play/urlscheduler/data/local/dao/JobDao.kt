package com.play.urlscheduler.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.play.urlscheduler.data.local.entity.RotatorJobEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JobDao {
    @Query("SELECT * FROM rotator_jobs WHERE enabled = 1 LIMIT 1")
    fun getActiveJob(): Flow<RotatorJobEntity?>

    @Query("SELECT * FROM rotator_jobs WHERE id = :jobId LIMIT 1")
    suspend fun getJobById(jobId: String): RotatorJobEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveJob(job: RotatorJobEntity)

    @Query("DELETE FROM rotator_jobs WHERE id = :jobId")
    suspend fun deleteJob(jobId: String)

    @Query("UPDATE rotator_jobs SET currentIndex = :index WHERE id = :jobId")
    suspend fun updateCurrentIndex(jobId: String, index: Int)
}
