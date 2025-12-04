package com.wapps1.redcarga.features.requests.domain.repositories

import android.net.Uri

interface MediaRepository {
    /**
     * Sube una imagen al servidor y retorna la URL segura de la imagen
     * @param imageUri URI local de la imagen a subir
     * @return URL segura de la imagen en la nube
     */
    suspend fun uploadImage(imageUri: Uri): String
}

