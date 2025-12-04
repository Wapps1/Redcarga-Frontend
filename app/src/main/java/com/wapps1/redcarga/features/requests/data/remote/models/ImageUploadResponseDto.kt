package com.wapps1.redcarga.features.requests.data.remote.models

import com.squareup.moshi.Json

data class ImageUploadResponseDto(
    @Json(name = "publicId")
    val publicId: String,
    
    @Json(name = "secureUrl")
    val secureUrl: String
)

