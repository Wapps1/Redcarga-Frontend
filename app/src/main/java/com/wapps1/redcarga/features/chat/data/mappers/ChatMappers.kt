package com.wapps1.redcarga.features.chat.data.mappers

import android.util.Log
import com.wapps1.redcarga.features.chat.data.remote.models.*
import com.wapps1.redcarga.features.chat.domain.models.*
import com.wapps1.redcarga.features.deals.data.mappers.DealsMappers.toDomain
import com.wapps1.redcarga.features.deals.data.remote.models.ChangeDto
import com.wapps1.redcarga.features.deals.data.remote.models.ChangeItemDto
import com.wapps1.redcarga.features.deals.domain.models.Change
import org.json.JSONObject
import java.time.Instant
import java.util.UUID

private const val TAG = "ChatMappers"

/**
 * Mappers para convertir entre DTOs y modelos de dominio
 */
object ChatMappers {

    // Helper functions para conversiones seguras
    private fun String.toInstant(): Instant {
        return try {
            Instant.parse(this)
        } catch (e: Exception) {
            Instant.now()
        }
    }

    private fun String.toChatMessageKind(): ChatMessageKind {
        return try {
            ChatMessageKind.valueOf(this.uppercase())
        } catch (_: IllegalArgumentException) {
            ChatMessageKind.TEXT
        }
    }

    private fun String.toChatMessageType(): ChatMessageType {
        return try {
            ChatMessageType.valueOf(this.uppercase())
        } catch (_: IllegalArgumentException) {
            ChatMessageType.USER
        }
    }

    private fun ChatMessageKind.toStringValue(): String = this.name

    private fun ChatMessageType.toStringValue(): String = this.name

    private fun String?.toUUIDOrNull(): UUID? {
        return if (this.isNullOrBlank()) null else {
            try {
                UUID.fromString(this)
            } catch (e: Exception) {
                null
            }
        }
    }

    /**
     * Convierte info (Any?) a String JSON para parsing
     */
    private fun infoToString(info: Any?): String? {
        return when (info) {
            null -> null
            is String -> info
            is Map<*, *> -> {
                // Si es un Map (deserializado por Moshi), convertirlo a JSON string
                try {
                    val jsonObj = JSONObject()
                    @Suppress("UNCHECKED_CAST")
                    (info as Map<String, Any?>).forEach { (key, value) ->
                        when (value) {
                            null -> jsonObj.put(key, JSONObject.NULL)
                            is Boolean -> jsonObj.put(key, value)
                            is Int -> jsonObj.put(key, value)
                            is Long -> jsonObj.put(key, value)
                            is Double -> jsonObj.put(key, value)
                            is String -> jsonObj.put(key, value)
                            is Map<*, *> -> {
                                @Suppress("UNCHECKED_CAST")
                                jsonObj.put(key, mapToJSONObject(value as Map<String, Any?>))
                            }
                            is List<*> -> {
                                val jsonArray = org.json.JSONArray()
                                value.forEach { item ->
                                    when (item) {
                                        is Map<*, *> -> {
                                            @Suppress("UNCHECKED_CAST")
                                            jsonArray.put(mapToJSONObject(item as Map<String, Any?>))
                                        }
                                        else -> jsonArray.put(item)
                                    }
                                }
                                jsonObj.put(key, jsonArray)
                            }
                            else -> jsonObj.put(key, value.toString())
                        }
                    }
                    jsonObj.toString()
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error al convertir Map a JSON string: ${e.message}", e)
                    null
                }
            }
            else -> {
                // Para otros tipos, intentar toString()
                try {
                    info.toString()
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error al convertir info a String: ${e.message}", e)
                    null
                }
            }
        }
    }
    
    /**
     * Helper para convertir Map a JSONObject recursivamente
     */
    private fun mapToJSONObject(map: Map<String, Any?>): JSONObject {
        val jsonObj = JSONObject()
        map.forEach { (key, value) ->
            when (value) {
                null -> jsonObj.put(key, JSONObject.NULL)
                is Boolean -> jsonObj.put(key, value)
                is Int -> jsonObj.put(key, value)
                is Long -> jsonObj.put(key, value)
                is Double -> jsonObj.put(key, value)
                is String -> jsonObj.put(key, value)
                is Map<*, *> -> {
                    @Suppress("UNCHECKED_CAST")
                    jsonObj.put(key, mapToJSONObject(value as Map<String, Any?>))
                }
                is List<*> -> {
                    val jsonArray = org.json.JSONArray()
                    value.forEach { item ->
                        when (item) {
                            is Map<*, *> -> {
                                @Suppress("UNCHECKED_CAST")
                                jsonArray.put(mapToJSONObject(item as Map<String, Any?>))
                            }
                            else -> jsonArray.put(item)
                        }
                    }
                    jsonObj.put(key, jsonArray)
                }
                else -> jsonObj.put(key, value.toString())
            }
        }
        return jsonObj
    }

