package com.wapps1.redcarga.core.session

import com.wapps1.redcarga.features.auth.domain.models.firebase.FirebaseSession
import com.wapps1.redcarga.features.auth.domain.models.session.AppLoginRequest
import com.wapps1.redcarga.features.auth.domain.models.session.AppSession
import com.wapps1.redcarga.features.auth.domain.models.value.Email
import com.wapps1.redcarga.features.auth.domain.models.value.Password
import com.wapps1.redcarga.features.auth.domain.models.value.Platform
import com.wapps1.redcarga.features.auth.domain.models.value.RoleCode
import com.wapps1.redcarga.features.auth.domain.repositories.AuthLocalRepository
import com.wapps1.redcarga.features.auth.domain.repositories.AuthRemoteRepository
import com.wapps1.redcarga.features.auth.domain.repositories.FirebaseAuthRepository
import com.wapps1.redcarga.features.auth.domain.repositories.SecureTokenRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthSessionStoreImpl @Inject constructor(
    private val secure: SecureTokenRepository,
    private val local: AuthLocalRepository,
    private val auth: AuthRemoteRepository,
    private val firebase: FirebaseAuthRepository,
) : AuthSessionStore {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _sessionState = MutableStateFlow<SessionState>(SessionState.SignedOut)
    override val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    private val _currentUserType = MutableStateFlow<UserType?>(null)
    override val currentUserType: StateFlow<UserType?> = _currentUserType.asStateFlow()

    // ---- helpers
    private fun List<RoleCode>.toUserType(): UserType? = when {
        isEmpty() -> null
        contains(RoleCode.PROVIDER) -> UserType.PROVIDER
        contains(RoleCode.CLIENT) -> UserType.CLIENT
        else -> null
    }

    private fun isExpired(atMillis: Long) = System.currentTimeMillis() >= atMillis

    private suspend fun emitFromStorageOrSignedOut() {
        val app = secure.getAppSession()
        if (app != null && !isExpired(app.expiresAt)) {
            _sessionState.value = SessionState.AppSignedIn(app)
            _currentUserType.value = app.roles.toUserType()
            return
        }
        val fb = secure.getFirebaseSession()
        if (fb != null && !isExpired(fb.expiresAt)) {
            _sessionState.value = SessionState.FirebaseOnly(fb)
            _currentUserType.value = null
        } else {
            _sessionState.value = SessionState.SignedOut
            _currentUserType.value = null
        }
    }

    // ---- API
    override suspend fun bootstrap() {
        emitFromStorageOrSignedOut()
        // si ya hay FirebaseOnly, intentamos auto-login
        val st = _sessionState.value
        if (st is SessionState.FirebaseOnly) {
            runCatching { tryAppLogin(platform = Platform.ANDROID, ip = "0.0.0.0") }
        }
    }

    override suspend fun signInManually(
        email: Email, password: Password, platform: Platform, ip: String
    ) {
        // 1) Firebase
        val fb = firebase.signInWithPassword(email, password)
        secure.saveFirebaseSession(fb)
        _sessionState.value = SessionState.FirebaseOnly(fb)

        // 2) /iam/login
        tryAppLogin(platform, ip)
    }

    override suspend fun setFirebaseSession(session: FirebaseSession) {
        secure.saveFirebaseSession(session)
        _sessionState.value = SessionState.FirebaseOnly(session)
        _currentUserType.value = null
    }

    override suspend fun tryAppLogin(platform: Platform, ip: String) {
        val fb = secure.getFirebaseSession()
            ?: throw IllegalStateException("No Firebase session for app login")
        
        // AuthRemoteRepositoryImpl ya persiste AppSession + AccountSnapshot
        val app = auth.login(AppLoginRequest(platform, ip))

        _sessionState.value = SessionState.AppSignedIn(app)
        _currentUserType.value = app.roles.toUserType()
    }

    override suspend fun logout() {
        secure.clearAppSession()
        secure.clearFirebaseSession()
        local.clearAllAuthData()
        _sessionState.value = SessionState.SignedOut
        _currentUserType.value = null
    }
}
