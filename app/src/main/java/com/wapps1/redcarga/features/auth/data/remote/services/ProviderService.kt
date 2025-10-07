package com.wapps1.redcarga.features.auth.data.remote.services

import com.wapps1.redcarga.features.auth.data.remote.models.CompanyRegisterRequestDto
import com.wapps1.redcarga.features.auth.data.remote.models.CompanyRegisterResponseDto
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ProviderService {
    @Headers("X-Firebase-Auth: true")
    @POST("providers/company/verify-and-register")
    suspend fun registerCompany(
        @Body body: CompanyRegisterRequestDto
    ): CompanyRegisterResponseDto
}
