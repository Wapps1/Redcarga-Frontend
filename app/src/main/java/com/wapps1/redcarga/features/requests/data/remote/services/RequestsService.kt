package com.wapps1.redcarga.features.requests.data.remote.services

import com.wapps1.redcarga.features.requests.data.remote.models.*
import retrofit2.http.*

interface RequestsService {
    @POST("/requests/create-request")
    suspend fun createRequest(@Body request: CreateRequestDto): CreateRequestResponseDto
    
    @GET("/requests")
    suspend fun getClientRequests(): List<RequestSummaryDto>
    
    @GET("/requests/{requestId}")
    suspend fun getRequestById(@Path("requestId") requestId: Long): RequestDto
}
