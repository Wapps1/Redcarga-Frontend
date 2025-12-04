package com.wapps1.redcarga.features.chat.data.remote.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * DTO de respuesta del endpoint GET /api/deals/chat/list
 */
@JsonClass(generateAdapter = true)
data class ChatListResponseDto(
    @Json(name = "chats") val chats: List<ChatListItemDto>
)

/**
 * DTO de un item de chat en la lista
 */
@JsonClass(generateAdapter = true)
data class ChatListItemDto(
    @Json(name = "quoteId") val quoteId: Int,
    @Json(name = "otherUserId") val otherUserId: Int,
    @Json(name = "otherCompanyId") val otherCompanyId: Int?,
    @Json(name = "otherCompanyLegalName") val otherCompanyLegalName: String?,
    @Json(name = "otherCompanyTradeName") val otherCompanyTradeName: String?,
    @Json(name = "otherPersonFullName") val otherPersonFullName: String?,
    @Json(name = "unreadCount") val unreadCount: Int
)

