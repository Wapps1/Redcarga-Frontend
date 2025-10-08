package com.wapps1.redcarga.features.fleet.data.remote.services

import com.wapps1.redcarga.features.fleet.data.remote.models.CreateRouteResponseDto
import com.wapps1.redcarga.features.fleet.data.remote.models.RouteCreateDto
import com.wapps1.redcarga.features.fleet.data.remote.models.RouteDto
import com.wapps1.redcarga.features.fleet.data.remote.models.RouteUpdateDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface PlanningRoutesService {
    @Headers("X-App-Auth: true")
    @GET("planning/providers/{providerId}/routes")
    suspend fun listRoutesByProvider(@Path("providerId") providerId: Long): List<RouteDto>

    // Backend expone lista por "providers" aunque el id es de compañía
    @Headers("X-App-Auth: true")
    @GET("planning/providers/{companyId}/routes")
    suspend fun listRoutesByCompany(@Path("companyId") companyId: Long): List<RouteDto>

    @Headers("X-App-Auth: true")
    @GET("planning/companies/{companyId}/routes/{routeId}")
    suspend fun getRoute(
        @Path("companyId") companyId: Long,
        @Path("routeId") routeId: Long
    ): RouteDto

    @Headers("X-App-Auth: true")
    @POST("planning/companies/{companyId}/routes")
    suspend fun createRoute(
        @Path("companyId") companyId: Long,
        @Body body: RouteCreateDto
    ): CreateRouteResponseDto

    @Headers("X-App-Auth: true")
    @PUT("planning/companies/{companyId}/routes/{routeId}")
    suspend fun updateRoute(
        @Path("companyId") companyId: Long,
        @Path("routeId") routeId: Long,
        @Body body: RouteUpdateDto
    ): Response<Unit>

    @Headers("X-App-Auth: true")
    @DELETE("planning/companies/{companyId}/routes/{routeId}")
    suspend fun deleteRoute(
        @Path("companyId") companyId: Long,
        @Path("routeId") routeId: Long
    ): Response<Unit>
}


