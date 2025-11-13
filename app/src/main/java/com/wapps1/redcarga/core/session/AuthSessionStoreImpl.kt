package com.wapps1.redcarga.core.session

import android.util.Log
import com.wapps1.redcarga.core.session.UserType
import com.wapps1.redcarga.core.websocket.RedcargaWebSocketManager
import com.wapps1.redcarga.core.websocket.WebSocketUserType
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
    private val webSocketManager: RedcargaWebSocketManager,
) : AuthSessionStore {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _sessionState = MutableStateFlow<SessionState>(SessionState.SignedOut)
    override val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    private val _currentUserType = MutableStateFlow<UserType?>(null)
    override val currentUserType: StateFlow<UserType?> = _currentUserType.asStateFlow()

    private val _currentCompanyId = MutableStateFlow<Long?>(null)
    override val currentCompanyId: StateFlow<Long?> = _currentCompanyId.asStateFlow()

    private val _currentUsername = MutableStateFlow<String?>(null)
    override val currentUsername: StateFlow<String?> = _currentUsername.asStateFlow()

    private fun List<RoleCode>.toUserType(): UserType? = when {
        isEmpty() -> null
        contains(RoleCode.PROVIDER) -> UserType.PROVIDER
        contains(RoleCode.CLIENT) -> UserType.CLIENT
        else -> null
    }

    private fun List<RoleCode>.toWebSocketUserType(): WebSocketUserType? = when {
        isEmpty() -> null
        contains(RoleCode.PROVIDER) -> WebSocketUserType.PROVIDER
        contains(RoleCode.CLIENT) -> WebSocketUserType.CLIENT
        else -> null
    }

    private fun isExpired(atMillis: Long) = System.currentTimeMillis() >= atMillis

    private suspend fun getCurrentUsername(): String? {
        return try {
            val session = secure.getAppSession()
            if (session != null) {
                local.getAccountSnapshot(session.accountId)?.username?.value
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w("AuthSessionStore", "Error obteniendo username: ${e.message}")
            null
        }
    }

    private suspend fun emitFromStorageOrSignedOut() {
        Log.d("AuthSessionStore", "🔍 emitFromStorageOrSignedOut() - Verificando almacenamiento...")

        val app = secure.getAppSession()
        Log.d("AuthSessionStore", "   AppSession obtenido: ${if (app != null) "✅ EXISTE" else "❌ NULL"}")

        if (app != null) {
            val expired = isExpired(app.expiresAt)
            Log.d("AuthSessionStore", "   AppSession expirado: $expired (expiresAt=${app.expiresAt})")
            Log.d("AuthSessionStore", "   AppSession companyId: ${app.companyId}")
            Log.d("AuthSessionStore", "   AppSession roles: ${app.roles}")
        }

        if (app != null && !isExpired(app.expiresAt)) {
            _sessionState.value = SessionState.AppSignedIn(app)
            _currentUserType.value = app.roles.toUserType()
            _currentCompanyId.value = app.companyId
            _currentUsername.value = getCurrentUsername()
            Log.d("AuthSessionStore", "📦 ✅ Sesión APP restaurada - CompanyId: ${app.companyId}, UserType: ${app.roles.toUserType()}")
            return
        }

        val fb = secure.getFirebaseSession()
        Log.d("AuthSessionStore", "   FirebaseSession obtenido: ${if (fb != null) "✅ EXISTE" else "❌ NULL"}")

        if (fb != null && !isExpired(fb.expiresAt)) {
            _sessionState.value = SessionState.FirebaseOnly(fb)
            _currentUserType.value = null
            _currentCompanyId.value = null
            _currentUsername.value = null
            Log.d("AuthSessionStore", "🔥 Solo sesión Firebase - Sin companyId (necesita app login)")
        } else {
            _sessionState.value = SessionState.SignedOut
            _currentUserType.value = null
            _currentCompanyId.value = null
            _currentUsername.value = null
            Log.d("AuthSessionStore", "🚪 Sin sesión - Usuario debe hacer login")
        }
    }

    override suspend fun bootstrap() {
        Log.d("AuthSessionStore", "🚀 bootstrap() llamado")
        emitFromStorageOrSignedOut()
        Log.d("AuthSessionStore", "🚀 bootstrap() completado - currentCompanyId=${_currentCompanyId.value}, userType=${_currentUserType.value}")
    }

    override suspend fun signInManually(
        email: Email,
        password: String,  // ✅ String sin validación para login
        platform: Platform,
        ip: String
    ) {
        // En login NO validamos la contraseña (ya existe en el backend)
        val fb = firebase.signInWithPassword(email, password)
        secure.saveFirebaseSession(fb)
        _sessionState.value = SessionState.FirebaseOnly(fb)
        _currentUserType.value = null
        _currentCompanyId.value = null
        _currentUsername.value = null
    }

    override suspend fun setFirebaseSession(session: FirebaseSession) {
        secure.saveFirebaseSession(session)
        _sessionState.value = SessionState.FirebaseOnly(session)
        _currentUserType.value = null
        _currentCompanyId.value = null
        _currentUsername.value = null
    }

    override suspend fun tryAppLogin(platform: Platform, ip: String) {

        val fb = secure.getFirebaseSession()
            ?: throw IllegalStateException("No Firebase session for app login")


        val app = auth.login(AppLoginRequest(platform, ip))

        // ✅ GUARDAR el AppSession en secure storage
        secure.saveAppSession(app)

        _sessionState.value = SessionState.AppSignedIn(app)
        _currentUserType.value = app.roles.toUserType()
        _currentCompanyId.value = app.companyId
        _currentUsername.value = getCurrentUsername()

        Log.d("AuthSessionStore", "🔐 App login exitoso - CompanyId: ${app.companyId}, UserType: ${app.roles.toUserType()}")

        val webSocketUserType = app.roles.toWebSocketUserType()
        if (webSocketUserType != null) {
            Log.d("AuthSessionStore", "🔌 Conectando WebSocket para $webSocketUserType con companyId=${app.companyId}")
            webSocketManager.connect(
                iamToken = app.accessToken,
                userType = webSocketUserType,
                companyId = app.companyId
            )

        } else {
            Log.w("AuthSessionStore", "⚠️ No se pudo determinar el tipo de usuario para WebSocket")
        }
    }

    override suspend fun logout() {



        webSocketManager.disconnect()

        secure.clearAppSession()
        secure.clearFirebaseSession()
        local.clearAllAuthData()
        _sessionState.value = SessionState.SignedOut
        _currentUserType.value = null
        _currentCompanyId.value = null
        _currentUsername.value = null


    }
}