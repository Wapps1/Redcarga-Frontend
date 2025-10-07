package com.wapps1.redcarga.features.auth.domain.repositories

import com.wapps1.redcarga.features.auth.domain.models.iam.*
import com.wapps1.redcarga.features.auth.domain.models.identity.PersonDraft

/**
 * Repositorio local para persistencia en Room
 * Solo datos necesarios para continuidad de UI
 */
interface AuthLocalRepository {
    // IAM
    suspend fun saveAccountSnapshot(snapshot: AccountSnapshot)
    suspend fun getAccountSnapshot(accountId: Long): AccountSnapshot?
    suspend fun saveSignupIntent(intent: SignupIntentSnapshot)
    suspend fun getSignupIntent(accountId: Long): SignupIntentSnapshot?

    // Identity (draft y snapshot mínimo tras confirmación)
    suspend fun savePersonDraft(draft: PersonDraft)
    suspend fun getPersonDraft(accountId: Long): PersonDraft?
    suspend fun clearPersonDraft(accountId: Long)

    // Limpieza al cerrar sesión
    suspend fun clearAllAuthData()
}
