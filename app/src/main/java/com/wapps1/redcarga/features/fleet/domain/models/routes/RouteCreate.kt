package com.wapps1.redcarga.features.fleet.domain.models.routes

data class RouteCreate(
    val routeType: RouteType,
    val originDeptCode: String,
    val originProvCode: String,
    val originDistCode: String,
    val destinationDeptCode: String,
    val destinationProvCode: String,
    val destinationDistCode: String,
    val stopDeptCode: String?,
    val stopProvCode: String?,
    val stopDistCode: String?,
    val active: Boolean
)

data class RouteUpdate(
    val routeType: RouteType,
    val originDeptCode: String,
    val originProvCode: String,
    val originDistCode: String,
    val destinationDeptCode: String,
    val destinationProvCode: String,
    val destinationDistCode: String,
    val stopDeptCode: String?,
    val stopProvCode: String?,
    val stopDistCode: String?,
    val active: Boolean
)

data class CreateRouteResult(val routeId: Long)


