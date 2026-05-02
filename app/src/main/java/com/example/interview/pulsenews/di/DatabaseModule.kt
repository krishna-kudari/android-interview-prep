package com.example.interview.pulsenews.di

import android.content.Context
import androidx.room.Room
import com.example.interview.pulsenews.data.local.dao.BookmarkDao
import com.example.interview.pulsenews.data.local.db.PulseNewsDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module @InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides @Singleton
    fun providePulseNewsDatabase(@ApplicationContext context: Context): PulseNewsDatabase =
        Room.databaseBuilder(
            context = context,
            klass = PulseNewsDatabase::class.java,
            name = "pulse_news_db"
        ).build()

    @Provides @Singleton
    fun provideBookmarkDao(db: PulseNewsDatabase): BookmarkDao = db.bookmarkDao()

}