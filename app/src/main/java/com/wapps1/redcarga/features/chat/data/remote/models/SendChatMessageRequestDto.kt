package com.wapps1.redcarga.features.chat.data.remote.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * DTO para enviar un mensaje de chat
 */
@JsonClass(generateAdapter = true)
data class SendChatMessageRequestDto(
    @Json(name = "dedupKey") val dedupKey: String?,
    @Json(name = "kind") val kind: String,        // "TEXT" o "IMAGE"
    @Json(name = "text") val text: String?,
    @Json(name = "url") val url: String?,
    @Json(name = "caption") val caption: String?
)

