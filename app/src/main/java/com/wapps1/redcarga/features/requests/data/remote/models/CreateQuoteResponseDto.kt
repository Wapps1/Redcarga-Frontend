package com.wapps1.redcarga.features.requests.data.remote.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateQuoteResponseDto(
    val quoteId: Long
)

