package com.wapps1.redcarga.features.auth.domain.models.identity

/**
 * Draft de persona para persistencia local (Room)
 * Datos del formulario antes de enviar al backend
 */
data class PersonDraft(
    val accountId: Long,
    val fullName: String,
    val docTypeCode: String, // "DNI"
    val docNumber: String,
    val birthDate: String, // ISO "yyyy-MM-dd"
    val phone: String,
    val ruc: String? // Cliente: puede ser null. Proveedor: se solicitará luego en su flujo
)
