package com.binodnagarkoti.intervalwalktracker.di

import com.binodnagarkoti.intervalwalktracker.data.database.WalkSessionDao
import com.binodnagarkoti.intervalwalktracker.data.repository.SessionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideSessionRepository(walkSessionDao: WalkSessionDao): SessionRepository {
        return SessionRepository(walkSessionDao)
    }
}
