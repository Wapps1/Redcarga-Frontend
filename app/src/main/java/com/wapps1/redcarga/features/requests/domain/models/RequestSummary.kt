package com.wapps1.redcarga.features.requests.domain.models

import java.math.BigDecimal
import java.time.Instant

data class RequestSummary(
    val requestId: Long,
    val requestName: String,
    val status: RequestStatus,
    val createdAt: Instant,
    val updatedAt: Instant,
    val closedAt: Instant?,
    val origin: UbigeoSnapshot,
    val destination: UbigeoSnapshot,
    val itemsCount: Int,
    val totalWeightKg: BigDecimal,
    val paymentOnDelivery: Boolean
) {
    fun getRouteDescription(): String {
        return "${origin.getShortLocation()} → ${destination.getShortLocation()}"
    }
    
    fun isOpen(): Boolean = status == RequestStatus.OPEN
    
    fun isClosed(): Boolean = status == RequestStatus.CLOSED
}
