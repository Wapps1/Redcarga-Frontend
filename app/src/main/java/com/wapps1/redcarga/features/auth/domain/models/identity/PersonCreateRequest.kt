package com.wapps1.redcarga.features.auth.domain.models.identity

/**
 * Request para crear persona en el BC Identity
 */
data class PersonCreateRequest(
    val accountId: Long,
    val fullName: String,
    val docTypeCode: String,
    val docNumber: String,
    val birthDate: String,
    val phone: String,
    val ruc: String
)
