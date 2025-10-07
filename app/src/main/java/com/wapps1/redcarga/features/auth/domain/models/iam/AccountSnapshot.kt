package com.wapps1.redcarga.features.auth.domain.models.iam

import com.wapps1.redcarga.features.auth.domain.models.value.*

/**
 * Snapshot de cuenta para persistencia local (Room)
 * Solo datos necesarios para continuidad de UI
 */
data class AccountSnapshot(
    val accountId: Long,
    val email: Email,
    val username: Username,
    val emailVerified: Boolean,
    val status: String, // "ACTIVE", etc. (string para no acoplar)
    val roleCode: RoleCode,
    val createdAt: Long?,
    val updatedAt: Long?
)
