package com.wapps1.redcarga.features.fleet.data.local.dao

import androidx.room.*
import com.wapps1.redcarga.features.fleet.data.local.entities.VehicleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VehiclesDao {
    @Query("SELECT * FROM vehicles WHERE companyId = :companyId AND deletedLocally = 0")
    fun observeVehicles(companyId: Long): Flow<List<VehicleEntity>>

    @Query("SELECT * FROM vehicles WHERE vehicleId = :vehicleId LIMIT 1")
    fun observeVehicle(vehicleId: Long): Flow<VehicleEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(list: List<VehicleEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: VehicleEntity)

    @Query("DELETE FROM vehicles WHERE companyId = :companyId AND dirty = 0")
    suspend fun clearForCompany(companyId: Long)

    @Query("UPDATE vehicles SET dirty = 0, deletedLocally = 0 WHERE vehicleId = :vehicleId")
    suspend fun markAsSynced(vehicleId: Long)

    @Query("DELETE FROM vehicles WHERE vehicleId = :vehicleId")
    suspend fun deleteById(vehicleId: Long)

    @Query("SELECT * FROM vehicles WHERE vehicleId = :vehicleId LIMIT 1")
    suspend fun find(vehicleId: Long): VehicleEntity?

    @Transaction
    suspend fun replaceAllForCompany(companyId: Long, fresh: List<VehicleEntity>) {
        clearForCompany(companyId)
        upsertAll(fresh)
    }
}