    /**
     * Parsea el campo info para extraer el acceptanceId cuando systemSubtypeCode == "ACCEPTANCE_REQUEST"
     * Estructura esperada: { "acceptanceId": 123, "note": "..." } (opcional)
     */
    private fun parseAcceptanceIdFromInfo(info: Any?): Long? {
        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "🔍 [parseAcceptanceIdFromInfo] Iniciando parsing")
        Log.d(TAG, "   info type: ${info?.javaClass?.simpleName}")
        Log.d(TAG, "   info == null: ${info == null}")
        
        // Convertir info a String JSON
        val infoString = infoToString(info)
        
        if (infoString.isNullOrBlank()) {
            Log.w(TAG, "⚠️ [parseAcceptanceIdFromInfo] Info es null o blank después de convertir, retornando null")
            return null
        }
        
        Log.d(TAG, "   infoString length: ${infoString.length}")
        Log.d(TAG, "   infoString completo (primeros 500 chars): ${infoString.take(500)}")
        
        return try {
            Log.d(TAG, "   Intentando crear JSONObject...")
            val infoJson = JSONObject(infoString)
            Log.d(TAG, "   ✅ JSONObject creado exitosamente")
            
            // ⚠️ DETECTAR ESTRUCTURA: Puede estar en la raíz o dentro de "acceptance"
            val acceptanceId: Long? = if (infoJson.has("acceptanceId") && !infoJson.isNull("acceptanceId")) {
                // Estructura directa: { "acceptanceId": 4 }
                val accId = infoJson.getInt("acceptanceId")
                Log.d(TAG, "   ✅ acceptanceId encontrado en raíz: $accId")
                accId.toLong()
            } else if (infoJson.has("acceptance") && !infoJson.isNull("acceptance")) {
                // Estructura anidada: { "acceptance": { "acceptanceId": 4, ... } }
                Log.d(TAG, "   ✅ Estructura anidada detectada (con wrapper 'acceptance')")
                val acceptanceJson = infoJson.getJSONObject("acceptance")
                Log.d(TAG, "   Keys en acceptance: ${acceptanceJson.keys().asSequence().toList()}")
                if (acceptanceJson.has("acceptanceId") && !acceptanceJson.isNull("acceptanceId")) {
                    val accId = acceptanceJson.getInt("acceptanceId")
                    Log.d(TAG, "   ✅ acceptanceId encontrado en acceptance.acceptanceId: $accId")
                    accId.toLong()
                } else {
                    Log.w(TAG, "   ⚠️ acceptanceId no encontrado dentro de 'acceptance'")
                    Log.w(TAG, "   Keys en acceptance: ${acceptanceJson.keys().asSequence().toList()}")
                    null
                }
            } else {
                Log.w(TAG, "   ⚠️ acceptanceId no encontrado ni en raíz ni en 'acceptance'")
                Log.w(TAG, "   Keys disponibles: ${infoJson.keys().asSequence().toList()}")
                null
            }
            
            Log.d(TAG, "═══════════════════════════════════════")
            acceptanceId
            
        } catch (e: Exception) {
            Log.e(TAG, "═══════════════════════════════════════")
            Log.e(TAG, "❌❌❌ [parseAcceptanceIdFromInfo] ERROR AL PARSEAR")
            Log.e(TAG, "═══════════════════════════════════════")
            Log.e(TAG, "   Exception: ${e.javaClass.simpleName}")
            Log.e(TAG, "   Message: ${e.message}")
            Log.e(TAG, "   Info original type: ${info?.javaClass?.simpleName}")
            Log.e(TAG, "   Info original: $info")
            Log.e(TAG, "   InfoString (primeros 500 chars): ${infoString?.take(500)}")
            Log.e(TAG, "═══════════════════════════════════════")
            null
        }
    }

    /**
     * Parsea el campo info para extraer el objeto change cuando systemSubtypeCode == "CHANGE_APPLIED"
     * Maneja dos estructuras:
     * 1. REST: { "change": { "changeId": ..., "kindCode": ..., "statusCode": ..., "items": [...] } }
     * 2. WebSocket: { "changeId": ..., "kind": ..., "status": ..., "items": [...] } (sin wrapper "change")
     */
    private fun parseChangeFromInfo(info: Any?): Change? {
        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "🔍 [parseChangeFromInfo] Iniciando parsing")
        Log.d(TAG, "   info type: ${info?.javaClass?.simpleName}")
        Log.d(TAG, "   info == null: ${info == null}")
        
        // Convertir info a String JSON
        val infoString = infoToString(info)
        
        if (infoString.isNullOrBlank()) {
            Log.w(TAG, "⚠️ [parseChangeFromInfo] Info es null o blank después de convertir, retornando null")
            return null
        }
        
        Log.d(TAG, "   infoString length: ${infoString.length}")
        Log.d(TAG, "   infoString completo (primeros 1000 chars): ${infoString.take(1000)}")
        
        return try {
            Log.d(TAG, "   Intentando crear JSONObject...")
            val infoJson = JSONObject(infoString)
            Log.d(TAG, "   ✅ JSONObject creado exitosamente")
            
            // ⚠️ DETECTAR ESTRUCTURA: REST tiene wrapper "change", WebSocket no
            val changeJson: JSONObject = if (infoJson.has("change") && !infoJson.isNull("change")) {
                // Estructura REST: { "change": { ... } }
                Log.d(TAG, "   ✅ Estructura REST detectada (con wrapper 'change')")
                infoJson.getJSONObject("change")
            } else if (infoJson.has("changeId")) {
                // Estructura WebSocket: { "changeId": ..., "kind": ..., "status": ... } (sin wrapper)
                Log.d(TAG, "   ✅ Estructura WebSocket detectada (sin wrapper 'change')")
                infoJson
            } else {
                Log.e(TAG, "❌ [parseChangeFromInfo] No se encontró 'change' ni 'changeId'")
                Log.e(TAG, "   Keys disponibles: ${infoJson.keys().asSequence().toList()}")
                return null
            }
            
            Log.d(TAG, "   ✅ changeJson obtenido")
            
            // Parsear campos básicos
            Log.d(TAG, "   Parseando campos básicos de change...")
            val changeId = changeJson.getInt("changeId")
            Log.d(TAG, "   changeId: $changeId")
            
            val quoteId = if (changeJson.has("quoteId") && !changeJson.isNull("quoteId")) {
                val qId = changeJson.optInt("quoteId")
                Log.d(TAG, "   quoteId: $qId")
                qId
            } else {
                Log.w(TAG, "   ⚠️ quoteId no encontrado o es null")
                null
            }
            
            // ⚠️ BUSCAR kind/status (WebSocket) O kindCode/statusCode (REST)
            val kindCode = when {
                changeJson.has("kind") && !changeJson.isNull("kind") -> {
                    val kind = changeJson.optString("kind", "LIBRE")
                    Log.d(TAG, "   kind (WebSocket): $kind")
                    kind
                }
                changeJson.has("kindCode") && !changeJson.isNull("kindCode") -> {
                    val kind = changeJson.optString("kindCode", "LIBRE")
                    Log.d(TAG, "   kindCode (REST): $kind")
                    kind
                }
                else -> {
                    Log.w(TAG, "   ⚠️ No se encontró 'kind' ni 'kindCode', usando default: LIBRE")
                    "LIBRE"
                }
            }
            
            val statusCode = when {
                changeJson.has("status") && !changeJson.isNull("status") -> {
                    val status = changeJson.optString("status", "APLICADO")
                    Log.d(TAG, "   status (WebSocket): $status")
                    status
                }
                changeJson.has("statusCode") && !changeJson.isNull("statusCode") -> {
                    val status = changeJson.optString("statusCode", "APLICADO")
                    Log.d(TAG, "   statusCode (REST): $status")
                    status
                }
                else -> {
                    Log.w(TAG, "   ⚠️ No se encontró 'status' ni 'statusCode', usando default: APLICADO")
                    "APLICADO"
                }
            }
            val createdBy = changeJson.getInt("createdBy")
            val createdAt = changeJson.optString("createdAt", "")
            
            Log.d(TAG, "   kindCode: $kindCode")
            Log.d(TAG, "   statusCode: $statusCode")
            Log.d(TAG, "   createdBy: $createdBy")
            Log.d(TAG, "   createdAt: $createdAt")
            
            // Parsear items
            Log.d(TAG, "   Parseando items...")
            val itemsJson = changeJson.optJSONArray("items")
            Log.d(TAG, "   itemsJson != null: ${itemsJson != null}")
            Log.d(TAG, "   itemsJson.length: ${itemsJson?.length() ?: 0}")
            
            val items = if (itemsJson != null) {
                (0 until itemsJson.length()).map { index ->
                    Log.d(TAG, "   ┌─ Parseando item[$index]...")
                    val itemJson = itemsJson.getJSONObject(index)
                    Log.d(TAG, "   │  itemJson keys: ${itemJson.keys().asSequence().toList()}")
                    
                    val changeItemId = if (itemJson.has("changeItemId") && !itemJson.isNull("changeItemId")) {
                        val cId = itemJson.optInt("changeItemId")
                        Log.d(TAG, "   │  changeItemId: $cId")
                        cId
                    } else {
                        Log.d(TAG, "   │  changeItemId: null (no encontrado)")
                        null
                    }
                    
                    val fieldCode = itemJson.optString("fieldCode", "")
                    val targetQuoteItemId = if (itemJson.has("targetQuoteItemId") && !itemJson.isNull("targetQuoteItemId")) {
                        itemJson.optInt("targetQuoteItemId")
                    } else null
                    val targetRequestItemId = if (itemJson.has("targetRequestItemId") && !itemJson.isNull("targetRequestItemId")) {
                        itemJson.optInt("targetRequestItemId")
                    } else null
                    val oldValue = itemJson.optString("oldValue").takeIf { it.isNotEmpty() && it != "null" }
                    val newValue = itemJson.optString("newValue").takeIf { it.isNotEmpty() && it != "null" }
                    
                    Log.d(TAG, "   │  fieldCode: $fieldCode")
                    Log.d(TAG, "   │  targetQuoteItemId: $targetQuoteItemId")
                    Log.d(TAG, "   │  oldValue: $oldValue")
                    Log.d(TAG, "   │  newValue: $newValue")
                    
                    val itemDto = ChangeItemDto(
                        changeItemId = changeItemId,
                        fieldCode = fieldCode,
                        targetQuoteItemId = targetQuoteItemId,
                        targetRequestItemId = targetRequestItemId,
                        oldValue = oldValue,
                        newValue = newValue
                    )
                    Log.d(TAG, "   └─ Item[$index] parseado")
                    itemDto
                }
            } else {
                Log.w(TAG, "   ⚠️ itemsJson es null, retornando lista vacía")
                emptyList()
            }
            
            Log.d(TAG, "   Total items parseados: ${items.size}")
            
            // Construir ChangeDto
            Log.d(TAG, "   Construyendo ChangeDto...")
            val changeDto = ChangeDto(
                changeId = changeId,
                quoteId = quoteId,
                kindCode = kindCode,
                statusCode = statusCode,
                createdBy = createdBy,
                createdAt = createdAt,
                items = items
            )
            Log.d(TAG, "   ✅ ChangeDto construido")
            
            // Convertir a dominio
            Log.d(TAG, "   Convirtiendo ChangeDto a dominio...")
            val change = changeDto.toDomain()
            Log.d(TAG, "   ✅ Cambio convertido a dominio:")
            Log.d(TAG, "      changeId=${change.changeId}")
            Log.d(TAG, "      quoteId=${change.quoteId}")
            Log.d(TAG, "      kindCode=${change.kindCode}")
            Log.d(TAG, "      statusCode=${change.statusCode}")
            Log.d(TAG, "      items.size=${change.items.size}")
            Log.d(TAG, "═══════════════════════════════════════")
            change
            
        } catch (e: Exception) {
            Log.e(TAG, "═══════════════════════════════════════")
            Log.e(TAG, "❌❌❌ [parseChangeFromInfo] ERROR AL PARSEAR")
            Log.e(TAG, "═══════════════════════════════════════")
            Log.e(TAG, "   Exception: ${e.javaClass.simpleName}")
            Log.e(TAG, "   Message: ${e.message}")
            Log.e(TAG, "   StackTrace:")
            e.printStackTrace()
            Log.e(TAG, "   Info original type: ${info?.javaClass?.simpleName}")
            Log.e(TAG, "   Info original: $info")
            Log.e(TAG, "   InfoString (primeros 1000 chars): ${infoString?.take(1000)}")
            Log.e(TAG, "   InfoString (completo): $infoString")
            Log.e(TAG, "═══════════════════════════════════════")
            null
        }
    }

    // DTO → Domain
    fun SendChatMessageRequestDto.toDomain(): SendChatMessageRequest = SendChatMessageRequest(
        dedupKey = dedupKey.toUUIDOrNull(),
        kind = kind.toChatMessageKind(),
        text = text,
        url = url,
        caption = caption
    )

    fun SendChatMessageResponseDto.toDomain(): SendChatMessageResponse = SendChatMessageResponse(
        ok = ok,
        messageId = messageId.toLong(),
        createdAt = createdAt.toInstant()
    )

    fun ChatMessageDto.toDomain(): ChatMessage {
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG, "🔄 [ChatMessageDto.toDomain] Convirtiendo mensaje")
        Log.d(TAG, "   messageId=${messageId}")
        Log.d(TAG, "   quoteId=${quoteId}")
        Log.d(TAG, "   typeCode=$typeCode")
        Log.d(TAG, "   contentCode=$contentCode")
        Log.d(TAG, "   systemSubtypeCode=$systemSubtypeCode")
        Log.d(TAG, "   isChangeAppliedMessage=${systemSubtypeCode == "CHANGE_APPLIED"}")
        Log.d(TAG, "   info type: ${info?.javaClass?.simpleName}")
        Log.d(TAG, "   info == null: ${info == null}")
        
        // Convertir info a String para almacenarlo en el dominio
        val infoString = infoToString(info)
        Log.d(TAG, "   infoString.isNullOrBlank(): ${infoString.isNullOrBlank()}")
        
        // Parsear change si es un mensaje relacionado con cambios
        // CHANGE_APPLIED, CHANGE_PROPOSED, CHANGE_ACCEPTED, CHANGE_REJECTED
        val parsedChange = if (
            (systemSubtypeCode == "CHANGE_APPLIED" || 
             systemSubtypeCode == "CHANGE_PROPOSED" || 
             systemSubtypeCode == "CHANGE_ACCEPTED" || 
             systemSubtypeCode == "CHANGE_REJECTED") && 
            !infoString.isNullOrBlank()
        ) {
            Log.d(TAG, "   ⚠️ Es $systemSubtypeCode, parseando change...")
            val change = parseChangeFromInfo(info)
            if (change != null) {
                Log.d(TAG, "   ✅ Change parseado exitosamente: changeId=${change.changeId}")
            } else {
                Log.e(TAG, "   ❌❌❌ ERROR: Change es null después de parsear!")
                Log.e(TAG, "   info original: $info")
                Log.e(TAG, "   infoString: $infoString")
            }
            change
        } else {
            if (systemSubtypeCode == "CHANGE_APPLIED" || 
                systemSubtypeCode == "CHANGE_PROPOSED" || 
                systemSubtypeCode == "CHANGE_ACCEPTED" || 
                systemSubtypeCode == "CHANGE_REJECTED") {
                Log.e(TAG, "   ⚠️ Es $systemSubtypeCode pero info es null o blank!")
                Log.e(TAG, "   info type: ${info?.javaClass?.simpleName}")
                Log.e(TAG, "   info: $info")
            }
            null
        }
        
        // Parsear acceptanceId si es un mensaje ACCEPTANCE_REQUEST
        val parsedAcceptanceId = if (systemSubtypeCode == "ACCEPTANCE_REQUEST" && !infoString.isNullOrBlank()) {
            Log.d(TAG, "   ⚠️ Es ACCEPTANCE_REQUEST, parseando acceptanceId...")
            val accId = parseAcceptanceIdFromInfo(info)
            if (accId != null) {
                Log.d(TAG, "   ✅ AcceptanceId parseado exitosamente: acceptanceId=$accId")
            } else {
                Log.e(TAG, "   ❌❌❌ ERROR: AcceptanceId es null después de parsear!")
                Log.e(TAG, "   info original: $info")
                Log.e(TAG, "   infoString: $infoString")
            }
            accId
        } else {
            if (systemSubtypeCode == "ACCEPTANCE_REQUEST") {
                Log.e(TAG, "   ⚠️ Es ACCEPTANCE_REQUEST pero info es null o blank!")
                Log.e(TAG, "   info type: ${info?.javaClass?.simpleName}")
                Log.e(TAG, "   info: $info")
            }
            null
        }
        
        val chatMessage = ChatMessage(
            messageId = messageId.toLong(),
            quoteId = quoteId.toLong(),
            typeCode = typeCode.toChatMessageType(),
            contentCode = contentCode.toChatMessageKind(),
            body = body,
            mediaUrl = mediaUrl,
            clientDedupKey = clientDedupKey.toUUIDOrNull(),
            createdBy = createdBy.toLong(),
            createdAt = createdAt.toInstant(),
            systemSubtypeCode = systemSubtypeCode,
            info = infoString,  // ⚠️ Almacenar como String en el dominio
            change = parsedChange,
            acceptanceId = parsedAcceptanceId
        )
        
        Log.d(TAG, "   ✅ Mensaje convertido:")
        Log.d(TAG, "      messageId=${chatMessage.messageId}")
        Log.d(TAG, "      systemSubtypeCode=${chatMessage.systemSubtypeCode}")
        Log.d(TAG, "      isChangeAppliedMessage=${chatMessage.isChangeAppliedMessage()}")
        Log.d(TAG, "      isChangeProposedMessage=${chatMessage.isChangeProposedMessage()}")
        Log.d(TAG, "      isChangeAcceptedMessage=${chatMessage.isChangeAcceptedMessage()}")
        Log.d(TAG, "      isChangeRejectedMessage=${chatMessage.isChangeRejectedMessage()}")
        Log.d(TAG, "      change=${if (chatMessage.change != null) "✅ Presente (changeId=${chatMessage.change.changeId}, statusCode=${chatMessage.change.statusCode})" else "❌ null"}")
        Log.d(TAG, "      isAcceptanceRequestMessage=${chatMessage.isAcceptanceRequestMessage()}")
        Log.d(TAG, "      acceptanceId=${if (chatMessage.acceptanceId != null) "✅ Presente (acceptanceId=${chatMessage.acceptanceId})" else "❌ null"}")
        if (chatMessage.isChangeAppliedMessage() && chatMessage.change == null) {
            Log.e(TAG, "   ⚠️⚠️⚠️ PROBLEMA: Es CHANGE_APPLIED pero change es null!")
        }
        if (chatMessage.isChangeProposedMessage() && chatMessage.change == null) {
            Log.e(TAG, "   ⚠️⚠️⚠️ PROBLEMA: Es CHANGE_PROPOSED pero change es null!")
        }
        if (chatMessage.isAcceptanceRequestMessage() && chatMessage.acceptanceId == null) {
            Log.e(TAG, "   ⚠️⚠️⚠️ PROBLEMA: Es ACCEPTANCE_REQUEST pero acceptanceId es null!")
        }
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        
        return chatMessage
    }

    fun MarkChatReadRequestDto.toDomain(): MarkChatReadRequest = MarkChatReadRequest(
        lastSeenMessageId = lastSeenMessageId.toLong()
    )

    fun ChatHistoryResponseDto.toDomain(): ChatHistoryResponse {
        Log.d(TAG, "🔄 [ChatHistoryResponseDto.toDomain] Convirtiendo historial")
        Log.d(TAG, "   lastReadMessageId: $lastReadMessageId")
        Log.d(TAG, "   messages.size: ${messages.size}")
        
        val domainMessages = messages.mapIndexed { index, dto ->
            Log.d(TAG, "   Convirtiendo mensaje[$index] de ${messages.size}...")
            dto.toDomain()
        }
        
        Log.d(TAG, "   ✅ Historial convertido: ${domainMessages.size} mensajes")
        
        return ChatHistoryResponse(
            lastReadMessageId = lastReadMessageId?.toLong(),
            messages = domainMessages
        )
    }

    // Domain → DTO
    fun SendChatMessageRequest.toDto(): SendChatMessageRequestDto = SendChatMessageRequestDto(
        dedupKey = dedupKey?.toString(),
        kind = kind.toStringValue(),
        text = text,
        url = url,
        caption = caption
    )

    fun MarkChatReadRequest.toDto(): MarkChatReadRequestDto = MarkChatReadRequestDto(
        lastSeenMessageId = lastSeenMessageId.toInt()
    )
}

