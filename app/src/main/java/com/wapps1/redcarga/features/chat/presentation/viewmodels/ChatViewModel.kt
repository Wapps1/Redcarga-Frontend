package com.wapps1.redcarga.features.chat.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wapps1.redcarga.core.session.AuthSessionStore
import com.wapps1.redcarga.core.session.SessionState
import com.wapps1.redcarga.core.session.UserType
import com.wapps1.redcarga.core.websocket.RedcargaWebSocketManager
import com.wapps1.redcarga.features.chat.data.mappers.ChatMappers.toDomain
import com.wapps1.redcarga.features.chat.domain.models.ChatHistoryResponse
import com.wapps1.redcarga.features.chat.domain.models.ChatMessage
import com.wapps1.redcarga.features.chat.domain.models.ChatMessageKind
import com.wapps1.redcarga.features.chat.domain.models.ChatMessageType
import com.wapps1.redcarga.features.chat.domain.models.SendChatMessageRequest
import android.net.Uri
import com.wapps1.redcarga.features.chat.domain.repositories.ChatRepository
import com.wapps1.redcarga.features.chat.presentation.coordination.ChatListUpdateNotifier
import com.wapps1.redcarga.features.requests.domain.repositories.MediaRepository
import com.wapps1.redcarga.features.requests.domain.repositories.QuotesRepository
import com.wapps1.redcarga.features.requests.domain.repositories.PlanningInboxRepository
import com.wapps1.redcarga.features.requests.domain.models.QuoteDetail
import com.wapps1.redcarga.features.requests.domain.models.Request
import com.wapps1.redcarga.features.deals.domain.repositories.DealsRepository
import com.wapps1.redcarga.features.deals.domain.models.ApplyChangeRequest
import com.wapps1.redcarga.features.deals.domain.DealsDomainError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

private const val TAG = "ChatViewModel"

