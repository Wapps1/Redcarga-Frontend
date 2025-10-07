package com.wapps1.redcarga.features.auth.data.remote.models

data class PersonCreateRequestDto(
    val accountId: Long,
    val fullName: String,
    val docTypeCode: String,
    val docNumber: String,
    val birthDate: String,
    val phone: String,
    val ruc: String?
)

data class PersonCreateResponseDto(
    val passed: Boolean,
    val personId: Long
)
