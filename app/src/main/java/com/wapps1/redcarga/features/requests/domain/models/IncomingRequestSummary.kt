package com.wapps1.redcarga.features.requests.domain.models

import java.time.Instant

/**
 * Modelo de dominio para el resumen de solicitudes entrantes a un proveedor
 */
data class IncomingRequestSummary(
    val requestId: Long,
    val companyId: Long,
    val matchedRouteId: Long,
    val routeTypeId: Long,
    val status: RequestStatus,
    val createdAt: Instant,
    val requesterName: String,
    val originDepartmentName: String,
    val originProvinceName: String,
    val destDepartmentName: String,
    val destProvinceName: String,
    val totalQuantity: Int
) {
    /**
     * Retorna la descripción de la ruta en formato "Origen → Destino"
     */
    fun getRouteDescription(): String {
        return "$originProvinceName, $originDepartmentName → $destProvinceName, $destDepartmentName"
    }
    
    /**
     * Verifica si la solicitud está abierta
     */
    fun isOpen(): Boolean = status == RequestStatus.OPEN
}

