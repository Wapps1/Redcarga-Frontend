package com.wapps1.redcarga.features.fleet.data.local.dao

import androidx.room.*
import com.wapps1.redcarga.features.fleet.data.local.entities.RouteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutesDao {
    @Query("SELECT * FROM routes WHERE companyId = :companyId AND deletedLocally = 0")
    fun observeRoutes(companyId: Long): Flow<List<RouteEntity>>

    @Query("SELECT * FROM routes WHERE routeId = :routeId AND companyId = :companyId LIMIT 1")
    fun observeRoute(companyId: Long, routeId: Long): Flow<RouteEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(list: List<RouteEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: RouteEntity)

    @Query("DELETE FROM routes WHERE companyId = :companyId AND dirty = 0")
    suspend fun clearForCompany(companyId: Long)

    @Query("UPDATE routes SET dirty = 0, deletedLocally = 0 WHERE routeId = :routeId")
    suspend fun markAsSynced(routeId: Long)

    @Query("DELETE FROM routes WHERE routeId = :routeId")
    suspend fun deleteById(routeId: Long)

    @Transaction
    suspend fun replaceAllForCompany(companyId: Long, fresh: List<RouteEntity>) {
        clearForCompany(companyId)
        upsertAll(fresh)
    }
}


