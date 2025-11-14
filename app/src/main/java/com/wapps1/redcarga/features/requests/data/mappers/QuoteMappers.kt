package com.wapps1.redcarga.features.requests.data.mappers

import com.wapps1.redcarga.features.requests.data.remote.models.CreateQuoteRequestDto
import com.wapps1.redcarga.features.requests.data.remote.models.CreateQuoteResponseDto
import com.wapps1.redcarga.features.requests.data.remote.models.QuoteDetailDto
import com.wapps1.redcarga.features.requests.data.remote.models.QuoteItemDto
import com.wapps1.redcarga.features.requests.data.remote.models.QuoteItemRequestDto
import com.wapps1.redcarga.features.requests.data.remote.models.QuoteSummaryDto
import com.wapps1.redcarga.features.requests.domain.models.CreateQuoteRequest
import com.wapps1.redcarga.features.requests.domain.models.CreateQuoteResponse
import com.wapps1.redcarga.features.requests.domain.models.QuoteDetail
import com.wapps1.redcarga.features.requests.domain.models.QuoteItem
import com.wapps1.redcarga.features.requests.domain.models.QuoteItemRequest
import com.wapps1.redcarga.features.requests.domain.models.QuoteSummary
import java.time.Instant

/**
 * Mappers para cotizaciones
 */
object QuoteMappers {
    
    // ========== CREATE QUOTE ==========
    
    fun CreateQuoteRequest.toDto(): CreateQuoteRequestDto {
        return CreateQuoteRequestDto(
            requestId = requestId,
            companyId = companyId,
            totalAmount = totalAmount.toDouble(),
            currency = currency,
            items = items.map { it.toDto() }
        )
    }
    
    fun QuoteItemRequest.toDto(): QuoteItemRequestDto {
        return QuoteItemRequestDto(
            requestItemId = requestItemId,
            qty = qty.toDouble()
        )
    }
    
    fun CreateQuoteResponseDto.toDomain(): CreateQuoteResponse {
        return CreateQuoteResponse(
            quoteId = quoteId
        )
    }
    
    // ========== QUOTE SUMMARY ==========
    
    fun QuoteSummaryDto.toDomain(): QuoteSummary {
        return QuoteSummary(
            quoteId = quoteId,
            requestId = requestId,
            companyId = companyId,
            totalAmount = totalAmount,
            currencyCode = currencyCode,
            createdAt = Instant.parse(createdAt)
        )
    }
    
    // ========== QUOTE DETAIL ==========
    
    fun QuoteDetailDto.toDomain(): QuoteDetail {
        return QuoteDetail(
            quoteId = quoteId,
            requestId = requestId,
            companyId = companyId,
            createdByAccountId = createdByAccountId,
            stateCode = stateCode,
            currencyCode = currencyCode,
            totalAmount = totalAmount,
            version = version,
            createdAt = Instant.parse(createdAt),
            updatedAt = Instant.parse(updatedAt),
            items = items.map { it.toDomain() }
        )
    }
    
    fun QuoteItemDto.toDomain(): QuoteItem {
        return QuoteItem(
            quoteItemId = quoteItemId,
            requestItemId = requestItemId,
            qty = qty,
            version = version
        )
    }
}

