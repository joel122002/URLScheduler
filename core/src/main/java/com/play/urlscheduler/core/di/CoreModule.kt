package com.play.urlscheduler.core.di

import android.content.Context
import com.play.urlscheduler.core.privilege.AccessibilityPrivileges
import com.play.urlscheduler.core.privilege.DeviceOwnerPrivileges
import com.play.urlscheduler.core.privilege.PrivilegeManager
import com.play.urlscheduler.core.privilege.RootPrivileges
import com.play.urlscheduler.core.privilege.RootShellExecutor
import com.play.urlscheduler.core.privilege.StandardPrivileges
import com.play.urlscheduler.domain.privilege.ShellExecutor
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
object CoreModule {

    @Provides
    @Singleton
    fun providePrivilegeManager(
        @ApplicationContext context: Context,
        stateRepository: StateRepository,
        shellExecutor: ShellExecutor,
        standardPrivileges: StandardPrivileges,
        accessibilityPrivileges: AccessibilityPrivileges,
        deviceOwnerPrivileges: DeviceOwnerPrivileges,
        rootPrivileges: RootPrivileges
    ): PrivilegeManager {
        return PrivilegeManager(
            context,
            stateRepository,
            shellExecutor,
            standardPrivileges,
            accessibilityPrivileges,
            deviceOwnerPrivileges,
            rootPrivileges
        )
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class CoreBindsModule {
    @Binds
    @Singleton
    abstract fun bindShellExecutor(
        rootShellExecutor: RootShellExecutor
    ): ShellExecutor
}
