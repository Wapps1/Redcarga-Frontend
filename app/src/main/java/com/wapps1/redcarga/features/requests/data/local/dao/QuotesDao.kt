package com.wapps1.redcarga.features.requests.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.wapps1.redcarga.features.requests.data.local.entities.QuoteSummaryEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para cotizaciones en caché local
 */
@Dao
interface QuotesDao {
    
    /**
     * Observa las cotizaciones de una compañía
     */
    @Query("SELECT * FROM quotes WHERE companyId = :companyId ORDER BY createdAt DESC")
    fun observeQuotesByCompany(companyId: Long): Flow<List<QuoteSummaryEntity>>
    
    /**
     * Obtiene todas las cotizaciones de una compañía (una sola vez)
     */
    @Query("SELECT * FROM quotes WHERE companyId = :companyId")
    suspend fun getQuotesByCompany(companyId: Long): List<QuoteSummaryEntity>
    
    /**
     * Inserta o actualiza cotizaciones
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(quotes: List<QuoteSummaryEntity>)
    
    /**
     * Elimina todas las cotizaciones de una compañía
     */
    @Query("DELETE FROM quotes WHERE companyId = :companyId")
    suspend fun deleteAllForCompany(companyId: Long)
    
    /**
     * Reemplaza todas las cotizaciones de una compañía
     */
    @Transaction
    suspend fun replaceAll(companyId: Long, quotes: List<QuoteSummaryEntity>) {
        deleteAllForCompany(companyId)
        insertAll(quotes)
    }
    
    /**
     * Obtiene los IDs de solicitudes cotizadas por una compañía
     */
    @Query("SELECT DISTINCT requestId FROM quotes WHERE companyId = :companyId")
    fun observeQuotedRequestIds(companyId: Long): Flow<List<Long>>
}

