package com.wapps1.redcarga.features.requests.data.remote.models

data class RequestItemDto(
    val itemId: Long,
    val itemName: String,
    val heightCm: Double,
    val widthCm: Double,
    val lengthCm: Double,
    val weightKg: Double,
    val totalWeightKg: Double,
    val quantity: Int,
    val fragile: Boolean,
    val notes: String?,  // ✅ Ahora acepta null
    val position: Int,
    val images: List<RequestImageDto>
)
