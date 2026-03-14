package com.binodnagarkoti.intervalwalktracker.di

import android.content.Context
import android.hardware.SensorManager
import com.binodnagarkoti.intervalwalktracker.audio.AudioCoachManager
import com.binodnagarkoti.intervalwalktracker.sensors.StepSensorManager
import com.binodnagarkoti.intervalwalktracker.timer.IntervalTimerManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSensorManager(@ApplicationContext context: Context): SensorManager {
        return context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    @Provides
    @Singleton
    fun provideStepSensorManager(@ApplicationContext context: Context): StepSensorManager {
        return StepSensorManager(context)
    }

    @Provides
    @Singleton
    fun provideIntervalTimerManager(): IntervalTimerManager {
        return IntervalTimerManager()
    }

    @Provides
    @Singleton
    fun provideAudioCoachManager(@ApplicationContext context: Context): AudioCoachManager {
        return AudioCoachManager(context)
    }
}
