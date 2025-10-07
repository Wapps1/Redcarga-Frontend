package com.wapps1.redcarga.features.auth.domain.models.provider

/**
 * Resultado del registro de empresa en el BC Provider
 */
data class CompanyRegisterResult(
    val success: Boolean,
    val companyId: Long
)
