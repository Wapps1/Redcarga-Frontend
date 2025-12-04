package com.wapps1.redcarga.features.chat.data.repositories

import android.util.Log
import com.wapps1.redcarga.core.session.AuthSessionStore
import com.wapps1.redcarga.core.session.UserType
import com.wapps1.redcarga.features.chat.data.mappers.ChatMappers.toDomain
import com.wapps1.redcarga.features.chat.data.mappers.ChatMappers.toDto
import com.wapps1.redcarga.features.chat.data.remote.services.ChatService
import com.wapps1.redcarga.features.chat.domain.ChatDomainError
import com.wapps1.redcarga.features.chat.domain.models.ChatHistoryResponse
import com.wapps1.redcarga.features.chat.domain.models.ChatMessage
import com.wapps1.redcarga.features.chat.domain.models.ChatSummary
import com.wapps1.redcarga.features.chat.domain.models.MarkChatReadRequest
import com.wapps1.redcarga.features.chat.domain.models.SendChatMessageRequest
import com.wapps1.redcarga.features.chat.domain.models.SendChatMessageResponse
import com.wapps1.redcarga.features.chat.domain.repositories.ChatRepository
import com.wapps1.redcarga.features.requests.data.remote.services.QuotesService
import com.wapps1.redcarga.features.requests.data.remote.services.RequestsService
import com.wapps1.redcarga.features.requests.data.mappers.RequestsMappers.toDomain
import com.wapps1.redcarga.features.requests.data.mappers.QuoteMappers.toDomain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "ChatRepository"

/**
 * Implementación del repositorio de chat
 */
