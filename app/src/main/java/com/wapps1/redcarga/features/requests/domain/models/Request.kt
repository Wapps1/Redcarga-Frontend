package com.wapps1.redcarga.features.requests.domain.models

import java.math.BigDecimal
import java.time.Instant

data class Request(
    val requestId: Long,
    val requesterAccountId: Long,
    val requesterNameSnapshot: String,
    val requestName: String,
    val requesterDocNumber: String,
    val status: RequestStatus,
    val createdAt: Instant,
    val updatedAt: Instant,
    val closedAt: Instant?,
    val origin: UbigeoSnapshot,
    val destination: UbigeoSnapshot,
    val itemsCount: Int,
    val totalWeightKg: BigDecimal,
    val paymentOnDelivery: Boolean,
    val items: List<RequestItem>
) {
    fun isOpen(): Boolean = status == RequestStatus.OPEN
    
    fun isClosed(): Boolean = status == RequestStatus.CLOSED
    
    fun getRouteDescription(): String {
        return "${origin.getShortLocation()} → ${destination.getShortLocation()}"
    }
    
    fun getTotalVolume(): BigDecimal {
        return items.sumOf { it.getTotalVolume() }
    }
    
    fun hasFragileItems(): Boolean = items.any { it.fragile }
    
    fun getFragileItemsCount(): Int = items.count { it.fragile }
}
