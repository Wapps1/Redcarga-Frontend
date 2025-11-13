package com.wapps1.redcarga.features.auth.data.repositories

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.wapps1.redcarga.features.auth.domain.models.firebase.FirebaseSession
import com.wapps1.redcarga.features.auth.domain.models.session.AppSession
import com.wapps1.redcarga.features.auth.domain.models.value.Email
import com.wapps1.redcarga.features.auth.domain.models.value.RoleCode
import com.wapps1.redcarga.features.auth.domain.models.value.SessionStatus
import com.wapps1.redcarga.features.auth.domain.models.value.TokenType
import com.wapps1.redcarga.features.auth.domain.repositories.SecureTokenRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SecureTokenRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context
) : SecureTokenRepository {

    private val prefs = EncryptedSharedPreferences.create(
        "auth_secure_store",
        MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    override suspend fun saveFirebaseSession(session: FirebaseSession) = withContext(Dispatchers.IO) {
        prefs.edit().apply {
            putString("fb_localId", session.localId)
            putString("fb_email", session.email.value)
            putString("fb_idToken", session.idToken)
            putString("fb_refreshToken", session.refreshToken)
            putLong("fb_expiresAt", session.expiresAt)
        }.apply()
    }

    override suspend fun getFirebaseSession(): FirebaseSession? = withContext(Dispatchers.IO) {
        val idToken = prefs.getString("fb_idToken", null) ?: return@withContext null
        FirebaseSession(
            localId = prefs.getString("fb_localId", "")!!,
            email = Email(prefs.getString("fb_email", "")!!),
            idToken = idToken,
            refreshToken = prefs.getString("fb_refreshToken", "")!!,
            expiresAt = prefs.getLong("fb_expiresAt", 0L)
        )
    }

    override suspend fun clearFirebaseSession() = withContext(Dispatchers.IO) {
        prefs.edit()
            .remove("fb_localId").remove("fb_email")
            .remove("fb_idToken").remove("fb_refreshToken")
            .remove("fb_expiresAt").apply()
    }

    override suspend fun saveAppSession(session: AppSession) = withContext(Dispatchers.IO) {
        prefs.edit().apply {
            putLong("app_sessionId", session.sessionId)
            putLong("app_accountId", session.accountId)
            putString("app_accessToken", session.accessToken)
            putLong("app_expiresAt", session.expiresAt)
            putString("app_tokenType", session.tokenType.name)
            putString("app_status", session.status.name)
            putString("app_roles_csv", session.roles.joinToString(","))
            
            // Guardar companyId si existe (solo para PROVIDER)
            if (session.companyId != null) {
                putLong("app_companyId", session.companyId)
            } else {
                remove("app_companyId")
            }
        }.apply()
    }

    override suspend fun getAppSession(): AppSession? = withContext(Dispatchers.IO) {
        val token = prefs.getString("app_accessToken", null) ?: return@withContext null
        val rolesCsv = prefs.getString("app_roles_csv", "") ?: ""
        val roles = rolesCsv.split(',').filter { it.isNotBlank() }.mapNotNull {
            when (it) {
                "CLIENT" -> RoleCode.CLIENT
                "PROVIDER" -> RoleCode.PROVIDER
                else -> null
            }
        }
        
        // Recuperar companyId si existe (solo para PROVIDER)
        val companyId = prefs.getLong("app_companyId", -1L).let {
            if (it == -1L) null else it
        }
        
        AppSession(
            sessionId = prefs.getLong("app_sessionId", 0L),
            accountId = prefs.getLong("app_accountId", 0L),
            accessToken = token,
            expiresAt = prefs.getLong("app_expiresAt", 0L),
            tokenType = TokenType.valueOf(prefs.getString("app_tokenType", TokenType.BEARER.name)!!),
            status = SessionStatus.valueOf(prefs.getString("app_status", SessionStatus.ACTIVE.name)!!),
            roles = roles,
            companyId = companyId
        )
    }

    override suspend fun clearAppSession() = withContext(Dispatchers.IO) {
        prefs.edit()
            .remove("app_sessionId").remove("app_accountId")
            .remove("app_accessToken").remove("app_expiresAt")
            .remove("app_tokenType").remove("app_status")
            .remove("app_roles_csv")
            .remove("app_companyId")  // Limpiar también el companyId
            .apply()
    }
}
