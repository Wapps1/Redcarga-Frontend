package com.wapps1.redcarga.features.requests.data.remote.services

import com.wapps1.redcarga.features.requests.data.remote.models.ImageUploadResponseDto
import okhttp3.MultipartBody
import retrofit2.http.*

interface MediaService {
    @Headers("X-App-Auth: true")
    @Multipart
    @POST("/media/uploads:image")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part
    ): ImageUploadResponseDto
}

