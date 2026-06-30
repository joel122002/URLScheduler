package com.play.urlscheduler.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.play.urlscheduler.data.local.dao.JobDao
import com.play.urlscheduler.data.local.dao.UrlDao
import com.play.urlscheduler.data.local.entity.RotatorJobEntity
import com.play.urlscheduler.data.local.entity.UrlEntity

@Database(
    entities = [RotatorJobEntity::class, UrlEntity::class],
    version = 1,
    exportSchema = false
)
abstract class RotatorDatabase : RoomDatabase() {
    abstract val jobDao: JobDao
    abstract val urlDao: UrlDao

    companion object {
        const val DATABASE_NAME = "rotator_db"
    }
}
