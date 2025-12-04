package com.wapps1.redcarga.features.deals.data.remote.models

import com.squareup.moshi.Json

/**
 * DTO para request de aplicar cambios
 */
data class ApplyChangeRequestDto(
    @Json(name = "items")
    val items: List<ChangeItemDto>
)

