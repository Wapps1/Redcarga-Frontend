package com.wapps1.redcarga.features.chat.data.di

import com.wapps1.redcarga.core.session.AuthSessionStore
import com.wapps1.redcarga.features.chat.data.remote.services.ChatService
import com.wapps1.redcarga.features.chat.data.repositories.ChatRepositoryImpl
import com.wapps1.redcarga.features.chat.domain.repositories.ChatRepository
import com.wapps1.redcarga.features.requests.data.remote.services.QuotesService
import com.wapps1.redcarga.features.requests.data.remote.services.RequestsService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

/**
 * Módulo de inyección de dependencias para el módulo de chat
 */
@Module
@InstallIn(SingletonComponent::class)
object ChatDataModule {

    @Provides
    @Singleton
    fun provideChatService(@Named("backend") retrofit: Retrofit): ChatService {
        return retrofit.create(ChatService::class.java)
    }

    // ⚠️ NO proporcionar RequestsService ni QuotesService aquí
    // Ya están proporcionados en RequestsDataModule y Hilt los resolverá automáticamente

    @Provides
    @Singleton
    fun provideChatRepository(
        chatService: ChatService,
        requestsService: RequestsService, // ✅ Inyectado desde RequestsDataModule
        quotesService: QuotesService,     // ✅ Inyectado desde RequestsDataModule
        authSessionStore: AuthSessionStore
    ): ChatRepository {
        return ChatRepositoryImpl(chatService, requestsService, quotesService, authSessionStore)
    }
}

