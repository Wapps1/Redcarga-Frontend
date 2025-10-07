package com.wapps1.redcarga.features.auth.domain.models.identity

/**
 * Resultado de la creación de persona en el BC Identity
 */
data class PersonCreateResult(
    val passed: Boolean,
    val personId: Long
)
