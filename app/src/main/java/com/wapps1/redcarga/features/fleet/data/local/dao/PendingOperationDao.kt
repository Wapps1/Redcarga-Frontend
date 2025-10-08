package com.wapps1.redcarga.features.fleet.data.local.dao

import androidx.room.*
import com.wapps1.redcarga.features.fleet.data.local.entities.PendingOperationEntity

@Dao
interface PendingOperationDao {
    @Insert
    suspend fun insert(op: PendingOperationEntity): Long

    @Query("SELECT * FROM pending_ops WHERE status IN ('PENDING','RETRYING') ORDER BY id ASC LIMIT 1")
    suspend fun nextPending(): PendingOperationEntity?

    @Update
    suspend fun update(op: PendingOperationEntity)

    @Query("DELETE FROM pending_ops WHERE id = :id")
    suspend fun delete(id: Long)
}


