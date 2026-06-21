package com.example.interview.eventloggerlab.di

import android.content.Context
import com.example.eventlogger.EventLoggerFactory
import com.example.eventlogger.api.EventLogger
import com.example.eventlogger.api.LoggerConfig
import com.example.interview.eventloggerlab.TimberEventUploader
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object EventLoggerModule {

    @Provides
    @Singleton
    fun provideEventLogger(@ApplicationContext context: Context): EventLogger {
        val config = LoggerConfig.Builder(uploader = TimberEventUploader())
            .dbBatchSize(20)
            .dbBatchTimeoutMs(3_000L)
            .uploadBatchSize(10)
            .uploadIntervalWhileForegroundMs(15_000L)
            .build()

        return EventLoggerFactory.create(context, config)
    }
}
