package com.wapps1.redcarga.features.requests.data.remote.models

data class RequestDto(
    val requestId: Long,
    val requesterAccountId: Long,
    val requesterNameSnapshot: String,
    val requestName: String,
    val requesterDocNumber: String,
    val status: String,
    val createdAt: String, // ISO 8601 string
    val updatedAt: String,
    val closedAt: String?,
    val origin: UbigeoSnapshotDto,
    val destination: UbigeoSnapshotDto,
    val itemsCount: Int,
    val totalWeightKg: Double,
    val paymentOnDelivery: Boolean,
    val items: List<RequestItemDto>
)
