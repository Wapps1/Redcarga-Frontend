package com.wapps1.redcarga.features.requests.data.mappers

import com.wapps1.redcarga.features.requests.data.local.entities.IncomingRequestEntity
import com.wapps1.redcarga.features.requests.data.remote.models.IncomingRequestSummaryDto
import com.wapps1.redcarga.features.requests.domain.models.IncomingRequestSummary
import com.wapps1.redcarga.features.requests.domain.models.RequestStatus
import java.time.Instant

/**
 * DTO → Entity
 * Maneja nulls del backend con valores por defecto
 */
fun IncomingRequestSummaryDto.toEntity(): IncomingRequestEntity {
    return IncomingRequestEntity(
        requestId = requestId,
        companyId = companyId,
        matchedRouteId = matchedRouteId ?: 0L,  // ✅ Default: 0 si es null
        routeTypeId = routeTypeId ?: 0L,        // ✅ Default: 0 si es null
        status = status,
        createdAt = createdAt ?: "",            // ✅ Default: string vacío si es null
        requesterName = requesterName ?: "",    // ✅ Default: string vacío si es null
        originDepartmentName = originDepartmentName ?: "",
        originProvinceName = originProvinceName ?: "",
        destDepartmentName = destDepartmentName ?: "",
        destProvinceName = destProvinceName ?: "",
        totalQuantity = totalQuantity ?: 0      // ✅ Default: 0 si es null
    )
}

/**
 * Entity → Domain
 * Maneja valores por defecto y conversiones seguras
 */
fun IncomingRequestEntity.toDomain(): IncomingRequestSummary {
    return IncomingRequestSummary(
        requestId = requestId,
        companyId = companyId,
        matchedRouteId = if (matchedRouteId == 0L) 0L else matchedRouteId,  // ✅ Mantener 0 si era null
        routeTypeId = if (routeTypeId == 0L) 0L else routeTypeId,           // ✅ Mantener 0 si era null
        status = try {
            RequestStatus.valueOf(status.uppercase())
        } catch (e: IllegalArgumentException) {
            RequestStatus.OPEN // Default fallback
        },
        createdAt = try {
            if (createdAt.isBlank()) Instant.now() else Instant.parse(createdAt)  // ✅ Manejar string vacío
        } catch (e: Exception) {
            Instant.now()
        },
        requesterName = requesterName.ifBlank { "Sin nombre" },  // ✅ Default si está vacío
        originDepartmentName = originDepartmentName.ifBlank { "" },
        originProvinceName = originProvinceName.ifBlank { "" },
        destDepartmentName = destDepartmentName.ifBlank { "" },
        destProvinceName = destProvinceName.ifBlank { "" },
        totalQuantity = totalQuantity
    )
}

