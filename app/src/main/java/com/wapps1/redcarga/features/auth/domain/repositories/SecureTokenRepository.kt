package com.wapps1.redcarga.features.auth.domain.repositories

import com.wapps1.redcarga.features.auth.domain.models.firebase.FirebaseSession
import com.wapps1.redcarga.features.auth.domain.models.session.AppSession

/**
 * Repositorio para persistencia segura de tokens (Encrypted DataStore/Prefs)
 */
interface SecureTokenRepository {
    // Firebase tokens
    suspend fun saveFirebaseSession(session: FirebaseSession)
    suspend fun getFirebaseSession(): FirebaseSession?
    suspend fun clearFirebaseSession()

    // App session
    suspend fun saveAppSession(session: AppSession)
    suspend fun getAppSession(): AppSession?
    suspend fun clearAppSession()
}
