package com.wapps1.redcarga.features.auth.data.local.dao

import androidx.room.*
import com.wapps1.redcarga.features.auth.data.local.entities.AccountSnapshotEntity

@Dao
interface AccountSnapshotDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: AccountSnapshotEntity)
    
    @Query("SELECT * FROM account_snapshot WHERE accountId = :accountId LIMIT 1")
    suspend fun find(accountId: Long): AccountSnapshotEntity?
    
    @Query("DELETE FROM account_snapshot")
    suspend fun clear()
}
