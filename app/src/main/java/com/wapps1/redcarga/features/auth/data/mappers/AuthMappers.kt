package com.wapps1.redcarga.features.auth.data.mappers

import com.wapps1.redcarga.features.auth.data.remote.models.*
import com.wapps1.redcarga.features.auth.data.local.entities.*
import com.wapps1.redcarga.features.auth.domain.models.*
import com.wapps1.redcarga.features.auth.domain.models.firebase.FirebaseSession
import com.wapps1.redcarga.features.auth.domain.models.iam.AccountSnapshot
import com.wapps1.redcarga.features.auth.domain.models.iam.RegistrationRequest
import com.wapps1.redcarga.features.auth.domain.models.iam.RegistrationStartResult
import com.wapps1.redcarga.features.auth.domain.models.iam.SignupIntentSnapshot
import com.wapps1.redcarga.features.auth.domain.models.identity.PersonCreateRequest
import com.wapps1.redcarga.features.auth.domain.models.identity.PersonCreateResult
import com.wapps1.redcarga.features.auth.domain.models.identity.PersonDraft
import com.wapps1.redcarga.features.auth.domain.models.provider.CompanyRegisterRequest
import com.wapps1.redcarga.features.auth.domain.models.provider.CompanyRegisterResult
import com.wapps1.redcarga.features.auth.domain.models.session.AppLoginRequest
import com.wapps1.redcarga.features.auth.domain.models.session.AppSession
import com.wapps1.redcarga.features.auth.domain.models.value.*

// DTO → Domain
fun RegisterStartResponseDto.toDomain() = RegistrationStartResult(
    accountId = accountId,
    signupIntentId = signupIntentId,
    email = Email(email),
    emailVerified = emailVerified,
    verificationLink = verificationLink
)

// Helper para mapear roles
private fun String.toRoleCodeOrNull(): RoleCode? = when (this) {
    "CLIENT" -> RoleCode.CLIENT
    "PROVIDER" -> RoleCode.PROVIDER
    else -> null
}

fun AppLoginResponseDto.toDomainSession(nowMs: Long) = AppSession(
    sessionId = sessionId,
    accountId = accountId,
    accessToken = accessToken,
    expiresAt = expiresAt ?: (nowMs + expiresIn * 1000),
    tokenType = TokenType.BEARER, // tu enum solo tiene BEARER
    status = when (status) {
        "ACTIVE" -> SessionStatus.ACTIVE
        "REVOKED" -> SessionStatus.REVOKED
        "EXPIRED" -> SessionStatus.EXPIRED
        else -> SessionStatus.ACTIVE
    },
    roles = (roles ?: emptyList()).mapNotNull { it.toRoleCodeOrNull() }
)

fun AppLoginResponseDto.toAccountSnapshotDomain(): AccountSnapshot? {
    val acc = account ?: return null
    val firstRole = roles?.firstOrNull()?.toRoleCodeOrNull() ?: RoleCode.CLIENT
    return AccountSnapshot(
        accountId = accountId,
        email = Email(acc.email),
        username = Username(acc.username),
        emailVerified = acc.emailVerified,
        status = "ACTIVE",          // no acoplar enum aquí
        roleCode = firstRole,
        createdAt = null,
        updatedAt = acc.updatedAt
    )
}

fun FirebaseSignInResponseDto.toDomain(nowMs: Long) = FirebaseSession(
    localId = localId,
    email = Email(email),
    idToken = idToken,
    refreshToken = refreshToken,
    expiresAt = nowMs + (expiresIn.toLong() * 1000)
)

fun PersonCreateResponseDto.toDomain() = PersonCreateResult(
    passed = passed,
    personId = personId
)

fun CompanyRegisterResponseDto.toDomain() = CompanyRegisterResult(
    success = success,
    companyId = companyId
)

// Domain → DTO
fun RegistrationRequest.toDto() = RegisterStartRequestDto(
    email = email.value,
    username = username.value,
    password = password.value,
    roleCode = roleCode.name,
    platform = platform.name
)

fun AppLoginRequest.toDto() = AppLoginRequestDto(
    platform = platform.name,
    ip = ip
)

fun PersonCreateRequest.toDto() = PersonCreateRequestDto(
    accountId = accountId,
    fullName = fullName,
    docTypeCode = docTypeCode,
    docNumber = docNumber,
    birthDate = birthDate,
    phone = phone,
    ruc = ruc
)

fun CompanyRegisterRequest.toDto() = CompanyRegisterRequestDto(
    accountId = accountId,
    legalName = legalName,
    tradeName = tradeName,
    ruc = ruc,
    email = email.value,
    phone = phone,
    address = address
)

// Entity → Domain
fun AccountSnapshotEntity.toDomain() = AccountSnapshot(
    accountId, Email(email), Username(username), emailVerified, status,
    RoleCode.valueOf(roleCode), createdAt, updatedAt
)

fun SignupIntentEntity.toDomain() = SignupIntentSnapshot(
    signupIntentId, accountId, SignupStatus.valueOf(status),
    expiresAt, lastStepAt, verificationSentCount, lastVerificationSentAt,
    Platform.valueOf(platform)
)

fun PersonDraftEntity.toDomain() = PersonDraft(
    accountId, fullName, docTypeCode, docNumber, birthDate, phone, ruc
)

// Domain → Entity
fun AccountSnapshot.toEntity() = AccountSnapshotEntity(
    accountId, email.value, username.value, emailVerified, status,
    roleCode.name, createdAt, updatedAt
)

fun SignupIntentSnapshot.toEntity() = SignupIntentEntity(
    signupIntentId, accountId, status.name,
    expiresAt, lastStepAt, verificationSentCount, lastVerificationSentAt,
    platform.name
)

fun PersonDraft.toEntity() = PersonDraftEntity(
    accountId, fullName, docTypeCode, docNumber, birthDate, phone, ruc
)
