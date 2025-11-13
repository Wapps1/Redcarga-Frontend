package com.wapps1.redcarga.features.requests.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.wapps1.redcarga.features.requests.data.local.entities.IncomingRequestEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para acceder a las solicitudes entrantes del inbox de proveedores
 */
@Dao
interface IncomingRequestsDao {
    
    /**
     * Observa todas las solicitudes entrantes para una compañía específica
     */
    @Query("SELECT * FROM incoming_requests WHERE companyId = :companyId ORDER BY createdAt DESC")
    fun observeInbox(companyId: Long): Flow<List<IncomingRequestEntity>>
    
    /**
     * Reemplaza todas las solicitudes de una compañía (delete + insert)
     */
    @Transaction
    suspend fun replaceAll(companyId: Long, requests: List<IncomingRequestEntity>) {
        deleteAllForCompany(companyId)
        insertAll(requests)
    }
    
    /**
     * Elimina todas las solicitudes de una compañía
     */
    @Query("DELETE FROM incoming_requests WHERE companyId = :companyId")
    suspend fun deleteAllForCompany(companyId: Long)
    
    /**
     * Inserta o reemplaza solicitudes
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(requests: List<IncomingRequestEntity>)
    
    /**
     * Obtiene una solicitud específica
     */
    @Query("SELECT * FROM incoming_requests WHERE requestId = :requestId LIMIT 1")
    suspend fun getById(requestId: Long): IncomingRequestEntity?
}

