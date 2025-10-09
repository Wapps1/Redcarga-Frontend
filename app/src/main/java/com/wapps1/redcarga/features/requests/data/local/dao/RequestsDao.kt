package com.wapps1.redcarga.features.requests.data.local.dao

import androidx.room.*
import com.wapps1.redcarga.features.requests.data.local.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RequestsDao {
    @Transaction
    @Query("SELECT * FROM requests WHERE requesterAccountId = :accountId AND deletedLocally = 0 ORDER BY createdAt DESC")
    fun observeClientRequests(accountId: Long): Flow<List<RequestWithItems>>
    
    @Transaction
    @Query("SELECT * FROM requests WHERE requestId = :requestId LIMIT 1")
    fun observeRequest(requestId: Long): Flow<RequestWithItems?>
    
    @Transaction
    @Query("SELECT * FROM requests WHERE requesterAccountId = :accountId AND deletedLocally = 0 ORDER BY createdAt DESC")
    suspend fun getClientRequests(accountId: Long): List<RequestWithItems>
    
    @Transaction
    @Query("SELECT * FROM requests WHERE requestId = :requestId LIMIT 1")
    suspend fun getRequest(requestId: Long): RequestWithItems?
    
    @Transaction
    @Query("SELECT * FROM requests WHERE requestId = :requestId LIMIT 1")
    suspend fun getRequestById(requestId: Long): RequestWithItems?
    
    @Transaction
    suspend fun saveRequestWithItems(request: RequestEntity, items: List<RequestItemEntity>, images: List<RequestImageEntity>) {
        upsertRequest(request)
        deleteItemsForRequest(request.requestId)
        upsertItems(items)
        upsertImages(images)
    }
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRequest(request: RequestEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertItems(items: List<RequestItemEntity>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertImages(images: List<RequestImageEntity>)
    
    @Query("DELETE FROM request_items WHERE requestId = :requestId")
    suspend fun deleteItemsForRequest(requestId: Long)
    
    @Query("DELETE FROM request_images WHERE itemId IN (SELECT itemId FROM request_items WHERE requestId = :requestId)")
    suspend fun deleteImagesForRequest(requestId: Long)
    
    @Query("DELETE FROM requests WHERE requestId = :requestId")
    suspend fun deleteRequest(requestId: Long)
    
    @Query("DELETE FROM requests WHERE requesterAccountId = :accountId AND dirty = 0")
    suspend fun clearForAccount(accountId: Long)
    
    @Query("DELETE FROM request_items WHERE requestId IN (SELECT requestId FROM requests WHERE requesterAccountId = :accountId)")
    suspend fun clearItemsForAccount(accountId: Long)
    
    @Query("DELETE FROM request_images WHERE itemId IN (SELECT itemId FROM request_items WHERE requestId IN (SELECT requestId FROM requests WHERE requesterAccountId = :accountId))")
    suspend fun clearImagesForAccount(accountId: Long)
    
    @Query("SELECT EXISTS(SELECT 1 FROM requests WHERE requestId = :requestId)")
    suspend fun hasRequest(requestId: Long): Boolean
    
    @Query("SELECT EXISTS(SELECT 1 FROM requests WHERE requesterAccountId = :accountId)")
    suspend fun hasClientRequests(accountId: Long): Boolean

    /**
     * Clase de datos para representar una solicitud con sus items anidados
     * Utilizada por Room para consultas con @Transaction
     */
    data class RequestWithItems(
        @Embedded val request: RequestEntity,
        @Relation(
            parentColumn = "requestId",
            entityColumn = "requestId",
            entity = RequestItemEntity::class
        )
        val items: List<RequestItemWithImages>
    )

    /**
     * Clase de datos para representar un item de solicitud con sus imágenes anidadas
     * Utilizada por Room para consultas con @Transaction
     */
    data class RequestItemWithImages(
        @Embedded val item: RequestItemEntity,
        @Relation(
            parentColumn = "itemId",
            entityColumn = "itemId",
            entity = RequestImageEntity::class
        )
        val images: List<RequestImageEntity>
    )
}
