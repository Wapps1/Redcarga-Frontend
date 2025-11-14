package com.wapps1.redcarga.features.requests.data.di

import android.content.Context
import com.wapps1.redcarga.core.session.AuthSessionStore
import com.wapps1.redcarga.features.requests.data.local.dao.IncomingRequestsDao
import com.wapps1.redcarga.features.requests.data.local.dao.QuotesDao
import com.wapps1.redcarga.features.requests.data.local.dao.RequestsDao
import com.wapps1.redcarga.features.requests.data.local.db.RequestsDatabase
import com.wapps1.redcarga.features.requests.data.remote.services.PlanningInboxService
import com.wapps1.redcarga.features.requests.data.remote.services.QuotesService
import com.wapps1.redcarga.features.requests.data.remote.services.RequestsService
import com.wapps1.redcarga.features.requests.data.repositories.PlanningInboxRepositoryImpl
import com.wapps1.redcarga.features.requests.data.repositories.QuotesRepositoryImpl
import com.wapps1.redcarga.features.requests.data.repositories.RequestsLocalRepositoryImpl
import com.wapps1.redcarga.features.requests.data.repositories.RequestsRepositoryImpl
import com.wapps1.redcarga.features.requests.domain.repositories.PlanningInboxRepository
import com.wapps1.redcarga.features.requests.domain.repositories.QuotesRepository
import com.wapps1.redcarga.features.requests.domain.repositories.RequestsLocalRepository
import com.wapps1.redcarga.features.requests.domain.repositories.RequestsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RequestsDataModule {

    @Provides
    @Singleton
    fun provideRequestsDatabase(@ApplicationContext context: Context): RequestsDatabase {
        return RequestsDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideRequestsDao(database: RequestsDatabase): RequestsDao {
        return database.requestsDao()
    }

    @Provides
    @Singleton
    fun provideRequestsService(@Named("backend") retrofit: Retrofit): RequestsService {
        return retrofit.create(RequestsService::class.java)
    }

    @Provides
    @Singleton
    fun provideRequestsRepository(
        remote: RequestsService,
        local: RequestsLocalRepository,
        authSessionStore: AuthSessionStore
    ): RequestsRepository {
        return RequestsRepositoryImpl(remote, local, authSessionStore)
    }

    @Provides
    @Singleton
    fun provideRequestsLocalRepository(
        dao: RequestsDao,
        authSessionStore: AuthSessionStore
    ): RequestsLocalRepository {
        return RequestsLocalRepositoryImpl(dao, authSessionStore)
    }
    
    // ========== PLANNING INBOX ==========
    
    @Provides
    @Singleton
    fun provideIncomingRequestsDao(database: RequestsDatabase): IncomingRequestsDao {
        return database.incomingRequestsDao()
    }
    
    @Provides
    @Singleton
    fun providePlanningInboxService(@Named("backend") retrofit: Retrofit): PlanningInboxService {
        return retrofit.create(PlanningInboxService::class.java)
    }
    
    @Provides
    @Singleton
    fun providePlanningInboxRepository(
        inboxService: PlanningInboxService,
        requestsService: RequestsService,
        inboxDao: IncomingRequestsDao
    ): PlanningInboxRepository {
        return PlanningInboxRepositoryImpl(inboxService, requestsService, inboxDao)
    }
    
    // ========== QUOTES ==========
    
    @Provides
    @Singleton
    fun provideQuotesDao(database: RequestsDatabase): QuotesDao {
        return database.quotesDao()
    }
    
    @Provides
    @Singleton
    fun provideQuotesService(@Named("backend") retrofit: Retrofit): QuotesService {
        return retrofit.create(QuotesService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideQuotesRepository(
        quotesService: QuotesService,
        quotesDao: QuotesDao
    ): QuotesRepository {
        return QuotesRepositoryImpl(quotesService, quotesDao)
    }
}
