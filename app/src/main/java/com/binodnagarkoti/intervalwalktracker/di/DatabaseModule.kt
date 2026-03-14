package com.binodnagarkoti.intervalwalktracker.di

import android.content.Context
import com.binodnagarkoti.intervalwalktracker.data.database.AppDatabase
import com.binodnagarkoti.intervalwalktracker.data.database.WalkSessionDao
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
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideWalkSessionDao(database: AppDatabase): WalkSessionDao {
        return database.walkSessionDao()
    }
}
