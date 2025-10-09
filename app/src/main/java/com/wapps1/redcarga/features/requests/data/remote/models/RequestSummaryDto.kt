package com.wapps1.redcarga.features.requests.data.remote.models

data class RequestSummaryDto(
    val requestId: Long,
    val requestName: String,
    val status: String,
    val createdAt: String,
    val updatedAt: String,
    val closedAt: String?,
    val origin: UbigeoSnapshotDto,
    val destination: UbigeoSnapshotDto,
    val itemsCount: Int,
    val totalWeightKg: Double,
    val paymentOnDelivery: Boolean
)
