package com.play.urlscheduler.data.di

import android.content.Context
import androidx.room.Room
import com.play.urlscheduler.data.local.dao.JobDao
import com.play.urlscheduler.data.local.dao.UrlDao
import com.play.urlscheduler.data.local.database.RotatorDatabase
import com.play.urlscheduler.data.repository.JobRepositoryImpl
import com.play.urlscheduler.data.repository.StateRepositoryImpl
import com.play.urlscheduler.domain.repository.JobRepository
import com.play.urlscheduler.domain.repository.StateRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideRotatorDatabase(@ApplicationContext context: Context): RotatorDatabase {
        return Room.databaseBuilder(
            context,
            RotatorDatabase::class.java,
            RotatorDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideJobDao(database: RotatorDatabase): JobDao {
        return database.jobDao
    }

    @Provides
    fun provideUrlDao(database: RotatorDatabase): UrlDao {
        return database.urlDao
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindJobRepository(
        jobRepositoryImpl: JobRepositoryImpl
    ): JobRepository

    @Binds
    @Singleton
    abstract fun bindStateRepository(
        stateRepositoryImpl: StateRepositoryImpl
    ): StateRepository
}
