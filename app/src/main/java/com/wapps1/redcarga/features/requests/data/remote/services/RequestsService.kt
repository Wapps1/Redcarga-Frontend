package com.wapps1.redcarga.features.requests.data.remote.services

import com.wapps1.redcarga.features.requests.data.remote.models.*
import retrofit2.http.*

interface RequestsService {
    @Headers("X-App-Auth: true")
    @POST("/requests/create-request")
    suspend fun createRequest(@Body request: CreateRequestDto): CreateRequestResponseDto

    @Headers("X-App-Auth: true")
    @GET("/requests")
    suspend fun getClientRequests(): List<RequestSummaryDto>

    @Headers("X-App-Auth: true")
    @GET("/requests/{requestId}")
    suspend fun getRequestById(@Path("requestId") requestId: Long): RequestDto
}
