package com.wapps1.redcarga.features.auth.data.remote.services

import com.wapps1.redcarga.features.auth.data.remote.models.FirebaseSignInRequestDto
import com.wapps1.redcarga.features.auth.data.remote.models.FirebaseSignInResponseDto
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface FirebaseAuthService {
    @POST("v1/accounts:signInWithPassword")
    suspend fun signInWithPassword(
        @Body body: FirebaseSignInRequestDto,
        @Query("key") apiKey: String
    ): FirebaseSignInResponseDto
}
