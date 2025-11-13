package com.wapps1.redcarga.features.requests.data.remote.models

/**
 * DTO para el resumen de solicitudes entrantes al inbox de un proveedor
 * Endpoint: GET /planning/companies/{companyId}/request-inbox
 */
data class IncomingRequestSummaryDto(
    val requestId: Long,
    val companyId: Long,
    val matchedRouteId: Long,
    val routeTypeId: Long,
    val status: String,
    val createdAt: String, // ISO 8601
    val requesterName: String,
    val originDepartmentName: String,
    val originProvinceName: String,
    val destDepartmentName: String,
    val destProvinceName: String,
    val totalQuantity: Int
)

