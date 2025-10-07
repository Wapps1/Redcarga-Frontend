package com.wapps1.redcarga.features.auth.data.local.dao

import androidx.room.*
import com.wapps1.redcarga.features.auth.data.local.entities.SignupIntentEntity

@Dao
interface SignupIntentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: SignupIntentEntity)
    
    @Query("SELECT * FROM signup_intent WHERE accountId = :accountId LIMIT 1")
    suspend fun findByAccount(accountId: Long): SignupIntentEntity?
    
    @Query("DELETE FROM signup_intent")
    suspend fun clear()
}
