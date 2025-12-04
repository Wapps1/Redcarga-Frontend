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
        android.util.Log.d("QuoteMappers", "🔄 MAPPER: Convirtiendo CreateQuoteRequest (Domain) → CreateQuoteRequestDto (DTO)")
        android.util.Log.d("QuoteMappers", "   Conversiones:")
        android.util.Log.d("QuoteMappers", "      requestId: $requestId (Long) → ${requestId.toInt()} (Int)")
        android.util.Log.d("QuoteMappers", "      companyId: $companyId (Long) → ${companyId.toInt()} (Int)")
        android.util.Log.d("QuoteMappers", "      totalAmount: $totalAmount (BigDecimal) → ${totalAmount.toDouble()} (Double)")
        android.util.Log.d("QuoteMappers", "      currency: $currency (String) → $currency (String) ✅")
        android.util.Log.d("QuoteMappers", "      items: ${items.size} items")
        
        return CreateQuoteRequestDto(
            requestId = requestId.toInt(), // ⭐ Convertir Long → Int para coincidir con backend (Integer)
            companyId = companyId.toInt(), // ⭐ Convertir Long → Int para coincidir con backend (Integer)
            totalAmount = totalAmount.toDouble(),
            currency = currency, // ✅ Backend acepta "PEN" como String y lo convierte a Currency enum
            items = items.map { it.toDto() }
        )
    }
    
    fun QuoteItemRequest.toDto(): QuoteItemRequestDto {
        android.util.Log.d("QuoteMappers", "   Item: requestItemId=${requestItemId} (Long) → ${requestItemId.toInt()} (Int), qty=${qty} (BigDecimal) → ${qty.toDouble()} (Double)")
        return QuoteItemRequestDto(
            requestItemId = requestItemId.toInt(), // ⭐ Convertir Long → Int para coincidir con backend (Integer)
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

