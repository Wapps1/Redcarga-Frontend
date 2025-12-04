package com.wapps1.redcarga.features.fleet.domain.models.drivers

import com.wapps1.redcarga.features.auth.domain.models.value.Email

/**
 * Request para iniciar el registro de un conductor (Paso 1)
 */
data class DriverRegistrationStartRequest(
    val email: Email,
    val username: String,
    val password: String,
    val platform: String = "ANDROID", // "ANDROID" | "WEB" | "IOS"
    val idempotencyKey: String? = null // Opcional, máx 64 chars
) {
    fun isValid(): Boolean {
        return username.isNotBlank() && 
               password.length >= 8 &&
               (idempotencyKey?.length ?: 0) <= 64
    }
}

/**
 * Resultado del paso 1: Crear cuenta básica
 */
data class DriverRegistrationStartResult(
    val accountId: Long,
    val signupIntentId: Long,
    val email: String,
    val emailVerified: Boolean,
    val verificationLink: String // Cambiado a String (no nullable) según el backend
)

/**
 * Request para verificar identidad del conductor (Paso 2)
 * Requiere email y password para obtener Firebase token
 */
data class DriverIdentityVerificationRequest(
    val accountId: Long,
    val email: Email, // Necesario para obtener Firebase token
    val password: String, // Necesario para obtener Firebase token
    val fullName: String, // "Juan Pérez García"
    val docTypeCode: String, // "DNI" | "CE" | "PAS"
    val docNumber: String,
    val birthDate: String, // "yyyy-MM-dd"
    val phone: String, // "+51987654321"
    val ruc: String
) {
    fun isValid(): Boolean {
        return fullName.isNotBlank() &&
               docTypeCode in listOf("DNI", "CE", "PAS") &&
               docNumber.isNotBlank() &&
               birthDate.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) &&
               phone.isNotBlank() &&
               ruc.isNotBlank() &&
               password.isNotBlank()
    }
}

/**
 * Resultado del paso 2: Verificar identidad
 */
data class DriverIdentityVerificationResult(
    val passed: Boolean,
    val personId: Long
)

/**
 * Request para asociar conductor a compañía (Paso 3)
 */
data class DriverCompanyAssociationRequest(
    val operatorId: Long, // accountId del conductor
    val roleId: Int = 2 // DRIVER role (típicamente 2)
)

/**
 * Request completo para registrar conductor (todos los datos)
 */
data class DriverFullRegistrationRequest(
    // Paso 1: Cuenta básica
    val email: Email,
    val username: String,
    val password: String,
    val platform: String = "ANDROID",
    val idempotencyKey: String? = null,
    
    // Paso 2: Identidad
    val fullName: String,
    val docTypeCode: String,
    val docNumber: String,
    val birthDate: String,
    val phone: String,
    val ruc: String,
    
    // Paso 4: Conductor
    val licenseNumber: String?,
    val active: Boolean = true
)

/**
 * Resultado completo del registro
 */
data class DriverFullRegistrationResult(
    val accountId: Long,
    val driverId: Long,
    val email: String,
    val emailVerified: Boolean,
    val verificationLink: String?
)

