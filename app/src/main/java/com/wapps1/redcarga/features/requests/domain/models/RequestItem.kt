package com.wapps1.redcarga.features.requests.domain.models

import java.math.BigDecimal

data class RequestItem(
    val itemId: Long? = null,
    val itemName: String,
    val heightCm: BigDecimal,
    val widthCm: BigDecimal,
    val lengthCm: BigDecimal,
    val weightKg: BigDecimal,
    val totalWeightKg: BigDecimal,
    val quantity: Int,
    val fragile: Boolean,
    val notes: String,
    val position: Int,
    val images: List<RequestImage>
) {
    fun getVolume(): BigDecimal {
        return heightCm.multiply(widthCm).multiply(lengthCm)
    }
    
    fun getTotalVolume(): BigDecimal {
        return getVolume().multiply(quantity.toBigDecimal())
    }
    
    fun hasImages(): Boolean = images.isNotEmpty()
    
    fun getPrimaryImage(): RequestImage? = images.firstOrNull()
}
