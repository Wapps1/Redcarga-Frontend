package com.wapps1.redcarga.features.deals.data.remote.models

import com.squareup.moshi.Json

/**
 * DTO para response de aplicar cambios
 * En TRATO siempre retorna changeId (nunca null)
 */
data class ApplyChangeResponseDto(
    @Json(name = "changeId")
    val changeId: Int
)

