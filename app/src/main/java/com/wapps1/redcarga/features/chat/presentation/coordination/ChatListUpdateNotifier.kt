package com.wapps1.redcarga.features.chat.presentation.coordination

import com.wapps1.redcarga.features.chat.domain.models.ChatMessage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ⭐ CHAT: Notificador de actualizaciones para la lista de chats
 * Permite que ChatViewModel notifique cambios a ChatListViewModel
 */
@Singleton
class ChatListUpdateNotifier @Inject constructor() {

    /**
     * Evento cuando se envía un mensaje
     */
    data class MessageSentEvent(
        val quoteId: Long,
        val message: ChatMessage
    )

    /**
     * Evento cuando se marca como leído
     */
    data class MarkedAsReadEvent(
        val quoteId: Long,
        val unreadCount: Int
    )

    private val _messageSentEvents = MutableSharedFlow<MessageSentEvent>(replay = 0, extraBufferCapacity = 1)
    val messageSentEvents: SharedFlow<MessageSentEvent> = _messageSentEvents.asSharedFlow()

    private val _markedAsReadEvents = MutableSharedFlow<MarkedAsReadEvent>(replay = 0, extraBufferCapacity = 1)
    val markedAsReadEvents: SharedFlow<MarkedAsReadEvent> = _markedAsReadEvents.asSharedFlow()

    /**
     * Notifica que se envió un mensaje
     */
    fun notifyMessageSent(quoteId: Long, message: ChatMessage) {
        _messageSentEvents.tryEmit(MessageSentEvent(quoteId, message))
    }

    /**
     * Notifica que se marcó como leído
     */
    fun notifyMarkedAsRead(quoteId: Long, unreadCount: Int) {
        _markedAsReadEvents.tryEmit(MarkedAsReadEvent(quoteId, unreadCount))
    }
}

