package com.wapps1.redcarga.features.chat.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wapps1.redcarga.core.session.AuthSessionStore
import com.wapps1.redcarga.core.session.SessionState
import com.wapps1.redcarga.core.websocket.RedcargaWebSocketManager
import com.wapps1.redcarga.features.chat.data.mappers.ChatMappers.toDomain
import com.wapps1.redcarga.features.chat.domain.models.ChatMessage
import com.wapps1.redcarga.features.chat.domain.models.ChatSummary
import com.wapps1.redcarga.features.chat.domain.repositories.ChatRepository
import com.wapps1.redcarga.features.chat.presentation.coordination.ChatListUpdateNotifier
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "ChatListViewModel"

/**
 * ViewModel para la lista de chats
 */
@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val webSocketManager: RedcargaWebSocketManager,
    private val authSessionStore: AuthSessionStore,
    private val chatListUpdateNotifier: ChatListUpdateNotifier
) : ViewModel() {

    sealed class UiState {
        object Loading : UiState()
        data class Success(val chats: List<ChatSummary>) : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // ⭐ CHAT: Map de suscripciones activas (quoteId -> subscriptionId)
    private val activeSubscriptions = mutableMapOf<Long, String>()
    
    // ⭐ CHAT: Jobs de observación por chat
    private val observationJobs = mutableMapOf<Long, Job>()
    
    // ⭐ CHAT: AccountId del usuario actual
    private var currentUserId: Long? = null

    init {
        // ⭐ CHAT: Observar eventos de actualización desde ChatViewModel (solo una vez)
        observeUpdateEvents()
        loadChatList()
    }

    /**
     * Carga la lista de chats
     */
    fun loadChatList() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "📋 Cargando lista de chats...")
                _uiState.value = UiState.Loading
                
                // Obtener accountId del usuario actual
                currentUserId = getCurrentAccountId()
                
                val chats = chatRepository.getChatList()
                Log.d(TAG, "✅ Lista de chats cargada: ${chats.size} chats")
                
                _uiState.value = UiState.Success(chats)

                // ⭐ CHAT: Suscribirse a todos los chats vía WebSocket
                subscribeToAllChats(chats.map { it.quoteId })
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error al cargar lista de chats: ${e.message}", e)
                _uiState.value = UiState.Error(
                    e.message ?: "Error al cargar los chats"
                )
            }
        }
    }

    /**
     * ⭐ CHAT: Suscribe a todos los chats y observa mensajes en tiempo real
     */
    private fun subscribeToAllChats(quoteIds: List<Long>) {
        // Cancelar observaciones anteriores
        observationJobs.values.forEach { it.cancel() }
        observationJobs.clear()
        
        // Desuscribirse de chats que ya no están en la lista
        val currentQuoteIds = quoteIds.toSet()
        activeSubscriptions.keys.filter { it !in currentQuoteIds }.forEach { quoteId ->
            webSocketManager.unsubscribeFromChat(quoteId)
            activeSubscriptions.remove(quoteId)
            Log.d(TAG, "💬 Desuscrito del chat que ya no está en la lista: quoteId=$quoteId")
        }

        // Suscribirse y observar cada chat
        quoteIds.forEach { quoteId ->
            // Suscribirse al chat
            val subscriptionId = webSocketManager.subscribeToChat(quoteId)
            if (subscriptionId != null) {
                activeSubscriptions[quoteId] = subscriptionId
                Log.d(TAG, "💬 Suscrito al chat quoteId=$quoteId, subscriptionId=$subscriptionId")
            }

            // Observar mensajes en tiempo real
            val job = viewModelScope.launch {
                webSocketManager.getChatMessageFlow(quoteId).collectLatest { newMessageDto ->
                    newMessageDto?.let { messageDto ->
                        Log.d(TAG, "💬 Mensaje recibido para chat quoteId=$quoteId: messageId=${messageDto.messageId}")
                        updateChatWithNewMessage(quoteId, messageDto.toDomain())
                    }
                }
            }
            observationJobs[quoteId] = job
        }
        
        Log.d(TAG, "✅ Suscrito a ${quoteIds.size} chats vía WebSocket")
    }
    private fun updateChatWithNewMessage(quoteId: Long, newMessage: ChatMessage) {
        val currentState = _uiState.value
        if (currentState is UiState.Success) {
            val currentChats = currentState.chats.toMutableList()
            val chatIndex = currentChats.indexOfFirst { it.quoteId == quoteId }
            
            if (chatIndex >= 0) {
                val existingChat = currentChats[chatIndex]
                
                // ⭐ Determinar si el mensaje es del otro usuario
                val isFromOtherUser = currentUserId != null && newMessage.createdBy != currentUserId
                
                // ⭐ Actualizar el chat con el nuevo mensaje
                val updatedChat = existingChat.copy(
                    lastMessage = newMessage,
                    // ⭐ Incrementar unreadCount solo si el mensaje es del otro usuario
                    unreadCount = if (isFromOtherUser) {
                        existingChat.unreadCount + 1
                    } else {
                        // Si es del usuario actual, mantener el unreadCount actual
                        existingChat.unreadCount
                    }
                )
                
                // Reemplazar el chat en la lista
                currentChats[chatIndex] = updatedChat
                
                // ⭐ Reordenar por fecha de última actividad (más reciente primero)
                // Usar toEpochMilli() para mejor rendimiento
                val sortedChats = currentChats.sortedByDescending { chat ->
                    chat.lastMessage?.createdAt?.toEpochMilli() ?: chat.createdAt.toEpochMilli()
                }
                
                Log.d(TAG, "✅ Chat actualizado: quoteId=$quoteId, nuevo mensaje messageId=${newMessage.messageId}, " +
                        "isFromOtherUser=$isFromOtherUser, unreadCount=${updatedChat.unreadCount} (antes: ${existingChat.unreadCount})")
                
                // Actualizar el estado SIN RECARGAR TODO
                _uiState.value = UiState.Success(sortedChats)
            } else {
                // ⭐ CHAT: Si el chat no está en la lista, recargar la lista completa
                // Esto solo pasa si llega un mensaje de un chat nuevo
                Log.d(TAG, "⚠️ Chat no encontrado en la lista, recargando lista completa: quoteId=$quoteId")
                loadChatList()
            }
        }
    }

    /**
     * ⭐ CHAT: Actualiza el unreadCount de un chat cuando se marca como leído
     * Puede ser llamado desde ChatViewModel o cuando se detecta que se marcó como leído
     */
    fun updateChatUnreadCount(quoteId: Long, newUnreadCount: Int) {
        val currentState = _uiState.value
        if (currentState is UiState.Success) {
            val currentChats = currentState.chats.toMutableList()
            val chatIndex = currentChats.indexOfFirst { it.quoteId == quoteId }
            
            if (chatIndex >= 0) {
                val existingChat = currentChats[chatIndex]
                val updatedChat = existingChat.copy(unreadCount = newUnreadCount)
                currentChats[chatIndex] = updatedChat
                
                // ⭐ Actualizar SIN RECARGAR TODO
                _uiState.value = UiState.Success(currentChats)
                
                Log.d(TAG, "✅ UnreadCount actualizado para quoteId=$quoteId: $newUnreadCount")
            }
        }
    }

    /**
     * ⭐ CHAT: Observa eventos de actualización desde ChatViewModel
     */
    private fun observeUpdateEvents() {
        // Observar eventos de mensajes enviados
        viewModelScope.launch {
            chatListUpdateNotifier.messageSentEvents.collectLatest { event ->
                Log.d(TAG, "📢 Evento recibido: mensaje enviado para quoteId=${event.quoteId}")
                updateChatWithNewMessage(event.quoteId, event.message)
            }
        }
        
        // Observar eventos de marcado como leído
        viewModelScope.launch {
            chatListUpdateNotifier.markedAsReadEvents.collectLatest { event ->
                Log.d(TAG, "📢 Evento recibido: marcado como leído para quoteId=${event.quoteId}, unreadCount=${event.unreadCount}")
                updateChatUnreadCount(event.quoteId, event.unreadCount)
            }
        }
    }

    /**
     * ⭐ CHAT: Notifica que se envió un mensaje (para actualizar la lista inmediatamente)
     * Puede ser llamado desde ChatViewModel cuando se envía un mensaje
     * Esto asegura que la lista se actualice incluso si el WebSocket tarda
     */
    fun notifyMessageSent(quoteId: Long, message: ChatMessage) {
        updateChatWithNewMessage(quoteId, message)
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
     * Refresca la lista de chats
     */
    fun refreshChatList() {
        loadChatList()
    }

    /**
     * Limpia recursos cuando el ViewModel es destruido
     */
    override fun onCleared() {
        super.onCleared()
        
        // ⭐ CHAT: Cancelar todas las observaciones
        observationJobs.values.forEach { it.cancel() }
        val subscriptionsCount = activeSubscriptions.size
        observationJobs.clear()
        
        // ⭐ CHAT: Desuscribirse de todos los chats
        activeSubscriptions.keys.forEach { quoteId ->
            webSocketManager.unsubscribeFromChat(quoteId)
        }
        activeSubscriptions.clear()
        
        Log.d(TAG, "🧹 Recursos limpiados: $subscriptionsCount suscripciones canceladas")
    }
}

