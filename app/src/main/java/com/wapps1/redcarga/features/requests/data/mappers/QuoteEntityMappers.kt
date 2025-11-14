package com.wapps1.redcarga.features.requests.data.mappers

import com.wapps1.redcarga.features.requests.data.local.entities.QuoteSummaryEntity
import com.wapps1.redcarga.features.requests.data.remote.models.QuoteSummaryDto
import com.wapps1.redcarga.features.requests.domain.models.QuoteSummary
import java.time.Instant

/**
 * Mappers para Quote entities de Room
 */

/**
 * Mappers para Quote entities de Room
 */

// DTO -> Entity
fun QuoteSummaryDto.toEntity(): QuoteSummaryEntity {
    return QuoteSummaryEntity(
        quoteId = quoteId,
        requestId = requestId,
        companyId = companyId,
        totalAmount = totalAmount,
        currencyCode = currencyCode,
        createdAt = createdAt
    )
}

// Entity -> Domain
fun QuoteSummaryEntity.toDomain(): QuoteSummary {
    return QuoteSummary(
        quoteId = quoteId,
        requestId = requestId,
        companyId = companyId,
        totalAmount = totalAmount,
        currencyCode = currencyCode,
        createdAt = Instant.parse(createdAt)
    )
}

