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

    // ---- helpers
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
        val app = secure.getAppSession()
        if (app != null && !isExpired(app.expiresAt)) {
            _sessionState.value = SessionState.AppSignedIn(app)
            _currentUserType.value = app.roles.toUserType()
            _currentCompanyId.value = app.companyId
            _currentUsername.value = getCurrentUsername()
            return
        }
        val fb = secure.getFirebaseSession()
        if (fb != null && !isExpired(fb.expiresAt)) {
            _sessionState.value = SessionState.FirebaseOnly(fb)
            _currentUserType.value = null
            _currentCompanyId.value = null
            _currentUsername.value = null
        } else {
            _sessionState.value = SessionState.SignedOut
            _currentUserType.value = null
            _currentCompanyId.value = null
            _currentUsername.value = null
        }
    }

    override suspend fun bootstrap() {
        emitFromStorageOrSignedOut()
    }

    override suspend fun signInManually(
        email: Email, 
        password: Password, 
        platform: Platform, 
        ip: String
    ) {
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
        Log.d("AuthSessionStore", "🚀 Iniciando login de aplicación...")
        Log.d("AuthSessionStore", "📱 Plataforma: $platform")
        Log.d("AuthSessionStore", "🌐 IP: $ip")
        
        val fb = secure.getFirebaseSession()
            ?: throw IllegalStateException("No Firebase session for app login")
        
        Log.d("AuthSessionStore", "✅ Sesión Firebase encontrada")
        
        // AuthRemoteRepositoryImpl ya persiste AppSession + AccountSnapshot
        val app = auth.login(AppLoginRequest(platform, ip))
        
        Log.d("AuthSessionStore", "✅ Login backend exitoso")
        Log.d("AuthSessionStore", "🔑 Token obtenido: ${app.accessToken.take(20)}...")
        Log.d("AuthSessionStore", "👤 Roles: ${app.roles}")
        Log.d("AuthSessionStore", "🏢 Company ID: ${app.companyId}")

        _sessionState.value = SessionState.AppSignedIn(app)
        _currentUserType.value = app.roles.toUserType()
        _currentCompanyId.value = app.companyId
        _currentUsername.value = getCurrentUsername()
        
        // 🆕 CONECTAR WEBSOCKET INMEDIATAMENTE DESPUÉS DEL LOGIN
        val webSocketUserType = app.roles.toWebSocketUserType()
        if (webSocketUserType != null) {
            Log.d("AuthSessionStore", "🔌 Conectando WebSocket para usuario: $webSocketUserType")
            webSocketManager.connect(
                iamToken = app.accessToken,
                userType = webSocketUserType,
                companyId = app.companyId
            )
            
            Log.d("AuthSessionStore", "✅ WebSocket iniciado correctamente")
        } else {
            Log.w("AuthSessionStore", "⚠️ No se pudo determinar el tipo de usuario para WebSocket")
        }
    }

    override suspend fun logout() {
        Log.d("AuthSessionStore", "🚪 Iniciando logout...")
        
        // Desconectar WebSocket
        Log.d("AuthSessionStore", "🔌 Desconectando WebSocket...")
        webSocketManager.disconnect()
        
        secure.clearAppSession()
        secure.clearFirebaseSession()
        local.clearAllAuthData()
        _sessionState.value = SessionState.SignedOut
        _currentUserType.value = null
        _currentCompanyId.value = null
        _currentUsername.value = null
        
        Log.d("AuthSessionStore", "✅ Logout completado")
    }
}