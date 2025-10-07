package com.wapps1.redcarga.core.di

import com.wapps1.redcarga.core.session.AuthSessionStore
import com.wapps1.redcarga.core.session.AuthSessionStoreImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SessionModule {
    
    @Provides
    @Singleton
    fun provideAuthSessionStore(impl: AuthSessionStoreImpl): AuthSessionStore = impl
}
