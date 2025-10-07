package com.wapps1.redcarga.features.auth.data.local.dao

import androidx.room.*
import com.wapps1.redcarga.features.auth.data.local.entities.PersonDraftEntity

@Dao
interface PersonDraftDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: PersonDraftEntity)
    
    @Query("SELECT * FROM person_draft WHERE accountId = :accountId LIMIT 1")
    suspend fun find(accountId: Long): PersonDraftEntity?
    
    @Query("DELETE FROM person_draft WHERE accountId = :accountId")
    suspend fun delete(accountId: Long)
    
    @Query("DELETE FROM person_draft")
    suspend fun clear()
}
