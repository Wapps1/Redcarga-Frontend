package com.wapps1.redcarga.features.auth.domain.models.provider

import com.wapps1.redcarga.features.auth.domain.models.value.Email

/**
 * Request para registrar empresa en el BC Provider
 */
data class CompanyRegisterRequest(
    val accountId: Long,
    val legalName: String,
    val tradeName: String,
    val ruc: String,
    val email: Email,
    val phone: String,
    val address: String
)
