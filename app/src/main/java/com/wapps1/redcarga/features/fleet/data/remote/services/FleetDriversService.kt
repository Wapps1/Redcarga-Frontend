package com.wapps1.redcarga.features.fleet.data.remote.services

import com.wapps1.redcarga.features.fleet.data.remote.models.CreateDriverResponseDto
import com.wapps1.redcarga.features.fleet.data.remote.models.DriverDto
import com.wapps1.redcarga.features.fleet.data.remote.models.DriverUpsertDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface FleetDriversService {
    @Headers("X-App-Auth: true")
    @GET("fleet/companies/{companyId}/drivers")
    suspend fun listDrivers(@Path("companyId") companyId: Long): List<DriverDto>

    @Headers("X-App-Auth: true")
    @GET("fleet/drivers/{driverId}")
    suspend fun getDriver(@Path("driverId") driverId: Long): DriverDto

    @Headers("X-App-Auth: true")
    @POST("fleet/companies/{companyId}/drivers")
    suspend fun createDriver(
        @Path("companyId") companyId: Long,
        @Body body: DriverUpsertDto
    ): CreateDriverResponseDto

    @Headers("X-App-Auth: true")
    @PUT("fleet/drivers/{driverId}")
    suspend fun updateDriver(
        @Path("driverId") driverId: Long,
        @Body body: DriverUpsertDto
    ): Response<Unit>

    @Headers("X-App-Auth: true")
    @DELETE("fleet/drivers/{driverId}")
    suspend fun deleteDriver(@Path("driverId") driverId: Long): Response<Unit>
}


