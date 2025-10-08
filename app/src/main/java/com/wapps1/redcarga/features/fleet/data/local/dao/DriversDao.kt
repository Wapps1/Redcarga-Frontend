package com.wapps1.redcarga.features.fleet.data.local.dao

import androidx.room.*
import com.wapps1.redcarga.features.fleet.data.local.entities.DriverEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DriversDao {
    @Query("SELECT * FROM drivers WHERE companyId = :companyId AND deletedLocally = 0")
    fun observeDrivers(companyId: Long): Flow<List<DriverEntity>>

    @Query("SELECT * FROM drivers WHERE driverId = :driverId LIMIT 1")
    fun observeDriver(driverId: Long): Flow<DriverEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(list: List<DriverEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: DriverEntity)

    @Query("DELETE FROM drivers WHERE companyId = :companyId AND dirty = 0")
    suspend fun clearForCompany(companyId: Long)

    @Query("UPDATE drivers SET dirty = 0, deletedLocally = 0 WHERE driverId = :driverId")
    suspend fun markAsSynced(driverId: Long)

    @Query("DELETE FROM drivers WHERE driverId = :driverId")
    suspend fun deleteById(driverId: Long)

    @Query("SELECT * FROM drivers WHERE driverId = :driverId LIMIT 1")
    suspend fun find(driverId: Long): DriverEntity?

    @Transaction
    suspend fun replaceAllForCompany(companyId: Long, fresh: List<DriverEntity>) {
        clearForCompany(companyId)
        upsertAll(fresh)
    }
}


