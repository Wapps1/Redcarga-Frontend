package com.wapps1.redcarga.features.fleet.domain.models.routes

import com.wapps1.redcarga.features.fleet.domain.models.common.CompanyId
import com.wapps1.redcarga.features.fleet.domain.models.common.RouteId

data class Route(
    val routeId: RouteId,
    val companyId: CompanyId,
    val companyName: String?,
    val routeType: RouteType,
    // Origen
    val originDeptCode: String,
    val originProvCode: String?,
    val originDistCode: String?,
    val originDeptName: String?,
    val originProvName: String?,
    val originDistName: String?,
    // Destino
    val destinationDeptCode: String,
    val destinationProvCode: String?,
    val destinationDistCode: String?,
    val destinationDeptName: String?,
    val destinationProvName: String?,
    val destinationDistName: String?,
    // Parada intermedia (opcional)
    val stopDeptCode: String?,
    val stopProvCode: String?,
    val stopDistCode: String?,
    val stopDeptName: String?,
    val stopProvName: String?,
    val stopDistName: String?,
    // Estado
    val active: Boolean
)


