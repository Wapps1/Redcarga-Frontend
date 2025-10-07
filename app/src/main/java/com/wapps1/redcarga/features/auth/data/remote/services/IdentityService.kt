package com.wapps1.redcarga.features.auth.data.remote.services

import com.wapps1.redcarga.features.auth.data.remote.models.PersonCreateRequestDto
import com.wapps1.redcarga.features.auth.data.remote.models.PersonCreateResponseDto
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface IdentityService {
    @Headers("X-Firebase-Auth: true")
    @POST("identity/verify-and-create")
    suspend fun verifyAndCreate(
        @Body body: PersonCreateRequestDto
    ): PersonCreateResponseDto
}
