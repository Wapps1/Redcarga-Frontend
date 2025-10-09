package com.wapps1.redcarga.features.requests.data.mappers

import com.wapps1.redcarga.features.requests.data.local.entities.*
import com.wapps1.redcarga.features.requests.data.local.dao.RequestsDao.RequestWithItems
import com.wapps1.redcarga.features.requests.data.local.dao.RequestsDao.RequestItemWithImages
import com.wapps1.redcarga.features.requests.data.remote.models.*
import com.wapps1.redcarga.features.requests.domain.models.*
import java.math.BigDecimal
import java.time.Instant

object RequestsMappers {

    // Helper functions for safe conversions
    private fun String.toRequestStatus(): RequestStatus {
        return try {
            RequestStatus.valueOf(this.uppercase())
        } catch (_: IllegalArgumentException) {
            RequestStatus.OPEN // Default fallback
        }
    }
    
    private fun RequestStatus.toStringValue(): String = this.name
    
    // Helper functions for Instant conversions (API 24 compatible)
    private fun String.toInstant(): Instant {
        return try {
            Instant.parse(this)
        } catch (e: Exception) {
            // Fallback to current time if parsing fails
            Instant.now()
        }
    }
    
    private fun Long.toInstant(): Instant {
        return try {
            Instant.ofEpochMilli(this)
        } catch (e: Exception) {
            // Fallback to current time if conversion fails
            Instant.now()
        }
    }
    
    private fun Instant.toEpochMilliSafe(): Long {
        return try {
            this.toEpochMilli()
        } catch (e: Exception) {
            // Fallback to current time
            System.currentTimeMillis()
        }
    }

    // DTO → Domain
    fun CreateRequestDto.toDomain(): CreateRequestRequest = CreateRequestRequest(
        origin = origin.toDomain(),
        destination = destination.toDomain(),
        paymentOnDelivery = paymentOnDelivery,
        requestName = request_name,
        items = items.map { it.toDomain() }
    )

    fun CreateRequestItemDto.toDomain(): CreateRequestItem = CreateRequestItem(
        itemName = itemName,
        heightCm = heightCm.toBigDecimal(),
        widthCm = widthCm.toBigDecimal(),
        lengthCm = lengthCm.toBigDecimal(),
        weightKg = weightKg.toBigDecimal(),
        totalWeightKg = totalWeightKg.toBigDecimal(),
        quantity = quantity,
        fragile = fragile,
        notes = notes,
        images = images.map { it.toDomain() }
    )

    fun CreateRequestImageDto.toDomain(): CreateRequestImage = CreateRequestImage(
        imageUrl = imageUrl,
        imagePosition = imagePosition
    )

    fun RequestDto.toDomain(): Request = Request(
        requestId = requestId,
        requesterAccountId = requesterAccountId,
        requesterNameSnapshot = requesterNameSnapshot,
        requestName = requestName,
        requesterDocNumber = requesterDocNumber,
        status = status.toRequestStatus(),
        createdAt = createdAt.toInstant(),
        updatedAt = updatedAt.toInstant(),
        closedAt = closedAt?.toInstant(),
        origin = origin.toDomain(),
        destination = destination.toDomain(),
        itemsCount = itemsCount,
        totalWeightKg = totalWeightKg.toBigDecimal(),
        paymentOnDelivery = paymentOnDelivery,
        items = items.map { it.toDomain() }
    )

    fun RequestSummaryDto.toDomain(): RequestSummary = RequestSummary(
        requestId = requestId,
        requestName = requestName,
        status = status.toRequestStatus(),
        createdAt = createdAt.toInstant(),
        updatedAt = updatedAt.toInstant(),
        closedAt = closedAt?.toInstant(),
        origin = origin.toDomain(),
        destination = destination.toDomain(),
        itemsCount = itemsCount,
        totalWeightKg = totalWeightKg.toBigDecimal(),
        paymentOnDelivery = paymentOnDelivery
    )