/**
 * ViewModel para el chat individual
 */
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authSessionStore: AuthSessionStore,
    private val webSocketManager: RedcargaWebSocketManager,
    private val chatListUpdateNotifier: ChatListUpdateNotifier,
    private val mediaRepository: MediaRepository,
    private val quotesRepository: QuotesRepository,
    private val planningInboxRepository: PlanningInboxRepository,
    private val dealsRepository: DealsRepository
) : ViewModel() {

    // ⭐ CHAT: ID de suscripción al chat vía WebSocket
    private var chatSubscriptionId: String? = null
    private var currentQuoteId: Long? = null
    private var chatObservationJob: Job? = null // ⭐ Job para cancelar observación anterior

    sealed class UiState {
        object Loading : UiState()
        data class Success(
            val messages: List<ChatMessage>,
            val lastReadMessageId: Long?,
            val currentUserId: Long
        ) : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _isSendingMessage = MutableStateFlow(false)
    val isSendingMessage: StateFlow<Boolean> = _isSendingMessage.asStateFlow()

    private val _messageText = MutableStateFlow("")
    val messageText: StateFlow<String> = _messageText.asStateFlow()

    // ⭐ IMAGEN: Estado para preview de imagen seleccionada
    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri.asStateFlow()

    // ⭐ IMAGEN: Caption para la imagen
    private val _imageCaption = MutableStateFlow("")
    val imageCaption: StateFlow<String> = _imageCaption.asStateFlow()

    // ⭐ IMAGEN: Estados para subida de imágenes
    sealed class ImageUploadState {
        object Idle : ImageUploadState()
        object Uploading : ImageUploadState()
        data class Success(val imageUrl: String) : ImageUploadState()
        data class Error(val message: String) : ImageUploadState()
    }

    private val _imageUploadState = MutableStateFlow<ImageUploadState>(ImageUploadState.Idle)
    val imageUploadState: StateFlow<ImageUploadState> = _imageUploadState.asStateFlow()

    // ⭐ QUOTE: Estado para almacenar los detalles de la cotización
    private val _quoteDetail = MutableStateFlow<QuoteDetail?>(null)
    val quoteDetail: StateFlow<QuoteDetail?> = _quoteDetail.asStateFlow()

    // ⭐ REQUEST: Estado para almacenar los detalles de la solicitud
    private val _requestDetail = MutableStateFlow<Request?>(null)
    val requestDetail: StateFlow<Request?> = _requestDetail.asStateFlow()

    // ⭐ USER: Rol del usuario actualmente logueado
    val currentUserType: StateFlow<UserType?> = authSessionStore.currentUserType

    // ⭐ APPLY CHANGE: Estados para aplicar cambios a la cotización
    sealed class ApplyChangeState {
        object Idle : ApplyChangeState()
        object Loading : ApplyChangeState()
        data class Success(val changeId: Long?) : ApplyChangeState()
        data class Error(val message: String) : ApplyChangeState()
    }

    private val _applyChangeState = MutableStateFlow<ApplyChangeState>(ApplyChangeState.Idle)
    val applyChangeState: StateFlow<ApplyChangeState> = _applyChangeState.asStateFlow()

    // ⭐ ACCEPTANCE: Estados para operaciones de aceptación
    sealed class AcceptanceState {
        object Idle : AcceptanceState()
        object Loading : AcceptanceState()
        data class Success(val acceptanceId: Long?) : AcceptanceState()
        data class Error(val message: String) : AcceptanceState()
    }

    private val _acceptanceState = MutableStateFlow<AcceptanceState>(AcceptanceState.Idle)
    val acceptanceState: StateFlow<AcceptanceState> = _acceptanceState.asStateFlow()

    /**
     * Carga el historial del chat
     */
    fun loadChatHistory(quoteId: Long) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "📥 Cargando historial de chat para quoteId=$quoteId")

                // ⭐ CHAT: Desuscribirse del chat anterior si existe y es diferente
                currentQuoteId?.let { previousQuoteId ->
                    if (previousQuoteId != quoteId) {
                        webSocketManager.unsubscribeFromChat(previousQuoteId)
                        Log.d(TAG, "💬 Desuscrito del chat anterior: quoteId=$previousQuoteId")
                    }
                }

                // ⭐ CHAT: Cancelar observación anterior
                chatObservationJob?.cancel()
                chatObservationJob = null

                _uiState.value = UiState.Loading

                // Guardar el quoteId actual
                currentQuoteId = quoteId

                // Obtener accountId del usuario actual
                val currentUserId = getCurrentAccountId()
                
                // ⭐ QUOTE: Obtener detalles de la cotización en paralelo con el historial
                val (history, quoteDetail) = coroutineScope {
                    val historyDeferred = async {
                        Log.d(TAG, "📥 [loadChatHistory] Llamando a chatRepository.getChatHistory($quoteId)")
                        val historyResult = chatRepository.getChatHistory(quoteId)
                        Log.d(TAG, "📥 [loadChatHistory] Historial recibido:")
                        Log.d(TAG, "   - Total mensajes: ${historyResult.messages.size}")
                        Log.d(TAG, "   - lastReadMessageId: ${historyResult.lastReadMessageId}")
                        
                        // Log detallado de cada mensaje recibido
                        historyResult.messages.forEachIndexed { index, message ->
                            Log.d(TAG, "   ┌─ Mensaje[$index] en ViewModel:")
                            Log.d(TAG, "   │  messageId=${message.messageId}")
                            Log.d(TAG, "   │  typeCode=${message.typeCode}")
                            Log.d(TAG, "   │  systemSubtypeCode=${message.systemSubtypeCode}")
                            Log.d(TAG, "   │  isChangeAppliedMessage=${message.isChangeAppliedMessage()}")
                            Log.d(TAG, "   │  change=${if (message.change != null) "✅ Presente (changeId=${message.change.changeId}, items=${message.change.items.size})" else "❌ null"}")
                            if (message.isChangeAppliedMessage() && message.change == null) {
                                Log.e(TAG, "   │  ⚠️⚠️⚠️ ERROR: Es CHANGE_APPLIED pero change es null!")
                                Log.e(TAG, "   │  info=${message.info?.take(500)}")
                            }
                            Log.d(TAG, "   └─────────────────────────────────────")
                        }
                        
                        historyResult
                    }
                    val quoteDetailDeferred = async {
                        try {
                            Log.d(TAG, "💰 Obteniendo detalles de cotización para quoteId=$quoteId")
                            quotesRepository.getQuoteDetail(quoteId)
                        } catch (e: Exception) {
                            Log.e(TAG, "❌ Error al obtener detalles de cotización: ${e.message}", e)
                            null // Si falla, continuamos sin los detalles
                        }
                    }
                    
                    // Esperar ambos resultados
                    Pair(historyDeferred.await(), quoteDetailDeferred.await())
                }
                
                // ⭐ QUOTE: Almacenar los detalles de la cotización
                _quoteDetail.value = quoteDetail
                if (quoteDetail != null) {
                    Log.d(TAG, "═══════════════════════════════════════")
                    Log.d(TAG, "📊 ESTADO DE LA COTIZACIÓN AL ENTRAR AL CHAT")
                    Log.d(TAG, "═══════════════════════════════════════")
                    Log.d(TAG, "   QuoteId: ${quoteDetail.quoteId}")
                    Log.d(TAG, "   StateCode: ${quoteDetail.stateCode}")
                    Log.d(TAG, "   TotalAmount: ${quoteDetail.totalAmount} ${quoteDetail.currencyCode}")
                    Log.d(TAG, "   RequestId: ${quoteDetail.requestId}")
                    Log.d(TAG, "   Version: ${quoteDetail.version}")
                    Log.d(TAG, "═══════════════════════════════════════")
                    Log.d(TAG, "✅ Detalles de cotización cargados: quoteId=${quoteDetail.quoteId}, totalAmount=${quoteDetail.totalAmount}, stateCode=${quoteDetail.stateCode}, requestId=${quoteDetail.requestId}")
                    
                    // ⭐ REQUEST: Obtener detalles de la solicitud usando el requestId de la cotización
                    try {
                        Log.d(TAG, "📋 Obteniendo detalles de solicitud para requestId=${quoteDetail.requestId}")
                        val requestDetail = planningInboxRepository.getRequestDetail(quoteDetail.requestId)
                        _requestDetail.value = requestDetail
                        Log.d(TAG, "✅ Detalles de solicitud cargados: requestId=${requestDetail.requestId}, requestName=${requestDetail.requestName}, itemsCount=${requestDetail.itemsCount}")
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Error al obtener detalles de solicitud: ${e.message}", e)
                        _requestDetail.value = null // Si falla, continuamos sin los detalles de la solicitud
                    }
                } else {
                    _requestDetail.value = null
                }
                
                Log.d(TAG, "✅ Historial cargado: ${history.messages.size} mensajes, lastReadMessageId=${history.lastReadMessageId}")
                
                _uiState.value = UiState.Success(
                    messages = history.messages,
                    lastReadMessageId = history.lastReadMessageId,
                    currentUserId = currentUserId
                )

                // Marcar como leído si hay mensajes nuevos
                history.lastReadMessageId?.let { lastRead ->
                    val latestMessageId = history.messages.maxOfOrNull { it.messageId }
                    if (latestMessageId != null && latestMessageId > lastRead) {
                        markAsRead(quoteId, latestMessageId)
                    }
                } ?: run {
                    // Si nunca ha leído, marcar el último mensaje como leído
                    history.messages.maxOfOrNull { it.messageId }?.let { latestId ->
                        markAsRead(quoteId, latestId)
                    }
                }

                // ⭐ CHAT: Suscribirse al chat vía WebSocket para recibir mensajes en tiempo real
                chatSubscriptionId = webSocketManager.subscribeToChat(quoteId)
                Log.d(TAG, "💬 Suscrito al chat vía WebSocket: subscriptionId=$chatSubscriptionId")

                // ⭐ CHAT: Observar mensajes en tiempo real (guardar el Job)
                chatObservationJob = viewModelScope.launch {
                    Log.d(TAG, "💬 [WebSocket] Iniciando observación de mensajes para quoteId=$quoteId")
                    webSocketManager.getChatMessageFlow(quoteId).collectLatest { newMessageDto ->
                        Log.d(TAG, "💬 [WebSocket] Mensaje recibido del flujo")
                        newMessageDto?.let { messageDto ->
                            Log.d(TAG, "═══════════════════════════════════════")
                            Log.d(TAG, "💬 [WebSocket] Mensaje recibido en tiempo real")
                            Log.d(TAG, "   messageId=${messageDto.messageId}")
                            Log.d(TAG, "   quoteId=${messageDto.quoteId}")
                            Log.d(TAG, "   typeCode=${messageDto.typeCode}")
                            Log.d(TAG, "   systemSubtypeCode=${messageDto.systemSubtypeCode}")
                            Log.d(TAG, "   isChangeAppliedMessage=${messageDto.systemSubtypeCode == "CHANGE_APPLIED"}")
                            Log.d(TAG, "   isChangeProposedMessage=${messageDto.systemSubtypeCode == "CHANGE_PROPOSED"}")
                            Log.d(TAG, "   isChangeAcceptedMessage=${messageDto.systemSubtypeCode == "CHANGE_ACCEPTED"}")
                            Log.d(TAG, "   isChangeRejectedMessage=${messageDto.systemSubtypeCode == "CHANGE_REJECTED"}")
                            Log.d(TAG, "   info=${messageDto.info?.toString()?.take(200)}")
                            
                            // Convertir DTO a dominio
                            Log.d(TAG, "   Convirtiendo DTO a dominio...")
                            val chatMessage = messageDto.toDomain()
                            Log.d(TAG, "   ✅ Mensaje convertido:")
                            Log.d(TAG, "      messageId=${chatMessage.messageId}")
                            Log.d(TAG, "      systemSubtypeCode=${chatMessage.systemSubtypeCode}")
                            Log.d(TAG, "      isChangeAppliedMessage=${chatMessage.isChangeAppliedMessage()}")
                            Log.d(TAG, "      isChangeProposedMessage=${chatMessage.isChangeProposedMessage()}")
                            Log.d(TAG, "      isChangeAcceptedMessage=${chatMessage.isChangeAcceptedMessage()}")
                            Log.d(TAG, "      isChangeRejectedMessage=${chatMessage.isChangeRejectedMessage()}")
                            Log.d(TAG, "      isAcceptanceRequestMessage=${chatMessage.isAcceptanceRequestMessage()}")
                            Log.d(TAG, "      isAcceptanceConfirmedMessage=${chatMessage.isAcceptanceConfirmedMessage()}")
                            Log.d(TAG, "      isAcceptanceRejectedMessage=${chatMessage.isAcceptanceRejectedMessage()}")
                            Log.d(TAG, "      isQuoteRejectedMessage=${chatMessage.isQuoteRejectedMessage()}")
                            Log.d(TAG, "      change=${if (chatMessage.change != null) "✅ Presente (changeId=${chatMessage.change.changeId}, statusCode=${chatMessage.change.statusCode})" else "❌ null"}")
                            Log.d(TAG, "      acceptanceId=${if (chatMessage.acceptanceId != null) "✅ Presente (acceptanceId=${chatMessage.acceptanceId})" else "❌ null"}")
                            if (chatMessage.isChangeAppliedMessage() && chatMessage.change == null) {
                                Log.e(TAG, "      ⚠️⚠️⚠️ ERROR: Es CHANGE_APPLIED pero change es null!")
                                Log.e(TAG, "      info completo: ${chatMessage.info}")
                            }
                            if (chatMessage.isChangeProposedMessage() && chatMessage.change == null) {
                                Log.e(TAG, "      ⚠️⚠️⚠️ ERROR: Es CHANGE_PROPOSED pero change es null!")
                                Log.e(TAG, "      info completo: ${chatMessage.info}")
                            }
                            if (chatMessage.isAcceptanceRequestMessage() && chatMessage.acceptanceId == null) {
                                Log.e(TAG, "      ⚠️⚠️⚠️ ERROR: Es ACCEPTANCE_REQUEST pero acceptanceId es null!")
                                Log.e(TAG, "      info completo: ${chatMessage.info}")
                            }
                            
                            // Obtener el estado actual
                            val currentState = _uiState.value
                            if (currentState is UiState.Success) {
                                val isDuplicate = if (chatMessage.messageId > 0) {
                                    currentState.messages.any { it.messageId == chatMessage.messageId }
                                } else {
                                    // Si messageId es null/0 (WebSocket), comparar por combinación única:
                                    // quoteId + createdAt + createdBy + (systemSubtypeCode o body)
                                    val uniqueKey = buildString {
                                        append(chatMessage.quoteId)
                                        append("|")
                                        append(chatMessage.createdAt)
                                        append("|")
                                        append(chatMessage.createdBy)
                                        append("|")
                                        if (chatMessage.systemSubtypeCode != null) {
                                            append(chatMessage.systemSubtypeCode)
                                            // Para mensajes de cambio, incluir changeId si está disponible
                                            if ((chatMessage.systemSubtypeCode == "CHANGE_APPLIED" || 
                                                 chatMessage.systemSubtypeCode == "CHANGE_PROPOSED" || 
                                                 chatMessage.systemSubtypeCode == "CHANGE_ACCEPTED" || 
                                                 chatMessage.systemSubtypeCode == "CHANGE_REJECTED") && 
                                                chatMessage.change != null) {
                                                append("|")
                                                append(chatMessage.change.changeId)
                                            }
                                            // Para ACCEPTANCE_REQUEST, incluir acceptanceId si está disponible
                                            if (chatMessage.systemSubtypeCode == "ACCEPTANCE_REQUEST" && chatMessage.acceptanceId != null) {
                                                append("|")
                                                append(chatMessage.acceptanceId)
                                            }
                                        } else {
                                            append(chatMessage.body ?: "")
                                        }
                                    }
                                    
                                    currentState.messages.any { existing ->
                                        val existingKey = buildString {
                                            append(existing.quoteId)
                                            append("|")
                                            append(existing.createdAt)
                                            append("|")
                                            append(existing.createdBy)
                                            append("|")
                                            if (existing.systemSubtypeCode != null) {
                                                append(existing.systemSubtypeCode)
                                                // Para mensajes de cambio, incluir changeId si está disponible
                                                if ((existing.systemSubtypeCode == "CHANGE_APPLIED" || 
                                                     existing.systemSubtypeCode == "CHANGE_PROPOSED" || 
                                                     existing.systemSubtypeCode == "CHANGE_ACCEPTED" || 
                                                     existing.systemSubtypeCode == "CHANGE_REJECTED") && 
                                                    existing.change != null) {
                                                    append("|")
                                                    append(existing.change.changeId)
                                                }
                                                if (existing.systemSubtypeCode == "ACCEPTANCE_REQUEST" && existing.acceptanceId != null) {
                                                    append("|")
                                                    append(existing.acceptanceId)
                                                }
                                            } else {
                                                append(existing.body ?: "")
                                            }
                                        }
                                        existingKey == uniqueKey
                                    }
                                }
                                
                                if (!isDuplicate) {
                                    Log.d(TAG, "   ✅ Agregando nuevo mensaje a la lista: messageId=${chatMessage.messageId}")
                                    
                                    // Agregar el mensaje a la lista (como push_back)
                                    val updatedMessages = currentState.messages + chatMessage
                                    
                                    // Actualizar el estado con la nueva lista de mensajes
                                    _uiState.value = currentState.copy(
                                        messages = updatedMessages
                                    )
                                    
                                    Log.d(TAG, "   ✅ Estado actualizado, total mensajes: ${updatedMessages.size}")
                                    
                                    // Marcar como leído si es un mensaje nuevo del otro usuario
                                    if (chatMessage.createdBy != currentUserId) {
                                        // Solo marcar como leído si messageId es válido
                                        if (chatMessage.messageId > 0) {
                                            markAsRead(quoteId, chatMessage.messageId)
                                        } else {
                                            Log.d(TAG, "   ⚠️ messageId es null/0, no se puede marcar como leído aún")
                                        }
                                    }
                                } else {
                                    Log.d(TAG, "   ⚠️ Mensaje duplicado detectado, ignorando: messageId=${chatMessage.messageId}")
                                }
                            } else {
                                Log.w(TAG, "   ⚠️ Estado actual no es Success, no se puede agregar mensaje")
                            }
                            Log.d(TAG, "═══════════════════════════════════════")
                        } ?: run {
                            Log.w(TAG, "💬 [WebSocket] Mensaje recibido es null")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error al cargar historial: ${e.message}", e)
                _uiState.value = UiState.Error(
                    e.message ?: "Error al cargar el chat"
                )
            }
        }
    }

    /**
     * Actualiza el texto del mensaje
     */
    fun updateMessageText(text: String) {
        _messageText.value = text
    }

    /**
     * ⭐ IMAGEN: Selecciona una imagen (desde cámara o galería)
     */
    fun selectImage(uri: Uri) {
        _selectedImageUri.value = uri
        _imageUploadState.value = ImageUploadState.Idle
    }

    /**
     * ⭐ IMAGEN: Limpia la imagen seleccionada
     */
    fun clearSelectedImage() {
        _selectedImageUri.value = null
        _imageCaption.value = ""
        _imageUploadState.value = ImageUploadState.Idle
    }

    /**
     * ⭐ IMAGEN: Actualiza el caption de la imagen
     */
    fun updateImageCaption(caption: String) {
        _imageCaption.value = caption
    }

    /**
     * ⭐ IMAGEN: Sube una imagen al servidor
     */
    fun uploadImage(imageUri: Uri) {
        viewModelScope.launch {
            try {
                _imageUploadState.value = ImageUploadState.Uploading
                Log.d(TAG, "📤 Subiendo imagen: $imageUri")
                
                val imageUrl = mediaRepository.uploadImage(imageUri)
                
                Log.d(TAG, "✅ Imagen subida exitosamente. URL: $imageUrl")
                _imageUploadState.value = ImageUploadState.Success(imageUrl)
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error al subir imagen: ${e.message}", e)
                val errorMessage = when (e) {
                    is com.wapps1.redcarga.features.requests.domain.RequestsDomainError.NetworkError ->
                        "No se pudo subir la imagen. Verifica tu conexión a internet."
                    else ->
                        "Error al subir la imagen: ${e.message ?: "Intenta nuevamente"}"
                }
                _imageUploadState.value = ImageUploadState.Error(errorMessage)
            }
        }
    }

    /**
     * ⭐ IMAGEN: Resetea el estado de subida de imágenes
     */
    fun resetImageUploadState() {
        _imageUploadState.value = ImageUploadState.Idle
    }

    /**
     * ⭐ IMAGEN: Envía un mensaje con imagen
     */
    fun sendImageMessage(quoteId: Long) {
        val imageUri = _selectedImageUri.value
        if (imageUri == null || _isSendingMessage.value) return

        viewModelScope.launch {
            try {
                _isSendingMessage.value = true
                Log.d(TAG, "📤 Enviando mensaje con imagen para quoteId=$quoteId")

                // Primero subir la imagen
                _imageUploadState.value = ImageUploadState.Uploading
                val imageUrl = mediaRepository.uploadImage(imageUri)
                Log.d(TAG, "✅ Imagen subida: $imageUrl")

                val currentUserId = getCurrentAccountId()
                val dedupKey = UUID.randomUUID()
                val caption = _imageCaption.value.takeIf { it.isNotBlank() }

                val request = SendChatMessageRequest(
                    dedupKey = dedupKey,
                    kind = ChatMessageKind.IMAGE,
                    text = null,
                    url = imageUrl,
                    caption = caption
                )

                // Enviar mensaje y obtener respuesta del backend
                val response = chatRepository.sendMessage(quoteId, request)
                Log.d(TAG, "✅ Mensaje con imagen enviado: messageId=${response.messageId}")

                // Limpiar estados
                _selectedImageUri.value = null
                _imageCaption.value = ""
                _imageUploadState.value = ImageUploadState.Idle

                // Agregar el mensaje a la lista actual sin recargar todo el chat
                val currentState = _uiState.value
                if (currentState is UiState.Success) {
                    // ⭐ Verificar que el mensaje no esté ya en la lista
                    if (!currentState.messages.any { it.messageId == response.messageId }) {
                        // Crear el nuevo mensaje con los datos del backend
                        val newMessage = ChatMessage(
                            messageId = response.messageId,
                            quoteId = quoteId,
                            typeCode = ChatMessageType.USER,
                            contentCode = ChatMessageKind.IMAGE,
                            body = caption, // El caption va en body
                            mediaUrl = imageUrl,
                            clientDedupKey = dedupKey,
                            createdBy = currentUserId,
                            createdAt = response.createdAt,
                            systemSubtypeCode = null,
                            info = null
                        )

                        // Agregar el mensaje a la lista
                        val updatedMessages = currentState.messages + newMessage
                        
                        // Actualizar el estado
                        _uiState.value = currentState.copy(
                            messages = updatedMessages,
                            lastReadMessageId = response.messageId
                        )

                        // Marcar como leído
                        markAsRead(quoteId, response.messageId)
                        
                        // ⭐ CHAT: Notificar a ChatListViewModel
                        chatListUpdateNotifier.notifyMessageSent(quoteId, newMessage)
                        Log.d(TAG, "📢 Notificado a ChatListViewModel: mensaje con imagen enviado")
                    } else {
                        Log.d(TAG, "⚠️ Mensaje ya existe en la lista, ignorando")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error al enviar mensaje con imagen: ${e.message}", e)
                val errorMessage = when (e) {
                    is com.wapps1.redcarga.features.requests.domain.RequestsDomainError.NetworkError ->
                        "No se pudo enviar la imagen. Verifica tu conexión."
                    else ->
                        "Error al enviar la imagen: ${e.message ?: "Intenta nuevamente"}"
                }
                _imageUploadState.value = ImageUploadState.Error(errorMessage)
            } finally {
                _isSendingMessage.value = false
            }
        }
    }

    /**
     * Envía un mensaje de texto
     */
    fun sendTextMessage(quoteId: Long) {
        val text = _messageText.value.trim()
        if (text.isBlank() || _isSendingMessage.value) return

        viewModelScope.launch {
            try {
                _isSendingMessage.value = true
                Log.d(TAG, "📤 Enviando mensaje de texto para quoteId=$quoteId")

                val messageTextToSend = text
                val currentUserId = getCurrentAccountId()
                val dedupKey = UUID.randomUUID()

                val request = SendChatMessageRequest(
                    dedupKey = dedupKey,
                    kind = ChatMessageKind.TEXT,
                    text = messageTextToSend,
                    url = null,
                    caption = null
                )

                // Enviar mensaje y obtener respuesta del backend
                val response = chatRepository.sendMessage(quoteId, request)
                Log.d(TAG, "✅ Mensaje enviado: messageId=${response.messageId}")

                // Limpiar el texto del input
                _messageText.value = ""

                // Agregar el mensaje a la lista actual sin recargar todo el chat
                val currentState = _uiState.value
                if (currentState is UiState.Success) {
                    // ⭐ Verificar que el mensaje no esté ya en la lista (por si llegó por WebSocket primero)
                    if (!currentState.messages.any { it.messageId == response.messageId }) {
                        // Crear el nuevo mensaje con los datos del backend
                        val newMessage = ChatMessage(
                            messageId = response.messageId,
                            quoteId = quoteId,
                            typeCode = ChatMessageType.USER,
                            contentCode = ChatMessageKind.TEXT,
                            body = messageTextToSend,
                            mediaUrl = null,
                            clientDedupKey = dedupKey, // ⭐ Guardar dedupKey para referencia
                            createdBy = currentUserId,
                            createdAt = response.createdAt,
                            systemSubtypeCode = null,
                            info = null
                        )

                        // Agregar el mensaje a la lista (como push_back)
                        val updatedMessages = currentState.messages + newMessage
                        
                        // Actualizar el estado con la nueva lista de mensajes
                        _uiState.value = currentState.copy(
                            messages = updatedMessages,
                            lastReadMessageId = response.messageId // Actualizar lastReadMessageId
                        )

                        // Marcar como leído el nuevo mensaje
                        markAsRead(quoteId, response.messageId)
                        
                        // ⭐ CHAT: Notificar a ChatListViewModel que se envió un mensaje
                        chatListUpdateNotifier.notifyMessageSent(quoteId, newMessage)
                        Log.d(TAG, "📢 Notificado a ChatListViewModel: mensaje enviado")
                    } else {
                        Log.d(TAG, "⚠️ Mensaje ya existe en la lista (llegó por WebSocket primero), ignorando")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error al enviar mensaje: ${e.message}", e)
                // El error se puede manejar en la UI si es necesario
            } finally {
                _isSendingMessage.value = false
            }
        }
    }

    /**
     * Marca mensajes como leídos
     */
    private fun markAsRead(quoteId: Long, lastSeenMessageId: Long) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "👁️ Marcando como leído: quoteId=$quoteId, lastSeenMessageId=$lastSeenMessageId")
                chatRepository.markAsRead(quoteId, lastSeenMessageId)
                
                // ⭐ CHAT: Notificar a ChatListViewModel que se marcó como leído
                // El unreadCount debería ser 0 porque se está viendo el chat
                chatListUpdateNotifier.notifyMarkedAsRead(quoteId, 0)
                Log.d(TAG, "📢 Notificado a ChatListViewModel: marcado como leído")
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ Error al marcar como leído: ${e.message}")
                // No es crítico, solo logueamos el error
            }
        }
    }

    /**
     * Obtiene el accountId del usuario actual
     */
    private fun getCurrentAccountId(): Long {
        return when (val sessionState = authSessionStore.sessionState.value) {
            is SessionState.AppSignedIn -> sessionState.app.accountId
            else -> throw IllegalStateException("Usuario no autenticado")
        }
    }

    /**
     * Refresca el historial del chat
     */
    fun refreshChatHistory(quoteId: Long) {
        loadChatHistory(quoteId)
    }

    /**
     * ⭐ APPLY CHANGE: Aplica cambios a la cotización en estado TRATO
     */
    fun applyChange(
        quoteId: Long, 
        request: ApplyChangeRequest,
        ifMatch: String? = null,
        idempotencyKey: String? = null
    ) {
        viewModelScope.launch {
            try {
                _applyChangeState.value = ApplyChangeState.Loading
                Log.d(TAG, "📝 Aplicando cambios a cotización: quoteId=$quoteId, items=${request.items.size}, ifMatch=$ifMatch")

                val response = dealsRepository.applyChange(quoteId, request, ifMatch, idempotencyKey)
                Log.d(TAG, "✅ Cambios procesados exitosamente: changeId=${response.changeId}")

                _applyChangeState.value = ApplyChangeState.Success(response.changeId)

                // ⚠️ NO recargar el historial completo - el mensaje llegará por WebSocket
                // En TRATO: mensaje CHANGE_APPLIED
                // En ACEPTADA: mensaje CHANGE_PROPOSED
                // El mensaje se agregará automáticamente cuando llegue por WebSocket (~2 segundos)
                // Si recargamos el historial aquí, causará duplicados porque:
                // 1. El mensaje llega por WebSocket (messageId = 0/null)
                // 2. Luego recargamos el historial (messageId válido)
                // 3. Resultado: duplicado
                Log.d(TAG, "💬 Esperando mensaje por WebSocket (CHANGE_APPLIED en TRATO, CHANGE_PROPOSED en ACEPTADA)")
                Log.d(TAG, "   (no recargando historial para evitar duplicados)")
                
                // Opcional: Recargar solo los detalles de la cotización para reflejar los cambios
                // (sin recargar todo el historial del chat)
                try {
                    val updatedQuoteDetail = quotesRepository.getQuoteDetail(quoteId)
                    _quoteDetail.value = updatedQuoteDetail
                    Log.d(TAG, "✅ Detalles de cotización actualizados (sin recargar historial)")
                    
                    // Si hay requestDetail, también actualizarlo si es necesario
                    updatedQuoteDetail?.requestId?.let { requestId ->
                        try {
                            val updatedRequestDetail = planningInboxRepository.getRequestDetail(requestId)
                            _requestDetail.value = updatedRequestDetail
                            Log.d(TAG, "✅ Detalles de solicitud actualizados")
                        } catch (e: Exception) {
                            Log.w(TAG, "⚠️ No se pudo actualizar requestDetail: ${e.message}")
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "⚠️ No se pudo actualizar quoteDetail: ${e.message}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error al aplicar cambios: ${e.message}", e)
                val errorMessage = when (e) {
                    is DealsDomainError.Unauthorized -> "No tienes permisos para realizar esta acción"
                    is DealsDomainError.NotChatParticipant -> "No eres participante de este chat"
                    is DealsDomainError.QuoteNotFound -> "La cotización no fue encontrada"
                    is DealsDomainError.InvalidChangeData -> "Los datos de cambio son inválidos"
                    is DealsDomainError.VersionConflict -> "La cotización fue modificada. Por favor, recarga la página"
                    is DealsDomainError.NetworkError -> "Error de conexión. Verifica tu internet"
                    is DealsDomainError.ServerError -> "Error del servidor. Intenta más tarde"
                    else -> e.message ?: "Error desconocido al aplicar cambios"
                }
                _applyChangeState.value = ApplyChangeState.Error(errorMessage)
            }
        }
    }

    /**
     * ⭐ APPLY CHANGE: Resetea el estado de aplicación de cambios
     */
    fun resetApplyChangeState() {
        _applyChangeState.value = ApplyChangeState.Idle
    }

    /**
     * ⭐ RECARGAR DATOS: Recarga los detalles de la cotización y solicitud
     * Se usa cuando se abren los modales para asegurar datos actualizados
     * @param quoteId ID de la cotización
     * @return true si se cargaron correctamente, false si hubo error
     */
    suspend fun reloadQuoteAndRequestData(quoteId: Long): Boolean {
        return try {
            Log.d(TAG, "🔄 Recargando datos de cotización y solicitud para quoteId=$quoteId")
            
            // Obtener detalles de la cotización
            val updatedQuoteDetail = quotesRepository.getQuoteDetail(quoteId)
            _quoteDetail.value = updatedQuoteDetail
            Log.d(TAG, "✅ Detalles de cotización recargados: quoteId=${updatedQuoteDetail?.quoteId}, stateCode=${updatedQuoteDetail?.stateCode}")
            
            // Si hay quoteDetail, obtener detalles de la solicitud
            updatedQuoteDetail?.requestId?.let { requestId ->
                try {
                    val updatedRequestDetail = planningInboxRepository.getRequestDetail(requestId)
                    _requestDetail.value = updatedRequestDetail
                    Log.d(TAG, "✅ Detalles de solicitud recargados: requestId=${updatedRequestDetail.requestId}, itemsCount=${updatedRequestDetail.itemsCount}")
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error al recargar detalles de solicitud: ${e.message}", e)
                    _requestDetail.value = null
                    return false
                }
            } ?: run {
                _requestDetail.value = null
            }
            
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al recargar datos: ${e.message}", e)
            false
        }
    }

    /**
     * ⭐ ACCEPTANCE: Propone aceptación de una cotización
     * Genera mensaje ACCEPTANCE_REQUEST en el chat
     */
    fun proposeAcceptance(
        quoteId: Long,
        note: String? = null,
        idempotencyKey: String? = null
    ) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "═══════════════════════════════════════")
                Log.d(TAG, "🤝 [PROPOSE ACCEPTANCE] Iniciando propuesta de aceptación")
                Log.d(TAG, "═══════════════════════════════════════")
                Log.d(TAG, "   QuoteId: $quoteId")
                Log.d(TAG, "   Note: ${note?.take(50) ?: "null"}")
                Log.d(TAG, "   IdempotencyKey: ${idempotencyKey ?: "null"}")
                
                _acceptanceState.value = AcceptanceState.Loading
                Log.d(TAG, "   Estado cambiado a: Loading")

                val request = com.wapps1.redcarga.features.deals.domain.models.AcceptanceRequest(
                    idempotencyKey = idempotencyKey,
                    note = note
                )
                Log.d(TAG, "   Request creado: idempotencyKey=${request.idempotencyKey}, note=${request.note?.take(50)}")

                Log.d(TAG, "   Llamando a dealsRepository.proposeAcceptance()...")
                val response = dealsRepository.proposeAcceptance(quoteId, request)
                Log.d(TAG, "   ✅ Respuesta recibida: acceptanceId=${response.acceptanceId}")

                _acceptanceState.value = AcceptanceState.Success(response.acceptanceId)
                Log.d(TAG, "   Estado cambiado a: Success(acceptanceId=${response.acceptanceId})")

                // ⚠️ NO recargar el historial completo - el mensaje ACCEPTANCE_REQUEST llegará por WebSocket
                Log.d(TAG, "   💬 Esperando mensaje ACCEPTANCE_REQUEST por WebSocket (no recargando historial para evitar duplicados)")

                // Recargar solo los detalles de la cotización para reflejar cambios
                try {
                    Log.d(TAG, "   🔄 Recargando detalles de cotización...")
                    val updatedQuoteDetail = quotesRepository.getQuoteDetail(quoteId)
                    _quoteDetail.value = updatedQuoteDetail
                    Log.d(TAG, "   ✅ Detalles de cotización actualizados: stateCode=${updatedQuoteDetail?.stateCode}")
                } catch (e: Exception) {
                    Log.w(TAG, "   ⚠️ No se pudo actualizar quoteDetail: ${e.message}")
                }
                
                Log.d(TAG, "═══════════════════════════════════════")
                Log.d(TAG, "✅ [PROPOSE ACCEPTANCE] Propuesta completada exitosamente")
                Log.d(TAG, "═══════════════════════════════════════")
            } catch (e: Exception) {
                Log.e(TAG, "═══════════════════════════════════════")
                Log.e(TAG, "❌ [PROPOSE ACCEPTANCE] ERROR")
                Log.e(TAG, "═══════════════════════════════════════")
                Log.e(TAG, "   Exception: ${e.javaClass.simpleName}")
                Log.e(TAG, "   Message: ${e.message}")
                Log.e(TAG, "   QuoteId: $quoteId")
                e.printStackTrace()
                
                val errorMessage = when (e) {
                    is DealsDomainError.Unauthorized -> "No tienes permisos para realizar esta acción"
                    is DealsDomainError.NotChatParticipant -> "No eres participante de este chat"
                    is DealsDomainError.QuoteNotFound -> "La cotización no fue encontrada"
                    is DealsDomainError.InvalidChangeData -> "Los datos de la propuesta son inválidos"
                    is DealsDomainError.VersionConflict -> "La cotización fue modificada. Por favor, recarga la página"
                    is DealsDomainError.NetworkError -> "Error de conexión. Verifica tu internet"
                    is DealsDomainError.ServerError -> "Error del servidor. Intenta más tarde"
                    else -> e.message ?: "Error desconocido al proponer aceptación"
                }
                _acceptanceState.value = AcceptanceState.Error(errorMessage)
                Log.e(TAG, "   Estado cambiado a: Error($errorMessage)")
                Log.e(TAG, "═══════════════════════════════════════")
            }
        }
    }

    /**
     * ⭐ ACCEPTANCE: Confirma una propuesta de aceptación
     * Cambia la cotización a estado ACEPTADA
     * Genera mensaje ACCEPTANCE_CONFIRMED en el chat
     */
    fun confirmAcceptance(
        quoteId: Long,
        acceptanceId: Long
    ) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "═══════════════════════════════════════")
                Log.d(TAG, "✅ [CONFIRM ACCEPTANCE] Iniciando confirmación de aceptación")
                Log.d(TAG, "═══════════════════════════════════════")
                Log.d(TAG, "   QuoteId: $quoteId")
                Log.d(TAG, "   AcceptanceId: $acceptanceId")
                
                _acceptanceState.value = AcceptanceState.Loading
                Log.d(TAG, "   Estado cambiado a: Loading")

                Log.d(TAG, "   Llamando a dealsRepository.confirmAcceptance()...")
                val response = dealsRepository.confirmAcceptance(quoteId, acceptanceId)
                Log.d(TAG, "   ✅ Respuesta recibida: ok=${response.ok}")

                _acceptanceState.value = AcceptanceState.Success(acceptanceId)
                Log.d(TAG, "   Estado cambiado a: Success(acceptanceId=$acceptanceId)")

                // ⚠️ NO recargar el historial completo - el mensaje ACCEPTANCE_CONFIRMED llegará por WebSocket
                Log.d(TAG, "   💬 Esperando mensaje ACCEPTANCE_CONFIRMED por WebSocket (no recargando historial para evitar duplicados)")
                Log.d(TAG, "   ⚠️ La cotización debería cambiar a estado ACEPTADA")

                // Recargar solo los detalles de la cotización para reflejar el cambio de estado
                try {
                    Log.d(TAG, "   🔄 Recargando detalles de cotización...")
                    val updatedQuoteDetail = quotesRepository.getQuoteDetail(quoteId)
                    _quoteDetail.value = updatedQuoteDetail
                    Log.d(TAG, "   ✅ Detalles de cotización actualizados: stateCode=${updatedQuoteDetail?.stateCode}")
                    if (updatedQuoteDetail?.stateCode == "ACEPTADA") {
                        Log.d(TAG, "   ✅✅✅ Estado confirmado: La cotización está en ACEPTADA")
                    } else {
                        Log.w(TAG, "   ⚠️⚠️⚠️ ADVERTENCIA: El estado no es ACEPTADA, es: ${updatedQuoteDetail?.stateCode}")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "   ⚠️ No se pudo actualizar quoteDetail: ${e.message}")
                }
                
                Log.d(TAG, "═══════════════════════════════════════")
                Log.d(TAG, "✅ [CONFIRM ACCEPTANCE] Confirmación completada exitosamente")
                Log.d(TAG, "═══════════════════════════════════════")
            } catch (e: Exception) {
                Log.e(TAG, "═══════════════════════════════════════")
                Log.e(TAG, "❌ [CONFIRM ACCEPTANCE] ERROR")
                Log.e(TAG, "═══════════════════════════════════════")
                Log.e(TAG, "   Exception: ${e.javaClass.simpleName}")
                Log.e(TAG, "   Message: ${e.message}")
                Log.e(TAG, "   QuoteId: $quoteId")
                Log.e(TAG, "   AcceptanceId: $acceptanceId")
                e.printStackTrace()
                
                val errorMessage = when (e) {
                    is DealsDomainError.Unauthorized -> "No tienes permisos para realizar esta acción"
                    is DealsDomainError.NotChatParticipant -> "No eres participante de este chat"
                    is DealsDomainError.QuoteNotFound -> "La cotización no fue encontrada"
                    is DealsDomainError.InvalidChangeData -> "No puedes confirmar tu propia propuesta"
                    is DealsDomainError.VersionConflict -> "La cotización fue modificada. Por favor, recarga la página"
                    is DealsDomainError.NetworkError -> "Error de conexión. Verifica tu internet"
                    is DealsDomainError.ServerError -> "Error del servidor. Intenta más tarde"
                    else -> e.message ?: "Error desconocido al confirmar aceptación"
                }
                _acceptanceState.value = AcceptanceState.Error(errorMessage)
                Log.e(TAG, "   Estado cambiado a: Error($errorMessage)")
                Log.e(TAG, "═══════════════════════════════════════")
            }
        }
    }

    /**
     * ⭐ ACCEPTANCE: Rechaza una propuesta de aceptación
     * La cotización NO cambia de estado (sigue en TRATO o EN_ESPERA)
     * Genera mensaje ACCEPTANCE_REJECTED en el chat
     */
    fun rejectAcceptance(
        quoteId: Long,
        acceptanceId: Long
    ) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "═══════════════════════════════════════")
                Log.d(TAG, "❌ [REJECT ACCEPTANCE] Iniciando rechazo de aceptación")
                Log.d(TAG, "═══════════════════════════════════════")
                Log.d(TAG, "   QuoteId: $quoteId")
                Log.d(TAG, "   AcceptanceId: $acceptanceId")
                
                _acceptanceState.value = AcceptanceState.Loading
                Log.d(TAG, "   Estado cambiado a: Loading")

                Log.d(TAG, "   Llamando a dealsRepository.rejectAcceptance()...")
                val response = dealsRepository.rejectAcceptance(quoteId, acceptanceId)
                Log.d(TAG, "   ✅ Respuesta recibida: ok=${response.ok}")

                _acceptanceState.value = AcceptanceState.Success(acceptanceId)
                Log.d(TAG, "   Estado cambiado a: Success(acceptanceId=$acceptanceId)")

                // ⚠️ NO recargar el historial completo - el mensaje ACCEPTANCE_REJECTED llegará por WebSocket
                Log.d(TAG, "   💬 Esperando mensaje ACCEPTANCE_REJECTED por WebSocket (no recargando historial para evitar duplicados)")
                Log.d(TAG, "   ⚠️ La cotización mantiene su estado actual (no cambia)")

                // Recargar solo los detalles de la cotización (aunque el estado no cambia)
                try {
                    Log.d(TAG, "   🔄 Recargando detalles de cotización...")
                    val updatedQuoteDetail = quotesRepository.getQuoteDetail(quoteId)
                    _quoteDetail.value = updatedQuoteDetail
                    Log.d(TAG, "   ✅ Detalles de cotización actualizados: stateCode=${updatedQuoteDetail?.stateCode}")
                } catch (e: Exception) {
                    Log.w(TAG, "   ⚠️ No se pudo actualizar quoteDetail: ${e.message}")
                }
                
                Log.d(TAG, "═══════════════════════════════════════")
                Log.d(TAG, "✅ [REJECT ACCEPTANCE] Rechazo completado exitosamente")
                Log.d(TAG, "═══════════════════════════════════════")
            } catch (e: Exception) {
                Log.e(TAG, "═══════════════════════════════════════")
                Log.e(TAG, "❌ [REJECT ACCEPTANCE] ERROR")
                Log.e(TAG, "═══════════════════════════════════════")
                Log.e(TAG, "   Exception: ${e.javaClass.simpleName}")
                Log.e(TAG, "   Message: ${e.message}")
                Log.e(TAG, "   QuoteId: $quoteId")
                Log.e(TAG, "   AcceptanceId: $acceptanceId")
                e.printStackTrace()
                
                val errorMessage = when (e) {
                    is DealsDomainError.Unauthorized -> "No tienes permisos para realizar esta acción"
                    is DealsDomainError.NotChatParticipant -> "No eres participante de este chat"
                    is DealsDomainError.QuoteNotFound -> "La cotización no fue encontrada"
                    is DealsDomainError.InvalidChangeData -> "No puedes rechazar tu propia propuesta"
                    is DealsDomainError.VersionConflict -> "La cotización fue modificada. Por favor, recarga la página"
                    is DealsDomainError.NetworkError -> "Error de conexión. Verifica tu internet"
                    is DealsDomainError.ServerError -> "Error del servidor. Intenta más tarde"
                    else -> e.message ?: "Error desconocido al rechazar aceptación"
                }
                _acceptanceState.value = AcceptanceState.Error(errorMessage)
                Log.e(TAG, "   Estado cambiado a: Error($errorMessage)")
                Log.e(TAG, "═══════════════════════════════════════")
            }
        }
    }

    /**
     * ⭐ ACCEPTANCE: Resetea el estado de aceptación
     */
    fun resetAcceptanceState() {
        _acceptanceState.value = AcceptanceState.Idle
    }

    // ⭐ CHANGE DECISION: Estados para decidir sobre cambios propuestos
    sealed class ChangeDecisionState {
        object Idle : ChangeDecisionState()
        object Loading : ChangeDecisionState()
        data class Success(val changeId: Long) : ChangeDecisionState()
        data class Error(val message: String) : ChangeDecisionState()
    }

    private val _changeDecisionState = MutableStateFlow<ChangeDecisionState>(ChangeDecisionState.Idle)
    val changeDecisionState: StateFlow<ChangeDecisionState> = _changeDecisionState.asStateFlow()

    /**
     * ⭐ CHANGE DECISION: Decide sobre un cambio propuesto (aceptar o rechazar)
     * Solo disponible en estado ACEPTADA
     */
    fun decisionChange(
        quoteId: Long,
        changeId: Long,
        accept: Boolean,
        ifMatch: String? = null
    ) {
        viewModelScope.launch {
            try {
                _changeDecisionState.value = ChangeDecisionState.Loading
                Log.d(TAG, "═══════════════════════════════════════")
                Log.d(TAG, "⚙️ [DECISION CHANGE] Decidiendo sobre cambio")
                Log.d(TAG, "═══════════════════════════════════════")
                Log.d(TAG, "   QuoteId: $quoteId")
                Log.d(TAG, "   ChangeId: $changeId")
                Log.d(TAG, "   Accept: $accept")
                Log.d(TAG, "   IfMatch: $ifMatch")

                dealsRepository.decisionChange(quoteId, changeId, accept, ifMatch)
                Log.d(TAG, "   ✅ Decisión aplicada exitosamente")

                _changeDecisionState.value = ChangeDecisionState.Success(changeId)

                // ⚠️ NO recargar el historial completo - el mensaje CHANGE_ACCEPTED/CHANGE_REJECTED llegará por WebSocket
                Log.d(TAG, "   💬 Esperando mensaje por WebSocket (no recargando historial para evitar duplicados)")

                // Recargar solo los detalles de la cotización
                try {
                    val updatedQuoteDetail = quotesRepository.getQuoteDetail(quoteId)
                    _quoteDetail.value = updatedQuoteDetail
                    Log.d(TAG, "   ✅ Detalles de cotización actualizados: stateCode=${updatedQuoteDetail?.stateCode}")
                } catch (e: Exception) {
                    Log.w(TAG, "   ⚠️ No se pudo actualizar quoteDetail: ${e.message}")
                }

                Log.d(TAG, "═══════════════════════════════════════")
                Log.d(TAG, "✅ [DECISION CHANGE] Decisión completada exitosamente")
                Log.d(TAG, "═══════════════════════════════════════")
            } catch (e: Exception) {
                Log.e(TAG, "═══════════════════════════════════════")
                Log.e(TAG, "❌ [DECISION CHANGE] ERROR")
                Log.e(TAG, "═══════════════════════════════════════")
                Log.e(TAG, "   Exception: ${e.javaClass.simpleName}")
                Log.e(TAG, "   Message: ${e.message}")
                Log.e(TAG, "   QuoteId: $quoteId")
                Log.e(TAG, "   ChangeId: $changeId")
                e.printStackTrace()

                val errorMessage = when (e) {
                    is DealsDomainError.Unauthorized -> "No tienes permisos para realizar esta acción"
                    is DealsDomainError.NotChatParticipant -> "No eres participante de este chat"
                    is DealsDomainError.QuoteNotFound -> "La cotización no fue encontrada"
                    is DealsDomainError.InvalidChangeData -> "No puedes decidir sobre tu propio cambio"
                    is DealsDomainError.VersionConflict -> "La cotización fue modificada. Por favor, recarga la página"
                    is DealsDomainError.NetworkError -> "Error de conexión. Verifica tu internet"
                    is DealsDomainError.ServerError -> "Error del servidor. Intenta más tarde"
                    else -> e.message ?: "Error desconocido al decidir sobre el cambio"
                }
                _changeDecisionState.value = ChangeDecisionState.Error(errorMessage)
                Log.e(TAG, "   Estado cambiado a: Error($errorMessage)")
                Log.e(TAG, "═══════════════════════════════════════")
            }
        }
    }

    /**
     * ⭐ CHANGE DECISION: Resetea el estado de decisión de cambio
     */
    fun resetChangeDecisionState() {
        _changeDecisionState.value = ChangeDecisionState.Idle
    }

    // ⭐ REJECT QUOTE: Estados para rechazar cotización
    sealed class RejectQuoteState {
        object Idle : RejectQuoteState()
        object Loading : RejectQuoteState()
        data object Success : RejectQuoteState()
        data class Error(val message: String) : RejectQuoteState()
    }

    private val _rejectQuoteState = MutableStateFlow<RejectQuoteState>(RejectQuoteState.Idle)
    val rejectQuoteState: StateFlow<RejectQuoteState> = _rejectQuoteState.asStateFlow()

    /**
     * ⭐ REJECT QUOTE: Rechaza una cotización
     * Disponible en estado ACEPTADA
     */
    fun rejectQuote(quoteId: Long) {
        viewModelScope.launch {
            try {
                _rejectQuoteState.value = RejectQuoteState.Loading
                Log.d(TAG, "═══════════════════════════════════════")
                Log.d(TAG, "❌ [REJECT QUOTE] Rechazando cotización")
                Log.d(TAG, "═══════════════════════════════════════")
                Log.d(TAG, "   QuoteId: $quoteId")

                val result = quotesRepository.rejectQuote(quoteId)

                if (result.isSuccess) {
                    Log.d(TAG, "   ✅ Cotización rechazada exitosamente")
                    _rejectQuoteState.value = RejectQuoteState.Success

                    // ⚠️ NO recargar el historial completo - el mensaje QUOTE_REJECTED llegará por WebSocket
                    Log.d(TAG, "   💬 Esperando mensaje QUOTE_REJECTED por WebSocket (no recargando historial para evitar duplicados)")

                    // Recargar solo los detalles de la cotización
                    try {
                        val updatedQuoteDetail = quotesRepository.getQuoteDetail(quoteId)
                        _quoteDetail.value = updatedQuoteDetail
                        Log.d(TAG, "   ✅ Detalles de cotización actualizados: stateCode=${updatedQuoteDetail?.stateCode}")
                    } catch (e: Exception) {
                        Log.w(TAG, "   ⚠️ No se pudo actualizar quoteDetail: ${e.message}")
                    }

                    Log.d(TAG, "═══════════════════════════════════════")
                    Log.d(TAG, "✅ [REJECT QUOTE] Rechazo completado exitosamente")
                    Log.d(TAG, "═══════════════════════════════════════")
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Error desconocido"
                    Log.e(TAG, "   ❌ Error al rechazar cotización: $error")
                    _rejectQuoteState.value = RejectQuoteState.Error(
                        error ?: "No se pudo rechazar la cotización. Por favor, intenta nuevamente."
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "═══════════════════════════════════════")
                Log.e(TAG, "❌ [REJECT QUOTE] ERROR")
                Log.e(TAG, "═══════════════════════════════════════")
                Log.e(TAG, "   Exception: ${e.javaClass.simpleName}")
                Log.e(TAG, "   Message: ${e.message}")
                Log.e(TAG, "   QuoteId: $quoteId")
                e.printStackTrace()

                _rejectQuoteState.value = RejectQuoteState.Error(
                    e.message ?: "Error desconocido al rechazar la cotización"
                )
                Log.e(TAG, "═══════════════════════════════════════")
            }
        }
    }

    /**
     * ⭐ REJECT QUOTE: Resetea el estado de rechazo de cotización
     */
    fun resetRejectQuoteState() {
        _rejectQuoteState.value = RejectQuoteState.Idle
    }

    /**
     * Limpia recursos cuando el ViewModel es destruido
     */
    override fun onCleared() {
        super.onCleared()
        
        // ⭐ CHAT: Cancelar observación de mensajes
        chatObservationJob?.cancel()
        chatObservationJob = null
        
        // ⭐ CHAT: Desuscribirse del chat cuando se sale de la vista
        currentQuoteId?.let { quoteId ->
            webSocketManager.unsubscribeFromChat(quoteId)
            Log.d(TAG, "💬 Desuscrito del chat: quoteId=$quoteId")
        }
        
        chatSubscriptionId = null
        currentQuoteId = null
    }
}

