package com.wapps1.redcarga.features.auth.domain.models.iam

import com.wapps1.redcarga.features.auth.domain.models.value.*

data class RegistrationRequest(
    val email: Email,
    val username: Username,
    val password: Password,
    val roleCode: RoleCode,
    val platform: Platform
)
