package com.example.interview.coinwatch.di

import com.example.interview.coinwatch.data.repository.CoinRepositoryImpl
import com.example.interview.coinwatch.domain.repository.CoinsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCoinRepository(impl: CoinRepositoryImpl): CoinsRepository
}