    fun RequestItemDto.toDomain(): RequestItem = RequestItem(
        itemId = itemId,
        itemName = itemName,
        heightCm = heightCm.toBigDecimal(),
        widthCm = widthCm.toBigDecimal(),
        lengthCm = lengthCm.toBigDecimal(),
        weightKg = weightKg.toBigDecimal(),
        totalWeightKg = totalWeightKg.toBigDecimal(),
        quantity = quantity,
        fragile = fragile,
        notes = notes,
        position = position,
        images = images.map { it.toDomain() }
    )

    fun RequestImageDto.toDomain(): RequestImage = RequestImage(
        imageId = imageId,
        imageUrl = imageUrl,
        imagePosition = imagePosition
    )

    fun UbigeoSnapshotDto.toDomain(): UbigeoSnapshot = UbigeoSnapshot(
        departmentCode = departmentCode,
        departmentName = departmentName,
        provinceCode = provinceCode,
        provinceName = provinceName,
        districtText = districtText
    )

    // Domain → DTO
    fun CreateRequestRequest.toDto(): CreateRequestDto = CreateRequestDto(
        origin = origin.toDto(),
        destination = destination.toDto(),
        paymentOnDelivery = paymentOnDelivery,
        request_name = requestName,
        items = items.map { it.toDto() }
    )

    fun CreateRequestItem.toDto(): CreateRequestItemDto = CreateRequestItemDto(
        itemName = itemName,
        heightCm = heightCm.toDouble(),
        widthCm = widthCm.toDouble(),
        lengthCm = lengthCm.toDouble(),
        weightKg = weightKg.toDouble(),
        totalWeightKg = totalWeightKg.toDouble(),
        quantity = quantity,
        fragile = fragile,
        notes = notes,
        images = images.map { it.toDto() }
    )

    fun CreateRequestImage.toDto(): CreateRequestImageDto = CreateRequestImageDto(
        imageUrl = imageUrl,
        imagePosition = imagePosition
    )

    fun UbigeoSnapshot.toDto(): UbigeoSnapshotDto = UbigeoSnapshotDto(
        departmentCode = departmentCode,
        departmentName = departmentName,
        provinceCode = provinceCode,
        provinceName = provinceName,
        districtText = districtText
    )

    // Domain → Entity
    fun Request.toEntity(): RequestEntity = RequestEntity(
        requestId = requestId,
        requesterAccountId = requesterAccountId,
        requesterNameSnapshot = requesterNameSnapshot,
        requestName = requestName,
        requesterDocNumber = requesterDocNumber,
        status = status.toStringValue(),
        createdAt = createdAt.toEpochMilliSafe(),
        updatedAt = updatedAt.toEpochMilliSafe(),
        closedAt = closedAt?.toEpochMilliSafe(),
        originDepartmentCode = origin.departmentCode,
        originDepartmentName = origin.departmentName,
        originProvinceCode = origin.provinceCode,
        originProvinceName = origin.provinceName,
        originDistrictText = origin.districtText,
        destinationDepartmentCode = destination.departmentCode,
        destinationDepartmentName = destination.departmentName,
        destinationProvinceCode = destination.provinceCode,
        destinationProvinceName = destination.provinceName,
        destinationDistrictText = destination.districtText,
        itemsCount = itemsCount,
        totalWeightKg = totalWeightKg.toString(),
        paymentOnDelivery = paymentOnDelivery
    )

    fun RequestItem.toEntity(requestId: Long): RequestItemEntity = RequestItemEntity(
        itemId = itemId ?: 0L, // Si es null, Room generará automáticamente
        requestId = requestId,
        itemName = itemName,
        heightCm = heightCm.toString(),
        widthCm = widthCm.toString(),
        lengthCm = lengthCm.toString(),
        weightKg = weightKg.toString(),
        totalWeightKg = totalWeightKg.toString(),
        quantity = quantity,
        fragile = fragile,
        notes = notes,
        position = position
    )

    fun RequestImage.toEntity(itemId: Long): RequestImageEntity = RequestImageEntity(
        imageId = imageId ?: 0L, // Si es null, Room generará automáticamente
        itemId = itemId,
        imageUrl = imageUrl,
        imagePosition = imagePosition
    )

