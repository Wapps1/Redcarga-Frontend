package com.wapps1.redcarga.features.chat.domain.repositories

import com.wapps1.redcarga.features.chat.domain.models.*

/**
 * Repositorio para operaciones de chat
 */
interface ChatRepository {
    /**
     * Envía un mensaje de chat en una cotización
     * @param quoteId ID de la cotización
     * @param request Datos del mensaje a enviar
     * @return Response con el ID del mensaje creado
     */
    suspend fun sendMessage(
        quoteId: Long,
        request: SendChatMessageRequest
    ): SendChatMessageResponse

    /**
     * Obtiene el historial completo de mensajes de una cotización
     * @param quoteId ID de la cotización
     * @return Respuesta con el último mensaje leído y todos los mensajes ordenados por messageId ascendente
     */
    suspend fun getChatHistory(
        quoteId: Long
    ): ChatHistoryResponse

    /**
     * Marca mensajes como leídos
     * @param quoteId ID de la cotización
     * @param lastSeenMessageId ID del último mensaje visto
     */
    suspend fun markAsRead(
        quoteId: Long,
        lastSeenMessageId: Long
    )

    /**
     * Obtiene la lista de chats del usuario actual
     * @return Lista de resúmenes de chats ordenados por última actividad
     */
    suspend fun getChatList(): List<ChatSummary>
}

