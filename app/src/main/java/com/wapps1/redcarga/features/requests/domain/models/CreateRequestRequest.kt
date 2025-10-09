package com.wapps1.redcarga.features.requests.domain.models

import java.math.BigDecimal

data class CreateRequestRequest(
    val origin: UbigeoSnapshot,
    val destination: UbigeoSnapshot,
    val paymentOnDelivery: Boolean,
    val requestName: String,
    val items: List<CreateRequestItem>
)

data class CreateRequestItem(
    val itemName: String,
    val heightCm: BigDecimal,
    val widthCm: BigDecimal,
    val lengthCm: BigDecimal,
    val weightKg: BigDecimal,
    val totalWeightKg: BigDecimal,
    val quantity: Int,
    val fragile: Boolean,
    val notes: String,
    val images: List<CreateRequestImage>
)

data class CreateRequestImage(
    val imageUrl: String,
    val imagePosition: Int
)
