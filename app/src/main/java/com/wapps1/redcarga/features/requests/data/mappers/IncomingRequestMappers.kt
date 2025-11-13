package com.wapps1.redcarga.features.requests.data.mappers

import com.wapps1.redcarga.features.requests.data.local.entities.IncomingRequestEntity
import com.wapps1.redcarga.features.requests.data.remote.models.IncomingRequestSummaryDto
import com.wapps1.redcarga.features.requests.domain.models.IncomingRequestSummary
import com.wapps1.redcarga.features.requests.domain.models.RequestStatus
import java.time.Instant

/**
 * DTO → Entity
 */
fun IncomingRequestSummaryDto.toEntity(): IncomingRequestEntity {
    return IncomingRequestEntity(
        requestId = requestId,
        companyId = companyId,
        matchedRouteId = matchedRouteId,
        routeTypeId = routeTypeId,
        status = status,
        createdAt = createdAt,
        requesterName = requesterName,
        originDepartmentName = originDepartmentName,
        originProvinceName = originProvinceName,
        destDepartmentName = destDepartmentName,
        destProvinceName = destProvinceName,
        totalQuantity = totalQuantity
    )
}

/**
 * Entity → Domain
 */
fun IncomingRequestEntity.toDomain(): IncomingRequestSummary {
    return IncomingRequestSummary(
        requestId = requestId,
        companyId = companyId,
        matchedRouteId = matchedRouteId,
        routeTypeId = routeTypeId,
        status = try {
            RequestStatus.valueOf(status.uppercase())
        } catch (e: IllegalArgumentException) {
            RequestStatus.OPEN // Default fallback
        },
        createdAt = try {
            Instant.parse(createdAt)
        } catch (e: Exception) {
            Instant.now()
        },
        requesterName = requesterName,
        originDepartmentName = originDepartmentName,
        originProvinceName = originProvinceName,
        destDepartmentName = destDepartmentName,
        destProvinceName = destProvinceName,
        totalQuantity = totalQuantity
    )
}

