package com.wapps1.redcarga.features.deals.data.di

import com.wapps1.redcarga.features.deals.data.remote.services.DealsService
import com.wapps1.redcarga.features.deals.data.repositories.DealsRepositoryImpl
import com.wapps1.redcarga.features.deals.domain.repositories.DealsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

/**
 * Módulo de inyección de dependencias para el módulo de deals
 * ⚠️ Por ahora solo implementa funcionalidad para estado TRATO
 */
@Module
@InstallIn(SingletonComponent::class)
object DealsDataModule {

    @Provides
    @Singleton
    fun provideDealsService(@Named("backend") retrofit: Retrofit): DealsService {
        return retrofit.create(DealsService::class.java)
    }

    @Provides
    @Singleton
    fun provideDealsRepository(
        dealsService: DealsService
    ): DealsRepository {
        return DealsRepositoryImpl(dealsService)
    }
}

