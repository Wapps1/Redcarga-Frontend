package com.wapps1.redcarga.features.fleet.data.remote.services

import com.wapps1.redcarga.features.fleet.data.remote.models.GeoCatalogDto
import retrofit2.http.GET

interface GeoService {
    @GET("geo/catalog")
    suspend fun getCatalog(): GeoCatalogDto
}


