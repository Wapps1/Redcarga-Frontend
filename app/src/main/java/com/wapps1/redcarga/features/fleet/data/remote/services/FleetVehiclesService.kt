package com.wapps1.redcarga.features.fleet.data.remote.services

import com.wapps1.redcarga.features.fleet.data.remote.models.CreateVehicleResponseDto
import com.wapps1.redcarga.features.fleet.data.remote.models.VehicleDto
import com.wapps1.redcarga.features.fleet.data.remote.models.VehicleUpsertDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface FleetVehiclesService {
    @Headers("X-App-Auth: true")
    @GET("fleet/companies/{companyId}/vehicles")
    suspend fun listVehicles(@Path("companyId") companyId: Long): List<VehicleDto>

    @Headers("X-App-Auth: true")
    @GET("fleet/vehicles/{vehicleId}")
    suspend fun getVehicle(@Path("vehicleId") vehicleId: Long): VehicleDto

    @Headers("X-App-Auth: true")
    @POST("fleet/companies/{companyId}/vehicles")
    suspend fun createVehicle(
        @Path("companyId") companyId: Long,
        @Body body: VehicleUpsertDto
    ): CreateVehicleResponseDto

    @Headers("X-App-Auth: true")
    @PUT("fleet/vehicles/{vehicleId}")
    suspend fun updateVehicle(
        @Path("vehicleId") vehicleId: Long,
        @Body body: VehicleUpsertDto
    ): Response<Unit>

    @Headers("X-App-Auth: true")
    @DELETE("fleet/vehicles/{vehicleId}")
    suspend fun deleteVehicle(@Path("vehicleId") vehicleId: Long): Response<Unit>
}


