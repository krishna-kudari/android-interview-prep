package com.example.interview.pulsenews.di

import com.example.interview.pulsenews.data.remote.api.FakeNewsApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideFakeNewsApi(): FakeNewsApi = FakeNewsApi
}
