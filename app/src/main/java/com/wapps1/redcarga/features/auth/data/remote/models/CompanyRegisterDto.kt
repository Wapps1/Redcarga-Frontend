package com.wapps1.redcarga.features.auth.data.remote.models

data class CompanyRegisterRequestDto(
    val accountId: Long,
    val legalName: String,
    val tradeName: String,
    val ruc: String,
    val email: String,
    val phone: String,
    val address: String
)

data class CompanyRegisterResponseDto(
    val success: Boolean,
    val companyId: Long
)
