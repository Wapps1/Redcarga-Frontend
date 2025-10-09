package com.wapps1.redcarga.core.websocket

import android.util.Log
import com.wapps1.redcarga.features.auth.domain.models.value.RoleCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.*
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager para conexiones WebSocket con el backend
 * Maneja conexión, suscripciones y mensajes en tiempo real
 */
@Singleton
class RedcargaWebSocketManager @Inject constructor() {
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    private var webSocket: WebSocket? = null
    private var iamToken: String? = null
    private var isConnected = false
    private var userType: WebSocketUserType? = null
    private var companyId: Long? = null
    
    private val _connectionState = MutableStateFlow<WebSocketState>(WebSocketState.Disconnected)
    val connectionState: StateFlow<WebSocketState> = _connectionState.asStateFlow()
    
    private val _receivedMessages = MutableStateFlow<List<WebSocketMessage>>(emptyList())
    val receivedMessages: StateFlow<List<WebSocketMessage>> = _receivedMessages.asStateFlow()
    
    /**
     * Conecta al WebSocket con el token IAM
     */
    fun connect(iamToken: String, userType: WebSocketUserType, companyId: Long? = null) {
        Log.d("WebSocketManager", "🔌 Iniciando conexión WebSocket...")
        Log.d("WebSocketManager", "👤 Tipo de usuario: $userType")
        Log.d("WebSocketManager", "🏢 Company ID: $companyId")
        
        this.iamToken = iamToken
        this.userType = userType
        this.companyId = companyId
        
        val wsUrl = "ws://10.0.2.2:8080/ws?access_token=${URLEncoder.encode(iamToken, "UTF-8")}"
        Log.d("WebSocketManager", "🌐 URL WebSocket: $wsUrl")
        
        val request = Request.Builder()
            .url(wsUrl)
            .build()
            
        val client = OkHttpClient.Builder()
            .pingInterval(30, java.util.concurrent.TimeUnit.SECONDS)
            .build()
            
        webSocket = client.newWebSocket(request, createListener())
        _connectionState.value = WebSocketState.Connecting
    }
    
    /**
     * Suscribe a notificaciones de empresa (solo para PROVEEDORES)
     */
    fun subscribeToCompanyNotifications(companyId: Long) {
        if (!isConnected) {
            Log.w("WebSocketManager", "⚠️ No se puede suscribir: WebSocket no conectado")
            return
        }
        
        val destination = "/topic/planning/company.$companyId.solicitudes"
        val subscribeMessage = """
            SUBSCRIBE
            id:sub-${System.currentTimeMillis()}
            destination:$destination
            
            ^@
        """.trimIndent()
        
        Log.d("WebSocketManager", "📡 Suscribiéndose a canal: $destination")
        Log.d("WebSocketManager", "📤 Mensaje de suscripción: $subscribeMessage")
        
        webSocket?.send(subscribeMessage)
    }
    
    /**
     * Desconecta el WebSocket
     */
    fun disconnect() {
        Log.d("WebSocketManager", "🔌 Desconectando WebSocket...")
        webSocket?.close(1000, "Desconexión manual")
        webSocket = null
        isConnected = false
        _connectionState.value = WebSocketState.Disconnected
    }
    
    private fun createListener() = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            isConnected = true
            _connectionState.value = WebSocketState.Connected
            
            Log.d("WebSocketManager", "✅ WebSocket conectado exitosamente!")
            Log.d("WebSocketManager", "📊 Response code: ${response.code}")
            Log.d("WebSocketManager", "📋 Response headers: ${response.headers}")
            
            // Si es PROVEEDOR, suscribirse automáticamente
            if (userType == WebSocketUserType.PROVIDER && companyId != null) {
                Log.d("WebSocketManager", "🏢 Usuario PROVEEDOR detectado, suscribiéndose a empresa $companyId")
                subscribeToCompanyNotifications(companyId!!)
            } else if (userType == WebSocketUserType.CLIENT) {
                Log.d("WebSocketManager", "👤 Usuario CLIENTE detectado, NO se suscribe a ningún canal")
            }
        }
        
        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.d("WebSocketManager", "📨 Mensaje recibido: $text")
            
            val message = WebSocketMessage(
                content = text,
                timestamp = System.currentTimeMillis(),
                type = parseMessageType(text)
            )
            
            scope.launch {
                val currentMessages = _receivedMessages.value.toMutableList()
                currentMessages.add(message)
                _receivedMessages.value = currentMessages
            }
            
            // Log detallado del mensaje
            Log.d("WebSocketManager", "📝 Tipo de mensaje: ${message.type}")
            Log.d("WebSocketManager", "⏰ Timestamp: ${message.timestamp}")
        }
        
        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            isConnected = false
            _connectionState.value = WebSocketState.Error(t.message ?: "Error desconocido")
            
            Log.e("WebSocketManager", "❌ Error de conexión WebSocket: ${t.message}")
            Log.e("WebSocketManager", "📊 Response: ${response?.code} - ${response?.message}")
            Log.e("WebSocketManager", "🔍 Stack trace:", t)
        }
        
        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            isConnected = false
            _connectionState.value = WebSocketState.Disconnected
            
            Log.d("WebSocketManager", "🔌 WebSocket cerrado")
            Log.d("WebSocketManager", "📊 Código: $code")
            Log.d("WebSocketManager", "📝 Razón: $reason")
        }
    }
    
    private fun parseMessageType(message: String): WebSocketMessageType {
        return when {
            message.contains("NEW_REQUEST") -> WebSocketMessageType.NEW_REQUEST
            message.contains("SUBSCRIBE") -> WebSocketMessageType.SUBSCRIBE
            message.contains("CONNECTED") -> WebSocketMessageType.CONNECTED
            else -> WebSocketMessageType.UNKNOWN
        }
    }
}

/**
 * Estados del WebSocket
 */
sealed class WebSocketState {
    object Disconnected : WebSocketState()
    object Connecting : WebSocketState()
    object Connected : WebSocketState()
    data class Error(val message: String) : WebSocketState()
}

/**
 * Tipo de usuario para WebSocket
 */
enum class WebSocketUserType {
    CLIENT,
    PROVIDER
}

/**
 * Mensaje recibido por WebSocket
 */
data class WebSocketMessage(
    val content: String,
    val timestamp: Long,
    val type: WebSocketMessageType
)

/**
 * Tipos de mensajes WebSocket
 */
enum class WebSocketMessageType {
    NEW_REQUEST,
    SUBSCRIBE,
    CONNECTED,
    UNKNOWN
}
