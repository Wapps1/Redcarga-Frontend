package com.wapps1.redcarga.features.chat.data.remote.services

import com.wapps1.redcarga.features.chat.data.remote.models.*
import retrofit2.http.*

/**
 * Servicio Retrofit para endpoints de chat
 */
interface ChatService {
    /**
     * Envía un mensaje de chat en una cotización
     * POST /api/deals/quotes/{quoteId}/chat/messages
     */
    @Headers("X-App-Auth: true")
    @POST("/api/deals/quotes/{quoteId}/chat/messages")
    suspend fun sendMessage(
        @Path("quoteId") quoteId: Long,
        @Body request: SendChatMessageRequestDto
    ): SendChatMessageResponseDto

    /**
     * Obtiene el historial completo de mensajes de una cotización
     * GET /api/deals/quotes/{quoteId}/chat
     * Retorna el historial completo con el último mensaje leído por el usuario
     */
    @Headers("X-App-Auth: true")
    @GET("/api/deals/quotes/{quoteId}/chat")
    suspend fun getChatHistory(
        @Path("quoteId") quoteId: Long
    ): ChatHistoryResponseDto

    /**
     * Marca mensajes como leídos
     * PUT /api/deals/quotes/{quoteId}/chat/read
     */
    @Headers("X-App-Auth: true")
    @PUT("/api/deals/quotes/{quoteId}/chat/read")
    suspend fun markAsRead(
        @Path("quoteId") quoteId: Long,
        @Body request: MarkChatReadRequestDto
    ): retrofit2.Response<Unit>

    /**
     * Obtiene la lista de chats del usuario actual
     * GET /api/deals/chat/list
     * Retorna todos los chats asociados al usuario logueado (identificado por el token IAM)
     */
    @Headers("X-App-Auth: true")
    @GET("/api/deals/chat/list")
    suspend fun getChatList(): ChatListResponseDto
}

