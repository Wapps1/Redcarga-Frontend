package com.wapps1.redcarga.features.requests.data.repositories

import android.content.Context
import android.net.Uri
import android.util.Log
import com.wapps1.redcarga.features.requests.data.remote.services.MediaService
import com.wapps1.redcarga.features.requests.domain.RequestsDomainError
import com.wapps1.redcarga.features.requests.domain.repositories.MediaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "MediaRepository"

@Singleton
class MediaRepositoryImpl @Inject constructor(
    private val mediaService: MediaService,
    private val context: Context
) : MediaRepository {

    override suspend fun uploadImage(imageUri: Uri): String = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "📤 Iniciando subida de imagen: $imageUri")
            
            // Convertir URI a File temporal
            val imageFile = uriToFile(imageUri)
            
            // Crear RequestBody para el archivo
            val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            
            // Crear MultipartBody.Part
            val filePart = MultipartBody.Part.createFormData("file", imageFile.name, requestFile)
            
            // Subir al servidor
            val response = mediaService.uploadImage(filePart)
            
            Log.d(TAG, "✅ Imagen subida exitosamente. URL: ${response.secureUrl}")
            
            // Limpiar archivo temporal
            imageFile.delete()
            
            response.secureUrl
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al subir imagen: ${e.message}", e)
            throw RequestsDomainError.NetworkError
        }
    }

    /**
     * Convierte un URI a un File temporal
     */
    private fun uriToFile(uri: Uri): File {
        return try {
            // Intentar obtener el path real del URI
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            if (inputStream == null) {
                throw IllegalArgumentException("No se pudo abrir el URI: $uri")
            }

            // Crear archivo temporal
            val tempFile = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
            tempFile.parentFile?.mkdirs()

            // Copiar contenido del URI al archivo temporal
            FileOutputStream(tempFile).use { output ->
                inputStream.use { input ->
                    input.copyTo(output)
                }
            }

            tempFile
        } catch (e: Exception) {
            Log.e(TAG, "Error convirtiendo URI a File: ${e.message}", e)
            throw e
        }
    }
}

