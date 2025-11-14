package com.wapps1.redcarga.features.requests.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity Room para caché de cotizaciones
 */
@Entity(tableName = "quotes")
data class QuoteSummaryEntity(
    @PrimaryKey val quoteId: Long,
    val requestId: Long,
    val companyId: Long,
    val totalAmount: Double,
    val currencyCode: String,
    val createdAt: String // ISO 8601
)

