package com.wapps1.redcarga.core.websocket

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import com.wapps1.redcarga.features.chat.data.remote.models.ChatMessageDto
import okhttp3.*
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RedcargaWebSocketManager @Inject constructor() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var webSocket: WebSocket? = null
    private var iamToken: String? = null
    private var isConnected = false
    private var isStompConnected = false
    private var userType: WebSocketUserType? = null
    private var companyId: Long? = null
    private var accountId: Long? = null

    private var reconnectAttempts = 0
    private val maxReconnectAttempts = 5
    private var isManualDisconnect = false

    private val _connectionState = MutableStateFlow<WebSocketState>(WebSocketState.Disconnected)
    val connectionState: StateFlow<WebSocketState> = _connectionState.asStateFlow()

    private val _receivedMessages = MutableStateFlow<List<WebSocketMessage>>(emptyList())
    val receivedMessages: StateFlow<List<WebSocketMessage>> = _receivedMessages.asStateFlow()

    // ⭐ CHAT: Map de suscripciones activas de chat (quoteId -> subscriptionId)
    private val activeChatSubscriptions = mutableMapOf<Long, String>()

    // ⭐ CHAT: Flow específico para mensajes de chat por quoteId
    private val _chatMessages = MutableStateFlow<Map<Long, ChatMessageDto>>(emptyMap())
    val chatMessages: StateFlow<Map<Long, ChatMessageDto>> = _chatMessages.asStateFlow()

    /**
     * ⭐ CHAT: Obtiene un Flow de mensajes de chat para un quoteId específico
     * Retorna el último mensaje recibido para ese chat
     */
    fun getChatMessageFlow(quoteId: Long): Flow<ChatMessageDto?> {
        return chatMessages.map { it[quoteId] }
    }

    fun connect(iamToken: String, userType: WebSocketUserType, companyId: Long? = null, accountId: Long? = null) {
        Log.d("WebSocketManager", "🔌 Iniciando conexión WebSocket...")
        Log.d("WebSocketManager", "👤 Tipo de usuario: $userType")
        Log.d("WebSocketManager", "🏢 Company ID: $companyId")
        Log.d("WebSocketManager", "👤 Account ID: $accountId")

        this.iamToken = iamToken
        this.userType = userType
        this.companyId = companyId
        this.accountId = accountId

        val encodedToken = iamToken.replace("+", "%2B").replace("/", "%2F").replace("=", "%3D")
        val wsUrl = "wss://redcargabk-b4b7cng3ftb2bfea.canadacentral-01.azurewebsites.net/ws?access_token=$encodedToken"
        Log.d("WebSocketManager", "🌐 URL WebSocket: $wsUrl")

        val request = Request.Builder()
            .url(wsUrl)
            .build()

        val client = OkHttpClient.Builder()
            .pingInterval(10, java.util.concurrent.TimeUnit.SECONDS)
            .build()

        webSocket = client.newWebSocket(request, createListener())
        _connectionState.value = WebSocketState.Connecting
    }

    private fun sendStompConnect() {
        if (!isConnected) {
            Log.w("WebSocketManager", "⚠️ WebSocket no conectado, no se puede enviar CONNECT")
            return
        }

        val connectMessage = buildString {
            append("CONNECT\n")
            append("accept-version:1.1,1.2\n")
            append("\n")
            append("\u0000")
        }

        Log.d("WebSocketManager", "📤 Enviando STOMP CONNECT...")
        Log.d("WebSocketManager", "📝 Mensaje (sin NULL): ${connectMessage.replace("\u0000", "\\0").replace("\n", "\\n")}")

        webSocket?.send(connectMessage)
    }

    fun subscribeToCompanyNotifications(companyId: Long) {
        if (!isStompConnected) {
            Log.w("WebSocketManager", "⚠️ STOMP no conectado, no se puede suscribir. Estado: isConnected=$isConnected, isStompConnected=$isStompConnected")
            return
        }

        val destination = "/topic/planning/company.$companyId.solicitudes"

        val subscribeMessage = buildString {
            append("SUBSCRIBE\n")
            append("id:sub-${System.currentTimeMillis()}\n")
            append("destination:$destination\n")
            append("\n")
            append("\u0000")
        }

        Log.d("WebSocketManager", "📡 Suscribiéndose a canal: $destination")
        Log.d("WebSocketManager", "📤 Mensaje STOMP SUBSCRIBE (sin NULL): ${subscribeMessage.replace("\u0000", "\\0").replace("\n", "\\n")}")

        webSocket?.send(subscribeMessage)
    }

    fun subscribeToClientQuotes(accountId: Long) {
        if (!isStompConnected) {
            Log.w("WebSocketManager", "⚠️ STOMP no conectado, no se puede suscribir a quotes")
            return
        }

        val destination = "/topic/requests.account.$accountId.quotes"

        val subscribeMessage = buildString {
            append("SUBSCRIBE\n")
            append("id:sub-quotes-${System.currentTimeMillis()}\n")
            append("destination:$destination\n")
            append("\n")
            append("\u0000")
        }

        Log.d("WebSocketManager", "📡 Suscribiéndose a cotizaciones: $destination")
        Log.d("WebSocketManager", "📤 Mensaje STOMP SUBSCRIBE (sin NULL): ${subscribeMessage.replace("\u0000", "\\0").replace("\n", "\\n")}")

        webSocket?.send(subscribeMessage)
    }

    fun subscribeToSystemErrors() {
        if (!isStompConnected) {
            Log.w("WebSocketManager", "⚠️ STOMP no conectado, no se puede suscribir a errores")
            return
        }

        val destination = "/user/queue/system/errors"

        val subscribeMessage = buildString {
            append("SUBSCRIBE\n")
            append("id:sub-errors-${System.currentTimeMillis()}\n")
            append("destination:$destination\n")
            append("\n")
            append("\u0000")
        }

        Log.d("WebSocketManager", "📡 Suscribiéndose a errores del sistema: $destination")

        webSocket?.send(subscribeMessage)
    }

    /**
     * ⭐ CHAT: Suscribirse a un chat específico
     * @param quoteId ID de la cotización
     * @return subscriptionId si la suscripción fue exitosa, null si no
     */
    fun subscribeToChat(quoteId: Long): String? {
        if (!isStompConnected) {
            Log.w("WebSocketManager", "⚠️ STOMP no conectado, no se puede suscribir al chat")
            return null
        }

        // Verificar si ya está suscrito
        if (activeChatSubscriptions.containsKey(quoteId)) {
            Log.d("WebSocketManager", "💬 Ya está suscrito al chat de quoteId=$quoteId")
            return activeChatSubscriptions[quoteId]
        }

        val destination = "/topic/deals.quotes.$quoteId.chat"
        val subscriptionId = "sub-chat-$quoteId-${System.currentTimeMillis()}"

        val subscribeMessage = buildString {
            append("SUBSCRIBE\n")
            append("id:$subscriptionId\n")
            append("destination:$destination\n")
            append("\n")
            append("\u0000")
        }

        Log.d("WebSocketManager", "💬 Suscribiéndose a chat:")
        Log.d("WebSocketManager", "   QuoteId: $quoteId")
        Log.d("WebSocketManager", "   Destination: $destination")
        Log.d("WebSocketManager", "   SubscriptionId: $subscriptionId")
        Log.d("WebSocketManager", "   Total suscripciones activas: ${activeChatSubscriptions.size + 1}")
        Log.d("WebSocketManager", "📤 Mensaje STOMP SUBSCRIBE (sin NULL): ${subscribeMessage.replace("\u0000", "\\0").replace("\n", "\\n")}")

        webSocket?.send(subscribeMessage)
        activeChatSubscriptions[quoteId] = subscriptionId

        return subscriptionId
    }

    /**
     * ⭐ CHAT: Desuscribirse de un chat específico
     * @param quoteId ID de la cotización
     */
    fun unsubscribeFromChat(quoteId: Long) {
        if (!isStompConnected) {
            Log.w("WebSocketManager", "⚠️ STOMP no conectado, no se puede desuscribir del chat")
            return
        }

        val subscriptionId = activeChatSubscriptions.remove(quoteId)
        if (subscriptionId == null) {
            Log.d("WebSocketManager", "💬 No hay suscripción activa para quoteId=$quoteId")
            return
        }

        val unsubscribeMessage = buildString {
            append("UNSUBSCRIBE\n")
            append("id:$subscriptionId\n")
            append("\n")
            append("\u0000")
        }

        Log.d("WebSocketManager", "💬 Desuscribiéndose del chat quoteId=$quoteId, subscriptionId=$subscriptionId")
        Log.d("WebSocketManager", "📤 Mensaje STOMP UNSUBSCRIBE (sin NULL): ${unsubscribeMessage.replace("\u0000", "\\0").replace("\n", "\\n")}")

        webSocket?.send(unsubscribeMessage)
    }

    fun disconnect() {
        Log.d("WebSocketManager", "🔌 Desconectando WebSocket...")

        isManualDisconnect = true
        reconnectAttempts = 0

        val ws = webSocket

        if (isStompConnected && ws != null) {
            val disconnectMessage = buildString {
                append("DISCONNECT\n")
                append("\n")
                append("\u0000")
            }
            Log.d("WebSocketManager", "📤 Enviando STOMP DISCONNECT...")
            ws.send(disconnectMessage)

            scope.launch {
                delay(100)
                try {
                    ws.close(1000, "Desconexión manual")
                } catch (e: Exception) {
                    Log.d("WebSocketManager", "⚠️ WebSocket ya estaba cerrado: ${e.message}")
                }
            }
        } else {
            try {
                ws?.close(1000, "Desconexión manual")
            } catch (e: Exception) {
                Log.d("WebSocketManager", "⚠️ WebSocket ya estaba cerrado: ${e.message}")
            }
        }

        webSocket = null
        isConnected = false
        isStompConnected = false
        
        // ⭐ CHAT: Limpiar suscripciones de chat al desconectar
        activeChatSubscriptions.clear()
        _chatMessages.value = emptyMap()
        
        _connectionState.value = WebSocketState.Disconnected
    }

    private fun createListener() = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            isConnected = true
            _connectionState.value = WebSocketState.Connecting

            reconnectAttempts = 0
            isManualDisconnect = false

            Log.d("WebSocketManager", "✅ WebSocket handshake completado")
            Log.d("WebSocketManager", "📊 Response code: ${response.code}")
            Log.d("WebSocketManager", "📋 Response headers: ${response.headers}")

            sendStompConnect()
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.d("WebSocketManager", "📨 Mensaje recibido (raw): ${text.take(200)}...")

            val stompFrame = parseStompFrame(text)

            Log.d("WebSocketManager", "📦 Frame STOMP parseado:")
            Log.d("WebSocketManager", "   Comando: ${stompFrame.command}")
            Log.d("WebSocketManager", "   Headers: ${stompFrame.headers}")
            Log.d("WebSocketManager", "   Body (primeros 200 chars): ${stompFrame.body.take(200)}")

            when (stompFrame.command) {
                "CONNECTED" -> {
                    isStompConnected = true
                    _connectionState.value = WebSocketState.Connected

                    Log.d("WebSocketManager", "✅✅ STOMP CONNECTED - Ahora podemos suscribirnos")
                    Log.d("WebSocketManager", "   Versión: ${stompFrame.headers["version"] ?: "unknown"}")
                    Log.d("WebSocketManager", "   Server: ${stompFrame.headers["server"] ?: "unknown"}")

                    subscribeToSystemErrors()

                    if (userType == WebSocketUserType.PROVIDER && companyId != null) {
                        Log.d("WebSocketManager", "🏢 Usuario PROVEEDOR - Suscribiéndose a empresa $companyId")
                        subscribeToCompanyNotifications(companyId!!)
                    } else if (userType == WebSocketUserType.CLIENT && accountId != null) {
                        Log.d("WebSocketManager", "👤 Usuario CLIENTE - Suscribiéndose a cotizaciones")
                        subscribeToClientQuotes(accountId!!)
                    } else {
                        Log.d("WebSocketManager", "⚠️ No hay suscripciones automáticas configuradas")
                    }

                    // ⭐ CHAT: Re-suscribirse a todos los chats activos después de reconectar
                    scope.launch {
                        delay(500) // Esperar un poco después de CONNECTED
                        val quoteIdsToResubscribe = activeChatSubscriptions.keys.toList()
                        if (quoteIdsToResubscribe.isNotEmpty()) {
                            Log.d("WebSocketManager", "💬 Re-suscribiéndose a ${quoteIdsToResubscribe.size} chats activos...")
                            quoteIdsToResubscribe.forEach { quoteId ->
                                subscribeToChat(quoteId)
                            }
                        }
                    }
                }
                "MESSAGE" -> {
                    val destination = stompFrame.headers["destination"] ?: ""
                    val subscriptionId = stompFrame.headers["subscription"] ?: ""
                    val body = stompFrame.body

                    Log.d("WebSocketManager", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    Log.d("WebSocketManager", "📬 STOMP MESSAGE RECIBIDO (WebSocket)")
                    Log.d("WebSocketManager", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    Log.d("WebSocketManager", "   Destination: $destination")
                    Log.d("WebSocketManager", "   Subscription ID: $subscriptionId")
                    Log.d("WebSocketManager", "   Body length: ${body.length} chars")
                    Log.d("WebSocketManager", "   Body completo (RAW):")
                    Log.d("WebSocketManager", "   ┌─────────────────────────────────────────────────────────")
                    Log.d("WebSocketManager", "   │ $body")
                    Log.d("WebSocketManager", "   └─────────────────────────────────────────────────────────")

                    if (destination == "/user/queue/system/errors") {
                        Log.e("WebSocketManager", "❌ Error del sistema recibido: $body")
                        
                        // ⭐ CHAT: Intentar extraer información del error y limpiar suscripciones rechazadas
                        try {
                            val errorJson = JSONObject(body)
                            val reason = errorJson.optString("reason", "Error desconocido")
                            val dest = errorJson.optString("destination", "")
                            
                            // Si es un error de suscripción a chat, remover de activeChatSubscriptions
                            if (dest.contains("/topic/deals.quotes.") && dest.contains(".chat")) {
                                val quoteId = extractQuoteIdFromChatDestination(dest)
                                if (quoteId != null) {
                                    activeChatSubscriptions.remove(quoteId)
                                    Log.w("WebSocketManager", "⚠️ Suscripción rechazada para chat quoteId=$quoteId: $reason")
                                }
                            }
                        } catch (e: Exception) {
                            Log.d("WebSocketManager", "⚠️ No se pudo parsear error: ${e.message}")
                        }
                        
                        val errorMessage = body.ifEmpty { "Error desconocido del sistema" }
                        _connectionState.value = WebSocketState.Error(errorMessage)
                        return@onMessage
                    }

                    val messageType = parseMessageType(body, destination)

                    Log.d("WebSocketManager", "   Tipo detectado: $messageType")

                    // ⭐ CHAT: Procesar mensajes de chat
                    if (messageType == WebSocketMessageType.CHAT_MESSAGE) {
                        try {
                            val quoteId = extractQuoteIdFromChatDestination(destination)
                            if (quoteId != null) {
                                val chatMessageDto = parseChatMessageDto(body)
                                if (chatMessageDto != null) {
                                    Log.d("WebSocketManager", "💬 Mensaje de chat recibido para quoteId=$quoteId: messageId=${chatMessageDto.messageId}")
                                    
                                    // Emitir al Flow específico de chat
                                    _chatMessages.value = _chatMessages.value + (quoteId to chatMessageDto)
                                } else {
                                    Log.w("WebSocketManager", "⚠️ No se pudo parsear el mensaje de chat")
                                }
                            } else {
                                Log.w("WebSocketManager", "⚠️ No se pudo extraer quoteId del destination: $destination")
                            }
                        } catch (e: Exception) {
                            Log.e("WebSocketManager", "❌ Error al procesar mensaje de chat: ${e.message}", e)
                        }
                    }

                    if (messageType == WebSocketMessageType.QUOTE_CREATED) {
                        Log.d("WebSocketManager", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                        Log.d("WebSocketManager", "💰💰💰 QUOTE_CREATED DETECTADO EN WEBSOCKET 💰💰💰")
                        Log.d("WebSocketManager", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                        Log.d("WebSocketManager", "   Destination: $destination")
                        Log.d("WebSocketManager", "   Mensaje JSON completo:")
                        try {
                            val json = JSONObject(body)
                            Log.d("WebSocketManager", "   ┌─────────────────────────────────────────────────────────")
                            json.keys().forEach { key ->
                                Log.d("WebSocketManager", "   │ $key: ${json.opt(key)}")
                            }
                            Log.d("WebSocketManager", "   └─────────────────────────────────────────────────────────")
                        } catch (e: Exception) {
                            Log.d("WebSocketManager", "   ⚠️ No se pudo parsear como JSON: ${e.message}")
                            Log.d("WebSocketManager", "   Body raw: $body")
                        }
                        Log.d("WebSocketManager", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    }

                    val message = WebSocketMessage(
                        content = body,
                        timestamp = System.currentTimeMillis(),
                        type = messageType
                    )

                    scope.launch {
                        val currentMessages = _receivedMessages.value.toMutableList()
                        currentMessages.add(message)
                        if (currentMessages.size > 100) {
                            currentMessages.removeAt(0)
                        }
                        _receivedMessages.value = currentMessages
                    }
                }
                "ERROR" -> {
                    Log.e("WebSocketManager", "❌ STOMP ERROR recibido")
                    Log.e("WebSocketManager", "   Headers: ${stompFrame.headers}")
                    Log.e("WebSocketManager", "   Body: ${stompFrame.body}")

                    val errorMessage = stompFrame.body.ifEmpty {
                        stompFrame.headers["message"] ?: "Error desconocido del servidor"
                    }

                    _connectionState.value = WebSocketState.Error(errorMessage)
                    isStompConnected = false
                }
                "RECEIPT" -> {
                    val receiptId = stompFrame.headers["receipt-id"] ?: ""
                    Log.d("WebSocketManager", "✅ STOMP RECEIPT recibido: $receiptId")
                }
                else -> {
                    Log.d("WebSocketManager", "📝 Comando STOMP desconocido: ${stompFrame.command}")
                }
            }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            isConnected = false
            isStompConnected = false
            _connectionState.value = WebSocketState.Error(t.message ?: "Error desconocido")

            Log.e("WebSocketManager", "❌ Error de conexión WebSocket: ${t.message}")
            Log.e("WebSocketManager", "📊 Response: ${response?.code} - ${response?.message}")
            Log.e("WebSocketManager", "🔍 Stack trace:", t)

            if (!isManualDisconnect && reconnectAttempts < maxReconnectAttempts && iamToken != null && userType != null) {
                reconnectAttempts++
                val backoffDelay = 2000L * reconnectAttempts

                Log.d("WebSocketManager", "🔄 Intentando reconexión automática (intento $reconnectAttempts/$maxReconnectAttempts) en ${backoffDelay}ms...")

                scope.launch {
                    delay(backoffDelay)
                    if (!isManualDisconnect) {
                        connect(iamToken!!, userType!!, companyId, accountId)
                    }
                }
            } else if (reconnectAttempts >= maxReconnectAttempts) {
                Log.e("WebSocketManager", "❌ Máximo de intentos de reconexión alcanzado ($maxReconnectAttempts)")
            }
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            isConnected = false
            isStompConnected = false
            _connectionState.value = WebSocketState.Disconnected

            Log.d("WebSocketManager", "🔌 WebSocket cerrado")
            Log.d("WebSocketManager", "📊 Código: $code")
            Log.d("WebSocketManager", "📝 Razón: $reason")

            if (!isManualDisconnect && code != 1000 && reconnectAttempts < maxReconnectAttempts && iamToken != null && userType != null) {
                reconnectAttempts++
                val backoffDelay = 2000L * reconnectAttempts

                Log.d("WebSocketManager", "🔄 Reconectando después de cierre inesperado (código: $code, intento $reconnectAttempts/$maxReconnectAttempts) en ${backoffDelay}ms...")

                scope.launch {
                    delay(backoffDelay)
                    if (!isManualDisconnect) {
                        connect(iamToken!!, userType!!, companyId, accountId)
                    }
                }
            } else if (code == 1000) {
                Log.d("WebSocketManager", "✅ Cierre normal (código 1000), no se reconectará")
            } else if (isManualDisconnect) {
                Log.d("WebSocketManager", "✅ Desconexión manual, no se reconectará")
            }
        }
    }

    private fun parseStompFrame(text: String): StompFrame {
        if (text.isEmpty()) {
            return StompFrame("UNKNOWN", emptyMap(), "")
        }

        val nullIndex = text.indexOf('\u0000')
        val content = if (nullIndex >= 0) text.substring(0, nullIndex) else text

        val lines = content.split("\n")
        if (lines.isEmpty()) {
            return StompFrame("UNKNOWN", emptyMap(), "")
        }

        val command = lines[0].trim()
        val headers = mutableMapOf<String, String>()
        var bodyStartIndex = 1

        for (i in 1 until lines.size) {
            val line = lines[i]
            if (line.isEmpty() || line.trim().isEmpty()) {
                bodyStartIndex = i + 1
                break
            }
            val colonIndex = line.indexOf(':')
            if (colonIndex > 0) {
                val key = line.substring(0, colonIndex).trim()
                val value = line.substring(colonIndex + 1).trim()
                headers[key] = value
            }
        }

        val body = if (bodyStartIndex < lines.size) {
            lines.subList(bodyStartIndex, lines.size).joinToString("\n")
        } else {
            ""
        }

        return StompFrame(command, headers, body)
    }

    private data class StompFrame(
        val command: String,
        val headers: Map<String, String>,
        val body: String
    )


    private fun parseMessageType(message: String, destination: String = ""): WebSocketMessageType {
        // ⭐ CHAT: Detectar mensajes de chat por destination PRIMERO (antes de parsear JSON)
        if (destination.contains("/topic/deals.quotes.") && destination.contains(".chat")) {
            Log.d("WebSocketManager", "💬 Mensaje de chat detectado por destination: $destination")
            return WebSocketMessageType.CHAT_MESSAGE
        }

        return try {
            if (message.trim().startsWith("{") && message.trim().endsWith("}")) {
                val json = JSONObject(message)

                val type = json.optString("type", "")
                if (type.isNotEmpty()) {
                    return when (type) {
                        "NEW_REQUEST" -> WebSocketMessageType.NEW_REQUEST
                        "QUOTE_CREATED" -> WebSocketMessageType.QUOTE_CREATED
                        "QUOTE_ACCEPTED" -> WebSocketMessageType.QUOTE_ACCEPTED
                        "QUOTE_REJECTED" -> WebSocketMessageType.QUOTE_REJECTED
                        else -> WebSocketMessageType.UNKNOWN
                    }
                }

                if (destination.contains("quotes") && destination.contains("account")) {
                    val quoteId = json.optLong("quoteId", -1L)
                    val requestId = json.optLong("requestId", -1L)

                    if (quoteId > 0 && requestId > 0) {
                        Log.d("WebSocketManager", "✅✅ Detectado QUOTE_CREATED por canal y campos (quoteId=$quoteId, requestId=$requestId)")
                        return WebSocketMessageType.QUOTE_CREATED
                    }
                }

                if (destination.contains("solicitudes") || destination.contains("planning")) {
                    val requestId = json.optLong("requestId", -1L)
                    if (requestId > 0) {
                        Log.d("WebSocketManager", "✅✅ Detectado NEW_REQUEST por canal y campos (requestId=$requestId)")
                        return WebSocketMessageType.NEW_REQUEST
                    }
                }

                val quoteId = json.optLong("quoteId", -1L)
                val requestId = json.optLong("requestId", -1L)

                if (quoteId > 0 && requestId > 0) {
                    Log.d("WebSocketManager", "✅ Detectado QUOTE_CREATED por campos JSON (quoteId=$quoteId, requestId=$requestId)")
                    return WebSocketMessageType.QUOTE_CREATED
                }

                WebSocketMessageType.UNKNOWN
            } else {
                parseMessageTypeFallback(message)
            }
        } catch (e: Exception) {
            Log.d("WebSocketManager", "⚠️ No se pudo parsear como JSON, usando fallback: ${e.message}")
            parseMessageTypeFallback(message)
        }
    }

    private fun parseMessageTypeFallback(message: String): WebSocketMessageType {
        return when {
            message.contains("\"type\":\"NEW_REQUEST\"") -> WebSocketMessageType.NEW_REQUEST
            message.contains("\"type\":\"QUOTE_CREATED\"") -> WebSocketMessageType.QUOTE_CREATED
            message.contains("\"type\":\"QUOTE_ACCEPTED\"") -> WebSocketMessageType.QUOTE_ACCEPTED
            message.contains("\"type\":\"QUOTE_REJECTED\"") -> WebSocketMessageType.QUOTE_REJECTED
            message.contains("NEW_REQUEST") -> WebSocketMessageType.NEW_REQUEST
            message.contains("QUOTE_CREATED") -> WebSocketMessageType.QUOTE_CREATED
            message.contains("QUOTE_ACCEPTED") -> WebSocketMessageType.QUOTE_ACCEPTED
            message.contains("QUOTE_REJECTED") -> WebSocketMessageType.QUOTE_REJECTED
            message.contains("SUBSCRIBE") -> WebSocketMessageType.SUBSCRIBE
            message.contains("CONNECTED") -> WebSocketMessageType.CONNECTED
            message.contains("ERROR") -> WebSocketMessageType.ERROR
            else -> WebSocketMessageType.UNKNOWN
        }
    }

    /**
     * ⭐ CHAT: Extrae el quoteId del destination del chat
     * Ejemplo: /topic/deals.quotes.14.chat -> 14
     */
    private fun extractQuoteIdFromChatDestination(destination: String): Long? {
        return try {
            val pattern = Regex("/topic/deals\\.quotes\\.(\\d+)\\.chat")
            val match = pattern.find(destination)
            match?.groupValues?.get(1)?.toLongOrNull()
        } catch (e: Exception) {
            Log.e("WebSocketManager", "❌ Error al extraer quoteId de destination: $destination", e)
            null
        }
    }

    /**
     * ⭐ CHAT: Parsea un mensaje de chat desde JSON a ChatMessageDto
     * Maneja diferencias entre REST y WebSocket:
     * - WebSocket: messageId puede ser null, system_subtype_code (snake_case), contentCode puede ser "CHANGE"
     * - REST: messageId siempre presente, systemSubtypeCode (camelCase), contentCode "TEXT" o "IMAGE"
     */
    private fun parseChatMessageDto(jsonBody: String): ChatMessageDto? {
        return try {
            val json = JSONObject(jsonBody)
            
            // ⭐ Validar campos requeridos (messageId es opcional en WebSocket)
            if (!json.has("quoteId") || !json.has("typeCode") ||
                !json.has("contentCode") || !json.has("createdBy") || !json.has("createdAt")) {
                Log.w("WebSocketManager", "⚠️ Mensaje de chat inválido: faltan campos requeridos")
                Log.w("WebSocketManager", "   JSON: ${jsonBody.take(500)}")
                return null
            }
            
            // ⭐ messageId puede ser null en WebSocket (se genera al guardar en BD)
            val messageId = if (json.has("messageId") && !json.isNull("messageId")) {
                json.getInt("messageId")
            } else {
                Log.d("WebSocketManager", "⚠️ messageId es null (normal en WebSocket)")
                0 // Valor temporal, se manejará en el dominio
            }
            
            // ⭐ Buscar systemSubtypeCode en ambos formatos (snake_case y camelCase)
            val systemSubtypeCode = when {
                json.has("system_subtype_code") && !json.isNull("system_subtype_code") -> {
                    json.optString("system_subtype_code").takeIf { it.isNotEmpty() && it != "null" }
                }
                json.has("systemSubtypeCode") && !json.isNull("systemSubtypeCode") -> {
                    json.optString("systemSubtypeCode").takeIf { it.isNotEmpty() && it != "null" }
                }
                else -> null
            }
            
            // ⭐ Parsear info correctamente (mantener como Any? para que el mapper lo procese)
            val infoValue: Any? = if (json.has("info") && !json.isNull("info")) {
                val infoObj = json.optJSONObject("info")
                if (infoObj != null) {
                    // Convertir JSONObject a Map para mantener como Any?
                    val infoMap = mutableMapOf<String, Any?>()
                    val keys = infoObj.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        val value = infoObj.get(key)
                        infoMap[key] = when (value) {
                            is JSONObject -> {
                                // Convertir JSONObject anidado a Map recursivamente
                                convertJsonObjectToMap(value)
                            }
                            is org.json.JSONArray -> {
                                // Convertir JSONArray a List
                                convertJsonArrayToList(value)
                            }
                            else -> value
                        }
                    }
                    infoMap
                } else {
                    // Si es un string directo (poco probable)
                    json.optString("info").takeIf { it.isNotEmpty() && it != "null" }
                }
            } else {
                null
            }
            
            Log.d("WebSocketManager", "💬 Parseando mensaje:")
            Log.d("WebSocketManager", "   messageId=$messageId (${if (messageId == 0) "null en WebSocket" else "presente"})")
            Log.d("WebSocketManager", "   systemSubtypeCode=$systemSubtypeCode")
            Log.d("WebSocketManager", "   contentCode=${json.getString("contentCode")}")
            Log.d("WebSocketManager", "   info type: ${infoValue?.javaClass?.simpleName}")
            
            ChatMessageDto(
                messageId = messageId,
                quoteId = json.getInt("quoteId"),
                typeCode = json.getString("typeCode"),
                contentCode = json.getString("contentCode"), // Puede ser "CHANGE" en WebSocket
                body = json.optString("body").takeIf { it.isNotEmpty() && it != "null" },
                mediaUrl = json.optString("mediaUrl").takeIf { it.isNotEmpty() && it != "null" },
                clientDedupKey = json.optString("clientDedupKey").takeIf { 
                    it.isNotEmpty() && it != "null" 
                },
                createdBy = json.getInt("createdBy"),
                createdAt = json.getString("createdAt"),
                systemSubtypeCode = systemSubtypeCode,
                info = infoValue // ⚠️ Mantener como Any? (Map o String)
            )
        } catch (e: Exception) {
            Log.e("WebSocketManager", "❌ Error al parsear ChatMessageDto: ${e.message}", e)
            Log.e("WebSocketManager", "   JSON body: ${jsonBody.take(500)}")
            null
        }
    }
    
    /**
     * Helper para convertir JSONObject a Map recursivamente
     */
    private fun convertJsonObjectToMap(jsonObj: JSONObject): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>()
        val keys = jsonObj.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val value = jsonObj.get(key)
            map[key] = when (value) {
                is JSONObject -> convertJsonObjectToMap(value)
                is org.json.JSONArray -> convertJsonArrayToList(value)
                org.json.JSONObject.NULL -> null
                else -> value
            }
        }
        return map
    }
    
    /**
     * Helper para convertir JSONArray a List recursivamente
     */
    private fun convertJsonArrayToList(jsonArray: org.json.JSONArray): List<Any?> {
        val list = mutableListOf<Any?>()
        for (i in 0 until jsonArray.length()) {
            val value = jsonArray.get(i)
            list.add(when (value) {
                is JSONObject -> convertJsonObjectToMap(value)
                is org.json.JSONArray -> convertJsonArrayToList(value)
                org.json.JSONObject.NULL -> null
                else -> value
            })
        }
        return list
    }
}

sealed class WebSocketState {
    object Disconnected : WebSocketState()
    object Connecting : WebSocketState()
    object Connected : WebSocketState()
    data class Error(val message: String) : WebSocketState()
}

enum class WebSocketUserType {
    CLIENT,
    PROVIDER
}

data class WebSocketMessage(
    val content: String,
    val timestamp: Long,
    val type: WebSocketMessageType
)

enum class WebSocketMessageType {
    NEW_REQUEST,
    QUOTE_CREATED,
    QUOTE_ACCEPTED,
    QUOTE_REJECTED,
    CHAT_MESSAGE,  // ⭐ CHAT: Mensaje de chat en tiempo real
    SUBSCRIBE,
    CONNECTED,
    ERROR,
    UNKNOWN
}
