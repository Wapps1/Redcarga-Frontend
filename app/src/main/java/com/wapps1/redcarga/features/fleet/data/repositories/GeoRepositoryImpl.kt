package com.wapps1.redcarga.features.fleet.data.repositories

import android.util.Log
import com.wapps1.redcarga.features.fleet.data.network.toFleetDomainError
import com.wapps1.redcarga.features.fleet.data.local.dao.GeoDao
import com.wapps1.redcarga.features.fleet.data.local.dao.MetaDao
import com.wapps1.redcarga.features.fleet.data.local.entities.DepartmentEntity
import com.wapps1.redcarga.features.fleet.data.local.entities.MetaEntity
import com.wapps1.redcarga.features.fleet.data.local.entities.ProvinceEntity
import com.wapps1.redcarga.features.fleet.data.remote.services.GeoService
import com.wapps1.redcarga.features.fleet.domain.DomainError
import com.wapps1.redcarga.features.fleet.domain.DomainException
import com.wapps1.redcarga.features.fleet.domain.models.geo.GeoCatalog
import com.wapps1.redcarga.features.fleet.domain.repositories.GeoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val TAG_GEO_REPO = "GeoRepository"

class GeoRepositoryImpl @Inject constructor(
    private val service: GeoService,
    private val geoDao: GeoDao,
    private val metaDao: MetaDao
) : GeoRepository {

    override fun observeCatalog(): Flow<GeoCatalog?> =
        combine(
            geoDao.observeDepartments(),
            geoDao.observeProvinces()
        ) { depts, provs ->
            Log.d(TAG_GEO_REPO, "observeCatalog Room emits: depts=${depts.size}, provs=${provs.size}")
            GeoCatalog(
                departments = depts.map { com.wapps1.redcarga.features.fleet.domain.models.geo.Department(it.code, it.name) },
                provinces = provs.map { com.wapps1.redcarga.features.fleet.domain.models.geo.Province(it.code, it.departmentCode, it.name) }
            )
        }

    override suspend fun refreshCatalog(force: Boolean) = withContext(Dispatchers.IO) {
        Log.d(TAG_GEO_REPO, "refreshCatalog(force=$force)")
        runCatching {
            val now = System.currentTimeMillis()
            if (!force) {
                val last = metaDao.getValue("geo_catalog_last_sync_at") ?: 0L
                if (now - last < 7L * 24 * 60 * 60 * 1000) {
                    Log.d(TAG_GEO_REPO, "skip refresh; age=${now - last}ms")
                    return@withContext
                }
            }
            val dto = service.getCatalog()
            geoDao.upsertDepartments(dto.departments.map { DepartmentEntity(it.code, it.name) })
            geoDao.upsertProvinces(dto.provinces.map { ProvinceEntity(it.code, it.departmentCode, it.name) })
            metaDao.upsert(MetaEntity("geo_catalog_last_sync_at", now))
            Log.d(TAG_GEO_REPO, "refreshCatalog OK: depts=${dto.departments.size}, provs=${dto.provinces.size}")
        }.getOrElse { e ->
            Log.e(TAG_GEO_REPO, "refreshCatalog failed", e)
            throw DomainException(e.toFleetDomainError(), e)
        }
    }

    override suspend fun getDepartmentName(code: String): String? =
        withContext(Dispatchers.IO) {
            Log.d(TAG_GEO_REPO, "getDepartmentName(${code})")
            geoDao.getDepartmentName(code)
        }

    override suspend fun getProvinceName(code: String): String? =
        withContext(Dispatchers.IO) {
            Log.d(TAG_GEO_REPO, "getProvinceName(${code})")
            geoDao.getProvinceName(code)
        }
}


