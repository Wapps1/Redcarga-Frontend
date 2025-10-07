package com.wapps1.redcarga.features.auth.data.repositories

import com.wapps1.redcarga.features.auth.data.local.db.AuthDatabase
import com.wapps1.redcarga.features.auth.data.mappers.toDomain
import com.wapps1.redcarga.features.auth.data.mappers.toEntity
import com.wapps1.redcarga.features.auth.domain.models.iam.*
import com.wapps1.redcarga.features.auth.domain.models.identity.PersonDraft
import com.wapps1.redcarga.features.auth.domain.repositories.AuthLocalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AuthLocalRepositoryImpl @Inject constructor(
    private val db: AuthDatabase
) : AuthLocalRepository {

    override suspend fun saveAccountSnapshot(snapshot: AccountSnapshot) =
        withContext(Dispatchers.IO) { db.accountDao().upsert(snapshot.toEntity()) }

    override suspend fun getAccountSnapshot(accountId: Long) =
        withContext(Dispatchers.IO) { db.accountDao().find(accountId)?.toDomain() }

    override suspend fun saveSignupIntent(intent: SignupIntentSnapshot) =
        withContext(Dispatchers.IO) { db.intentDao().upsert(intent.toEntity()) }

    override suspend fun getSignupIntent(accountId: Long) =
        withContext(Dispatchers.IO) { db.intentDao().findByAccount(accountId)?.toDomain() }

    override suspend fun savePersonDraft(draft: PersonDraft) =
        withContext(Dispatchers.IO) { db.personDraftDao().upsert(draft.toEntity()) }

    override suspend fun getPersonDraft(accountId: Long) =
        withContext(Dispatchers.IO) { db.personDraftDao().find(accountId)?.toDomain() }

    override suspend fun clearPersonDraft(accountId: Long) =
        withContext(Dispatchers.IO) { db.personDraftDao().delete(accountId) }

    override suspend fun clearAllAuthData() = withContext(Dispatchers.IO) {
        db.accountDao().clear()
        db.intentDao().clear()
        db.personDraftDao().clear()
    }
}
