package com.wapps1.redcarga.features.requests.data.remote.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * DTO para el resumen de solicitudes entrantes al inbox de un proveedor
 * Endpoint: GET /planning/companies/{companyId}/request-inbox?status={opcional}
 * 
 * Nota: Según la documentación del backend, varios campos pueden ser null
 */
@JsonClass(generateAdapter = true)
data class IncomingRequestSummaryDto(
    @Json(name = "requestId") val requestId: Long,
    @Json(name = "companyId") val companyId: Long,
    @Json(name = "matchedRouteId") val matchedRouteId: Long?,      // ✅ Nullable según backend
    @Json(name = "routeTypeId") val routeTypeId: Long?,           // ✅ Nullable según backend
    @Json(name = "status") val status: String,
    @Json(name = "createdAt") val createdAt: String?,            // ✅ Nullable según backend (ISO 8601)
    @Json(name = "requesterName") val requesterName: String?,     // ✅ Nullable según backend
    @Json(name = "originDepartmentName") val originDepartmentName: String?, // ✅ Nullable según backend
    @Json(name = "originProvinceName") val originProvinceName: String?,     // ✅ Nullable según backend
    @Json(name = "destDepartmentName") val destDepartmentName: String?,      // ✅ Nullable según backend
    @Json(name = "destProvinceName") val destProvinceName: String?,         // ✅ Nullable según backend
    @Json(name = "totalQuantity") val totalQuantity: Int?          // ✅ Nullable según backend
)