@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val chatService: ChatService,
    private val requestsService: RequestsService,
    private val quotesService: QuotesService,
    private val authSessionStore: AuthSessionStore
) : ChatRepository {

    override suspend fun sendMessage(
        quoteId: Long,
        request: SendChatMessageRequest
    ): SendChatMessageResponse = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "📤 Enviando mensaje de chat para quoteId=$quoteId")
            Log.d(TAG, "   kind=${request.kind}, text=${request.text?.take(50)}, url=${request.url?.take(50)}")

            // Validar request
            if (!request.isValid()) {
                Log.e(TAG, "❌ Request inválido")
                throw ChatDomainError.InvalidMessageData
            }

            val dto = request.toDto()
            val response = chatService.sendMessage(quoteId, dto)

            Log.d(TAG, "✅ Mensaje enviado exitosamente: messageId=${response.messageId}")
            response.toDomain()
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al enviar mensaje: ${e.message}", e)
            when {
                e.message?.contains("401") == true -> throw ChatDomainError.Unauthorized
                e.message?.contains("403") == true -> throw ChatDomainError.NotChatParticipant
                e.message?.contains("404") == true -> throw ChatDomainError.QuoteNotFound
                e.message?.contains("400") == true -> throw ChatDomainError.InvalidMessageData
                e.message?.contains("500") == true -> throw ChatDomainError.ServerError
                else -> throw ChatDomainError.NetworkError
            }
        }
    }

    override suspend fun getChatHistory(
        quoteId: Long
    ): ChatHistoryResponse = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "═══════════════════════════════════════")
            Log.d(TAG, "📥 [getChatHistory] Iniciando para quoteId=$quoteId")
            Log.d(TAG, "═══════════════════════════════════════")

            val responseDto = chatService.getChatHistory(quoteId)
            
            Log.d(TAG, "📦 [getChatHistory] DTO recibido del servicio:")
            Log.d(TAG, "   - lastReadMessageId: ${responseDto.lastReadMessageId}")
            Log.d(TAG, "   - messages.size: ${responseDto.messages.size}")
            
            // Log detallado de cada mensaje DTO antes de convertir
            responseDto.messages.forEachIndexed { index, messageDto ->
                Log.d(TAG, "   ┌─ Mensaje DTO[$index]:")
                Log.d(TAG, "   │  messageId=${messageDto.messageId}")
                Log.d(TAG, "   │  quoteId=${messageDto.quoteId}")
                Log.d(TAG, "   │  typeCode=${messageDto.typeCode}")
                Log.d(TAG, "   │  contentCode=${messageDto.contentCode}")
                Log.d(TAG, "   │  systemSubtypeCode=${messageDto.systemSubtypeCode}")
                Log.d(TAG, "   │  body=${messageDto.body?.take(50)}")
                Log.d(TAG, "   │  mediaUrl=${messageDto.mediaUrl?.take(50)}")
                Log.d(TAG, "   │  info=${messageDto.info?.toString()?.take(200)}")
                Log.d(TAG, "   │  createdBy=${messageDto.createdBy}")
                Log.d(TAG, "   │  createdAt=${messageDto.createdAt}")
                if (messageDto.systemSubtypeCode == "CHANGE_APPLIED") {
                    Log.d(TAG, "   │  ⚠️ ES CHANGE_APPLIED - info completo:")
                    Log.d(TAG, "   │  ${messageDto.info}")
                }
                Log.d(TAG, "   └─────────────────────────────────────")
            }
            
            val response = responseDto.toDomain()

            Log.d(TAG, "✅ [getChatHistory] Historial convertido:")
            Log.d(TAG, "   - Total mensajes: ${response.messages.size}")
            Log.d(TAG, "   - lastReadMessageId: ${response.lastReadMessageId}")
            
            // Log detallado de cada mensaje después de convertir
            response.messages.forEachIndexed { index, message ->
                Log.d(TAG, "   ┌─ Mensaje Domain[$index]:")
                Log.d(TAG, "   │  messageId=${message.messageId}")
                Log.d(TAG, "   │  typeCode=${message.typeCode}")
                Log.d(TAG, "   │  contentCode=${message.contentCode}")
                Log.d(TAG, "   │  systemSubtypeCode=${message.systemSubtypeCode}")
                Log.d(TAG, "   │  isChangeAppliedMessage=${message.isChangeAppliedMessage()}")
                Log.d(TAG, "   │  change=${if (message.change != null) "✅ Presente (changeId=${message.change.changeId})" else "❌ null"}")
                if (message.isChangeAppliedMessage() && message.change == null) {
                    Log.e(TAG, "   │  ⚠️⚠️⚠️ ERROR: Es CHANGE_APPLIED pero change es null!")
                    Log.e(TAG, "   │  info=${message.info?.take(500)}")
                }
                Log.d(TAG, "   └─────────────────────────────────────")
            }
            
            Log.d(TAG, "═══════════════════════════════════════")
            response
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al obtener historial: ${e.message}", e)
            when {
                e.message?.contains("401") == true -> throw ChatDomainError.Unauthorized
                e.message?.contains("403") == true -> throw ChatDomainError.NotChatParticipant
                e.message?.contains("404") == true -> throw ChatDomainError.QuoteNotFound
                e.message?.contains("500") == true -> throw ChatDomainError.ServerError
                else -> throw ChatDomainError.NetworkError
            }
        }
    }

    override suspend fun markAsRead(
        quoteId: Long,
        lastSeenMessageId: Long
    ): Unit = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "👁️ Marcando como leído: quoteId=$quoteId, lastSeenMessageId=$lastSeenMessageId")

            val request = MarkChatReadRequest(lastSeenMessageId)
            val dto = request.toDto()
            val response = chatService.markAsRead(quoteId, dto)

            if (response.isSuccessful) {
                Log.d(TAG, "✅ Mensajes marcados como leídos")
            } else {
                Log.e(TAG, "❌ Error al marcar como leído: ${response.code()}")
                throw ChatDomainError.NetworkError
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al marcar como leído: ${e.message}", e)
            when {
                e.message?.contains("401") == true -> throw ChatDomainError.Unauthorized
                e.message?.contains("403") == true -> throw ChatDomainError.NotChatParticipant
                e.message?.contains("404") == true -> throw ChatDomainError.QuoteNotFound
                e.message?.contains("500") == true -> throw ChatDomainError.ServerError
                else -> throw ChatDomainError.NetworkError
            }
        }
    }

    override suspend fun getChatList(): List<ChatSummary> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "📋 Obteniendo lista de chats desde endpoint optimizado...")

            // 1. Obtener lista de chats desde el endpoint optimizado
            val chatListResponse = chatService.getChatList()
            val chatItems = chatListResponse.chats

            Log.d(TAG, "✅ Endpoint retornó ${chatItems.size} chats")

            if (chatItems.isEmpty()) {
                return@withContext emptyList()
            }

            // 2. Para cada chat, obtener último mensaje y detalles de cotización en paralelo
            val chatsWithDetails = coroutineScope {
                chatItems.map { chatItem ->
                    async {
                        try {
                            val quoteId = chatItem.quoteId.toLong()

                            // Obtener último mensaje y detalles de cotización en paralelo
                            val (lastMessage, quoteDetail) = coroutineScope {
                                val lastMessageDeferred = async {
                                    try {
                                        val historyResponse = chatService.getChatHistory(quoteId)
                                        // Obtener el último mensaje (el más reciente)
                                        historyResponse.messages.lastOrNull()?.toDomain()
                                    } catch (e: Exception) {
                                        Log.w(TAG, "⚠️ Error al obtener último mensaje para quoteId=$quoteId: ${e.message}")
                                        null
                                    }
                                }

                                val quoteDetailDeferred = async {
                                    try {
                                        quotesService.getQuoteDetail(quoteId)
                                    } catch (e: Exception) {
                                        Log.w(TAG, "⚠️ Error al obtener detalle de quote $quoteId: ${e.message}")
                                        null
                                    }
                                }

                                Pair(lastMessageDeferred.await(), quoteDetailDeferred.await())
                            }

                            ChatSummary(
                                quoteId = quoteId,
                                requestId = quoteDetail?.requestId ?: 0L,
                                companyId = chatItem.otherCompanyId?.toLong() ?: 0L,
                                totalAmount = quoteDetail?.totalAmount ?: 0.0,
                                currencyCode = quoteDetail?.currencyCode ?: "PEN",
                                createdAt = quoteDetail?.createdAt?.toInstant() ?: java.time.Instant.now(),
                                lastMessage = lastMessage,
                                unreadCount = chatItem.unreadCount // ✅ Ahora tenemos el conteo real desde el backend
                            )
                        } catch (e: Exception) {
                            Log.w(TAG, "⚠️ Error al obtener datos para quoteId=${chatItem.quoteId}: ${e.message}")
                            // Retornar chat básico sin detalles
                            ChatSummary(
                                quoteId = chatItem.quoteId.toLong(),
                                requestId = 0L,
                                companyId = chatItem.otherCompanyId?.toLong() ?: 0L,
                                totalAmount = 0.0,
                                currencyCode = "PEN",
                                createdAt = java.time.Instant.now(),
                                lastMessage = null,
                                unreadCount = chatItem.unreadCount
                            )
                        }
                    }
                }.awaitAll()
            }

            // 3. Ordenar por última actividad (más reciente primero)
            val sorted = chatsWithDetails.sortedByDescending { it.getLastActivityDate() }

            Log.d(TAG, "✅ Lista de chats obtenida: ${sorted.size} chats")
            sorted
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al obtener lista de chats: ${e.message}", e)
            throw ChatDomainError.NetworkError
        }
    }

    /**
     * Helper para convertir String ISO 8601 a Instant
     */
    private fun String.toInstant(): java.time.Instant {
        return try {
            java.time.Instant.parse(this)
        } catch (e: Exception) {
            java.time.Instant.now()
        }
    }
}

