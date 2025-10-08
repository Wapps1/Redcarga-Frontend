package com.wapps1.redcarga.features.fleet.domain.repositories

import com.wapps1.redcarga.features.fleet.domain.models.geo.GeoCatalog
import kotlinx.coroutines.flow.Flow

interface GeoRepository {
    fun observeCatalog(): Flow<GeoCatalog?>
    suspend fun refreshCatalog(force: Boolean = false)
    suspend fun getDepartmentName(code: String): String?

    suspend fun getProvinceName(code: String): String?
}


