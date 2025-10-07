package com.wapps1.redcarga.features.auth.domain.models.iam

import com.wapps1.redcarga.features.auth.domain.models.value.*

/**
 * Snapshot de intent de registro para persistencia local (Room)
 * Solo datos necesarios para continuidad de UI
 */
data class SignupIntentSnapshot(
    val signupIntentId: Long,
    val accountId: Long,
    val status: SignupStatus,
    val expiresAt: Long?,
    val lastStepAt: Long?,
    val verificationSentCount: Int?,
    val lastVerificationSentAt: Long?,
    val platform: Platform
)
