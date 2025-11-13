package com.wapps1.redcarga.features.fleet.data.local.dao

import androidx.room.*
import com.wapps1.redcarga.features.fleet.data.local.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GeoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDepartments(list: List<DepartmentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProvinces(list: List<ProvinceEntity>)

    @Query("SELECT name FROM departments WHERE code = :code LIMIT 1")
    suspend fun getDepartmentName(code: String): String?

    @Query("SELECT name FROM provinces WHERE code = :code LIMIT 1")
    suspend fun getProvinceName(code: String): String?

    // Observables para componer GeoCatalog desde Room
    @Query("SELECT * FROM departments")
    fun observeDepartments(): Flow<List<DepartmentEntity>>

    @Query("SELECT * FROM provinces")
    fun observeProvinces(): Flow<List<ProvinceEntity>>
}

@Dao
interface MetaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(meta: MetaEntity)

    @Query("SELECT value FROM meta WHERE key = :key LIMIT 1")
    suspend fun getValue(key: String): Long?
}


