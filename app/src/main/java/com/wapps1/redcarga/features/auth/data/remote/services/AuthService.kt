package com.wapps1.redcarga.features.auth.data.remote.services

import com.wapps1.redcarga.features.auth.data.remote.models.*
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface AuthService {
    @POST("iam/register-start")
    suspend fun registerStart(
        @Body body: RegisterStartRequestDto
    ): RegisterStartResponseDto

    @Headers("X-Firebase-Auth: true")
    @POST("iam/login")
    suspend fun login(
        @Body body: AppLoginRequestDto
    ): AppLoginResponseDto
}
