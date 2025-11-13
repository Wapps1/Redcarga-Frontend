package com.wapps1.redcarga.features.auth.domain.models.iam

import com.wapps1.redcarga.features.auth.domain.models.value.Email

data class RegistrationStartResult(
    val accountId: Long,
    val signupIntentId: Long,
    val email: Email,
    val emailVerified: Boolean,
    val verificationLink: String
)
