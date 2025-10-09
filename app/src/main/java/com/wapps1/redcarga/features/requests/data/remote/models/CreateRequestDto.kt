package com.wapps1.redcarga.features.requests.data.remote.models

data class CreateRequestDto(
    val origin: UbigeoSnapshotDto,
    val destination: UbigeoSnapshotDto,
    val paymentOnDelivery: Boolean,
    val request_name: String, // Backend uses snake_case
    val items: List<CreateRequestItemDto>
)

data class CreateRequestItemDto(
    val itemName: String,
    val heightCm: Double,
    val widthCm: Double,
    val lengthCm: Double,
    val weightKg: Double,
    val totalWeightKg: Double,
    val quantity: Int,
    val fragile: Boolean,
    val notes: String,
    val images: List<CreateRequestImageDto>
)

data class CreateRequestImageDto(
    val imageUrl: String,
    val imagePosition: Int
)

data class CreateRequestResponseDto(val requestId: Long)
