package com.wapps1.redcarga.core.session

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * EntryPoint para acceder a AuthSessionStore desde composables
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface AuthSessionStoreEntryPoint {
    fun authSessionStore(): AuthSessionStore
}
