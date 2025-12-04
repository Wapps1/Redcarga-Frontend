package com.wapps1.redcarga.features.fleet.domain.repositories

import com.wapps1.redcarga.features.fleet.domain.models.common.CompanyId
import com.wapps1.redcarga.features.fleet.domain.models.common.DriverId
import com.wapps1.redcarga.features.fleet.domain.models.drivers.*
import kotlinx.coroutines.flow.Flow

interface FleetDriversRepository {
    fun observeDrivers(companyId: CompanyId): Flow<List<Driver>>

    fun observeDriver(driverId: DriverId): Flow<Driver?>

    suspend fun refreshDrivers(companyId: CompanyId)

    suspend fun getDriver(driverId: DriverId): Driver
    suspend fun createDriver(companyId: CompanyId, body: DriverUpsert): CreateDriverResult
    suspend fun updateDriver(driverId: DriverId, body: DriverUpsert)
    suspend fun deleteDriver(driverId: DriverId)
    
    /**
     * Paso 1: Crear cuenta básica del conductor
     * POST /iam/register-start
     */
    suspend fun registerDriverStart(
        request: DriverRegistrationStartRequest
    ): DriverRegistrationStartResult
    
    /**
     * Paso 2: Verificar identidad del conductor
     * POST /identity/verify-and-create
     * Requiere Firebase token del conductor
     */
    suspend fun verifyDriverIdentity(
        request: DriverIdentityVerificationRequest
    ): DriverIdentityVerificationResult
    
    /**
     * Paso 3: Asociar conductor a compañía con rol DRIVER
     * POST /providers/company/{companyId}/operators
     * Requiere IAM token del admin
     */
    suspend fun associateDriverToCompany(
        companyId: CompanyId,
        request: DriverCompanyAssociationRequest
    )
    
    /**
     * Paso 4: Registrar conductor desde accountId
     * POST /fleet/companies/{companyId}/drivers
     * Requiere IAM token del admin
     */
    suspend fun createDriverFromAccount(
        companyId: CompanyId,
        accountId: Long,
        licenseNumber: String?,
        active: Boolean
    ): CreateDriverResult
    
    /**
     * Flujo completo: Registra un conductor desde cero
     * Orquesta todos los pasos del flujo (1-4)
     * 
     * Nota: El paso de verificación de email es manual (usuario accede al link)
     * Este método espera que el email ya esté verificado antes de continuar
     */
    suspend fun registerDriverComplete(
        companyId: CompanyId,
        request: DriverFullRegistrationRequest
    ): DriverFullRegistrationResult
}