    fun RequestSummary.toEntity(): RequestEntity = RequestEntity(
        requestId = requestId,
        requesterAccountId = 0L, // Will be set by repository
        requesterNameSnapshot = "",
        requestName = requestName,
        requesterDocNumber = "",
        status = status.toStringValue(),
        createdAt = createdAt.toEpochMilliSafe(),
        updatedAt = updatedAt.toEpochMilliSafe(),
        closedAt = closedAt?.toEpochMilliSafe(),
        originDepartmentCode = origin.departmentCode,
        originDepartmentName = origin.departmentName,
        originProvinceCode = origin.provinceCode,
        originProvinceName = origin.provinceName,
        originDistrictText = origin.districtText,
        destinationDepartmentCode = destination.departmentCode,
        destinationDepartmentName = destination.departmentName,
        destinationProvinceCode = destination.provinceCode,
        destinationProvinceName = destination.provinceName,
        destinationDistrictText = destination.districtText,
        itemsCount = itemsCount,
        totalWeightKg = totalWeightKg.toString(),
        paymentOnDelivery = paymentOnDelivery
    )

    // Entity → Domain
    fun RequestWithItems.toDomain(): Request = Request(
        requestId = request.requestId,
        requesterAccountId = request.requesterAccountId,
        requesterNameSnapshot = request.requesterNameSnapshot,
        requestName = request.requestName,
        requesterDocNumber = request.requesterDocNumber,
        status = request.status.toRequestStatus(),
        createdAt = request.createdAt.toInstant(),
        updatedAt = request.updatedAt.toInstant(),
        closedAt = request.closedAt?.toInstant(),
        origin = UbigeoSnapshot(
            departmentCode = request.originDepartmentCode,
            departmentName = request.originDepartmentName,
            provinceCode = request.originProvinceCode,
            provinceName = request.originProvinceName,
            districtText = request.originDistrictText
        ),
        destination = UbigeoSnapshot(
            departmentCode = request.destinationDepartmentCode,
            departmentName = request.destinationDepartmentName,
            provinceCode = request.destinationProvinceCode,
            provinceName = request.destinationProvinceName,
            districtText = request.destinationDistrictText
        ),
        itemsCount = request.itemsCount,
        totalWeightKg = request.totalWeightKg.toBigDecimal(),
        paymentOnDelivery = request.paymentOnDelivery,
        items = items.map { it.toDomain() }
    )

    fun RequestItemWithImages.toDomain(): RequestItem = RequestItem(
        itemId = item.itemId,
        itemName = item.itemName,
        heightCm = item.heightCm.toBigDecimal(),
        widthCm = item.widthCm.toBigDecimal(),
        lengthCm = item.lengthCm.toBigDecimal(),
        weightKg = item.weightKg.toBigDecimal(),
        totalWeightKg = item.totalWeightKg.toBigDecimal(),
        quantity = item.quantity,
        fragile = item.fragile,
        notes = item.notes,
        position = item.position,
        images = images.map { it.toDomain() }
    )

    fun RequestImageEntity.toDomain(): RequestImage = RequestImage(
        imageId = imageId,
        imageUrl = imageUrl,
        imagePosition = imagePosition
    )

    fun RequestWithItems.toDomainSummary(): RequestSummary = RequestSummary(
        requestId = request.requestId,
        requestName = request.requestName,
        status = request.status.toRequestStatus(),
        createdAt = request.createdAt.toInstant(),
        updatedAt = request.updatedAt.toInstant(),
        closedAt = request.closedAt?.toInstant(),
        origin = UbigeoSnapshot(
            departmentCode = request.originDepartmentCode,
            departmentName = request.originDepartmentName,
            provinceCode = request.originProvinceCode,
            provinceName = request.originProvinceName,
            districtText = request.originDistrictText
        ),
        destination = UbigeoSnapshot(
            departmentCode = request.destinationDepartmentCode,
            departmentName = request.destinationDepartmentName,
            provinceCode = request.destinationProvinceCode,
            provinceName = request.destinationProvinceName,
            districtText = request.destinationDistrictText
        ),
        itemsCount = request.itemsCount,
        totalWeightKg = request.totalWeightKg.toBigDecimal(),
        paymentOnDelivery = request.paymentOnDelivery
    )
}
