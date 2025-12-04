package com.wapps1.redcarga.features.deals.data.mappers

import com.wapps1.redcarga.features.deals.data.remote.models.*
import com.wapps1.redcarga.features.deals.domain.models.*
import java.time.Instant

/**
 * Mappers para convertir entre DTOs y modelos de dominio
 */
object DealsMappers {

    // Helper functions para conversiones seguras
    private fun String.toInstant(): Instant {
        return try {
            Instant.parse(this)
        } catch (e: Exception) {
            Instant.now()
        }
    }

    private fun String.toChangeFieldCode(): ChangeFieldCode {
        return try {
            ChangeFieldCode.valueOf(this.uppercase())
        } catch (_: IllegalArgumentException) {
            ChangeFieldCode.PRICE_TOTAL
        }
    }

    private fun String.toChangeKind(): ChangeKind {
        return try {
            ChangeKind.valueOf(this.uppercase())
        } catch (_: IllegalArgumentException) {
            ChangeKind.LIBRE
        }
    }

    private fun String.toChangeStatus(): ChangeStatus {
        return try {
            ChangeStatus.valueOf(this.uppercase())
        } catch (_: IllegalArgumentException) {
            ChangeStatus.APLICADO
        }
    }

    // ========== DTO → Domain ==========

    fun ChangeItemDto.toDomain(): ChangeItem = ChangeItem(
        changeItemId = changeItemId?.toLong(),  // ⚠️ NUEVO: convertir Int? a Long?
        fieldCode = fieldCode.toChangeFieldCode(),
        targetQuoteItemId = targetQuoteItemId?.toLong(),
        targetRequestItemId = targetRequestItemId?.toLong(),
        oldValue = oldValue,
        newValue = newValue
    )

    fun ChangeDto.toDomain(): Change = Change(
        changeId = changeId.toLong(),
        quoteId = quoteId?.toLong(),  // ⚠️ NUEVO: convertir Int? a Long?
        kindCode = kindCode.toChangeKind(),
        statusCode = statusCode.toChangeStatus(),
        createdBy = createdBy.toLong(),
        createdAt = createdAt.toInstant(),
        items = items.map { it.toDomain() }
    )

    fun ApplyChangeResponseDto.toDomain(): ApplyChangeResponse = ApplyChangeResponse(
        changeId = changeId.toLong()
    )

    // ========== Domain → DTO ==========

    fun ChangeItem.toDto(): ChangeItemDto = ChangeItemDto(
        changeItemId = null,  // ⚠️ SIEMPRE null para requests (el backend no espera este campo)
        fieldCode = fieldCode.name,
        targetQuoteItemId = targetQuoteItemId?.toInt(),
        targetRequestItemId = targetRequestItemId?.toInt(),
        oldValue = oldValue,
        newValue = newValue
    )

    fun ApplyChangeRequest.toDto(): ApplyChangeRequestDto = ApplyChangeRequestDto(
        items = items.map { it.toDto() }
    )

    // ========== Acceptance: DTO → Domain ==========

    fun AcceptanceResponseDto.toDomain(): AcceptanceResponse = AcceptanceResponse(
        acceptanceId = acceptanceId.toLong()
    )

    fun ConfirmAcceptanceResponseDto.toDomain(): ConfirmAcceptanceResponse = ConfirmAcceptanceResponse(
        ok = ok
    )

    // ========== Acceptance: Domain → DTO ==========

    fun AcceptanceRequest.toDto(): AcceptanceRequestDto = AcceptanceRequestDto(
        idempotencyKey = idempotencyKey,
        note = note
    )
}

