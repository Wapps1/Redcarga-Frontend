package com.wapps1.redcarga.features.fleet.data.repositories

import android.util.Log
import com.wapps1.redcarga.features.auth.data.remote.services.AuthService
import com.wapps1.redcarga.features.auth.data.remote.services.IdentityService
import com.wapps1.redcarga.features.auth.domain.models.value.Email
import com.wapps1.redcarga.features.auth.domain.repositories.FirebaseAuthRepository
import com.wapps1.redcarga.features.auth.domain.repositories.SecureTokenRepository
import com.wapps1.redcarga.features.fleet.data.network.toFleetDomainError
import com.wapps1.redcarga.features.fleet.data.local.dao.DriversDao
import com.wapps1.redcarga.features.fleet.data.local.entities.DriverEntity
import com.wapps1.redcarga.features.fleet.data.mappers.*
import com.wapps1.redcarga.features.fleet.data.remote.models.CreateDriverFromAccountDto
import com.wapps1.redcarga.features.fleet.data.remote.services.FleetDriversService
import com.wapps1.redcarga.features.fleet.data.remote.services.ProvidersService
import com.wapps1.redcarga.features.fleet.domain.DomainException
import com.wapps1.redcarga.features.fleet.domain.models.common.CompanyId
import com.wapps1.redcarga.features.fleet.domain.models.common.DriverId
import com.wapps1.redcarga.features.fleet.domain.models.drivers.*
import com.wapps1.redcarga.features.fleet.domain.repositories.FleetDriversRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val TAG = "FleetDriversRepo"

class FleetDriversRepositoryImpl @Inject constructor(
    private val service: FleetDriversService,
    private val dao: DriversDao,
    private val authService: AuthService,
    private val identityService: IdentityService,
    private val providersService: ProvidersService,
    private val firebaseAuthRepository: FirebaseAuthRepository,
    private val secureTokenRepository: SecureTokenRepository
) : FleetDriversRepository {

    override fun observeDrivers(companyId: CompanyId): Flow<List<Driver>> =
        dao.observeDrivers(companyId.value).map { list -> list.map { it.toDomain() } }

    override fun observeDriver(driverId: DriverId): Flow<Driver?> =
        dao.observeDriver(driverId.value).map { it?.toDomain() }

    override suspend fun refreshDrivers(companyId: CompanyId) = withContext(Dispatchers.IO) {
        runCatching {
            val remote = service.listDrivers(companyId.value).map { it.toEntity() }
            dao.replaceAllForCompany(companyId.value, remote)
            Unit
        }.getOrElse { e ->
            throw DomainException(e.toFleetDomainError(), e)
        }
    }

    override suspend fun getDriver(driverId: DriverId): Driver = withContext(Dispatchers.IO) {
        runCatching { service.getDriver(driverId.value).toDomain() }
            .getOrElse { e -> throw DomainException(e.toFleetDomainError(), e) }
    }

    override suspend fun createDriver(companyId: CompanyId, body: DriverUpsert): CreateDriverResult =
        withContext(Dispatchers.IO) {
            runCatching {
                val res = service.createDriver(companyId.value, body.toDto())
                val now = System.currentTimeMillis()
                val cached = DriverEntity(
                    driverId = res.driverId,
                    companyId = companyId.value,
                    firstName = body.firstName,
                    lastName = body.lastName,
                    email = body.email.value,
                    phone = body.phone,
                    licenseNumber = body.licenseNumber,
                    active = body.active,
                    createdAt = now,
                    updatedAt = now,
                    dirty = false,
                    deletedLocally = false
                )
                dao.upsert(cached)
                CreateDriverResult(res.driverId)
            }.getOrElse { e ->
                throw DomainException(e.toFleetDomainError(), e)
            }
        }

    override suspend fun updateDriver(driverId: DriverId, body: DriverUpsert) =
        withContext(Dispatchers.IO) {
        runCatching {
                service.updateDriver(driverId.value, body.toDto())
                // Refrescar instantáneamente en Room
                val existing = dao.find(driverId.value)
                if (existing != null) {
                    val now = System.currentTimeMillis()
                    dao.upsert(
                        existing.copy(
                            firstName = body.firstName,
                            lastName = body.lastName,
                            email = body.email.value,
                            phone = body.phone,
                            licenseNumber = body.licenseNumber,
                            active = body.active,
                            updatedAt = now,
                            dirty = false,
                            deletedLocally = false
                        )
                    )
                } else {
                    dao.markAsSynced(driverId.value)
                }
            Unit
        }.getOrElse { e ->
            throw DomainException(e.toFleetDomainError(), e)
        }
        }

    override suspend fun deleteDriver(driverId: DriverId) = withContext(Dispatchers.IO) {
        runCatching {
            service.deleteDriver(driverId.value)
            dao.deleteById(driverId.value)
            Unit
        }.getOrElse { e ->
            throw DomainException(e.toFleetDomainError(), e)
        }
    }
    
    // ========== MÉTODOS PARA REGISTRO DE CONDUCTOR ==========
    
    override suspend fun registerDriverStart(
        request: DriverRegistrationStartRequest
    ): DriverRegistrationStartResult = withContext(Dispatchers.IO) {
        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "📝 [PASO 1] Registrando cuenta básica del conductor")
        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "   Email: ${request.email.value}")
        Log.d(TAG, "   Username: ${request.username}")
        Log.d(TAG, "   Platform: ${request.platform}")
        
        runCatching {
            if (!request.isValid()) {
                Log.e(TAG, "❌ Request inválido")
                throw DomainException(
                    com.wapps1.redcarga.features.fleet.domain.DomainError.InvalidData("Datos inválidos"),
                    null
                )
            }
            
            // ✅ CORRECCIÓN: Usar RegisterStartRequestDto del módulo auth
            val dto = request.toRegisterStartRequestDto()
            Log.d(TAG, "   Llamando a POST /iam/register-start...")
            val response = authService.registerStart(dto)
            Log.d(TAG, "   ✅ Respuesta recibida: accountId=${response.accountId}")
            
            // ✅ CORRECCIÓN: Mapear RegisterStartResponseDto a DriverRegistrationStartResult
            val result = response.toDriverRegistrationStartResult()
            Log.d(TAG, "   Estado: ${if (result.emailVerified) "EMAIL_VERIFIED" else "PENDING_EMAIL_VERIFICATION"}")
            Log.d(TAG, "═══════════════════════════════════════")
            result
        }.getOrElse { e ->
            Log.e(TAG, "❌ Error al registrar cuenta básica: ${e.message}", e)
            throw DomainException(e.toFleetDomainError(), e)
        }
    }
    
    override suspend fun verifyDriverIdentity(
        request: DriverIdentityVerificationRequest
    ): DriverIdentityVerificationResult = withContext(Dispatchers.IO) {
        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "🆔 [PASO 2] Verificando identidad del conductor")
        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "   AccountId: ${request.accountId}")
        Log.d(TAG, "   Email: ${request.email.value}")
        Log.d(TAG, "   FullName: ${request.fullName}")
        Log.d(TAG, "   DocType: ${request.docTypeCode}")
        Log.d(TAG, "   DocNumber: ${request.docNumber}")
        
        runCatching {
            if (!request.isValid()) {
                Log.e(TAG, "❌ Request inválido")
                throw DomainException(
                    com.wapps1.redcarga.features.fleet.domain.DomainError.InvalidData("Datos inválidos"),
                    null
                )
            }
            
            // ✅ SOLUCIÓN: Guardar el token del admin temporalmente
            val adminFirebaseSession = secureTokenRepository.getFirebaseSession()
            Log.d(TAG, "   Guardando token del admin temporalmente...")
            if (adminFirebaseSession != null) {
                Log.d(TAG, "   Admin email: ${adminFirebaseSession.email.value}")
            } else {
                Log.w(TAG, "   ⚠️ No hay token del admin guardado")
            }
            
            // Obtener Firebase token del conductor
            Log.d(TAG, "   Obteniendo Firebase token del conductor...")
            val driverFirebaseSession = firebaseAuthRepository.signInWithPassword(
                request.email,
                request.password
            )
            Log.d(TAG, "   ✅ Firebase token del conductor obtenido")
            Log.d(TAG, "   Conductor email: ${driverFirebaseSession.email.value}")
            Log.d(TAG, "   Conductor localId: ${driverFirebaseSession.localId}")
            
            // ✅ SOLUCIÓN: Guardar temporalmente el token del conductor
            Log.d(TAG, "   Guardando token del conductor en SecureTokenRepository...")
            secureTokenRepository.saveFirebaseSession(driverFirebaseSession)
            Log.d(TAG, "   ✅ Token del conductor guardado")
            
            try {
                // ✅ CORRECCIÓN: Usar PersonCreateRequestDto del módulo auth
                val dto = request.toPersonCreateRequestDto()
                Log.d(TAG, "   Llamando a POST /identity/verify-and-create...")
                Log.d(TAG, "   ⚠️ Usando Firebase token del conductor (guardado temporalmente)")
                val response = identityService.verifyAndCreate(dto)
                Log.d(TAG, "   ✅ Respuesta recibida: passed=${response.passed}, personId=${response.personId}")
                Log.d(TAG, "   ⚠️ Estado actualizado a BASIC_PROFILE_COMPLETED (asíncrono)")
                Log.d(TAG, "═══════════════════════════════════════")
                
                // ✅ CORRECCIÓN: Mapear PersonCreateResponseDto a DriverIdentityVerificationResult
                response.toDriverIdentityVerificationResult()
            } finally {
                // ✅ SOLUCIÓN: Restaurar el token del admin
                if (adminFirebaseSession != null) {
                    Log.d(TAG, "   Restaurando token del admin...")
                    secureTokenRepository.saveFirebaseSession(adminFirebaseSession)
                    Log.d(TAG, "   ✅ Token del admin restaurado")
                } else {
                    Log.w(TAG, "   ⚠️ No había token del admin guardado, limpiando sesión Firebase")
                    secureTokenRepository.clearFirebaseSession()
                }
            }
        }.getOrElse { e ->
            Log.e(TAG, "❌ Error al verificar identidad: ${e.message}", e)
            throw DomainException(e.toFleetDomainError(), e)
        }
    }
    
    override suspend fun associateDriverToCompany(
        companyId: CompanyId,
        request: DriverCompanyAssociationRequest
    ) = withContext(Dispatchers.IO) {
        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "🏢 [PASO 3] Asociando conductor a compañía")
        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "   CompanyId: ${companyId.value}")
        Log.d(TAG, "   OperatorId (accountId): ${request.operatorId}")
        Log.d(TAG, "   RoleId: ${request.roleId} (DRIVER)")
        
        runCatching {
            val dto = request.toDto()
            Log.d(TAG, "   Llamando a POST /providers/company/${companyId.value}/operators...")
            Log.d(TAG, "   ⚠️ Requiere IAM token del admin")
            val response = providersService.associateOperatorToCompany(companyId.value, dto)
            
            if (!response.isSuccessful) {
                Log.e(TAG, "❌ Error HTTP: ${response.code()} - ${response.message()}")
                throw DomainException(
                    com.wapps1.redcarga.features.fleet.domain.DomainError.Http(
                        response.code(),
                        response.message()
                    ),
                    null
                )
            }
            
            Log.d(TAG, "   ✅ Conductor asociado exitosamente a la compañía")
            Log.d(TAG, "═══════════════════════════════════════")
            Unit
        }.getOrElse { e ->
            Log.e(TAG, "❌ Error al asociar conductor: ${e.message}", e)
            throw DomainException(e.toFleetDomainError(), e)
        }
    }
    
    override suspend fun createDriverFromAccount(
        companyId: CompanyId,
        accountId: Long,
        licenseNumber: String?,
        active: Boolean
    ): CreateDriverResult = withContext(Dispatchers.IO) {
        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "🚗 [PASO 4] Registrando conductor en Fleet")
        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "   CompanyId: ${companyId.value}")
        Log.d(TAG, "   AccountId: $accountId")
        Log.d(TAG, "   LicenseNumber: ${licenseNumber ?: "null"}")
        Log.d(TAG, "   Active: $active")
        
        runCatching {
            val dto = CreateDriverFromAccountDto(
                accountId = accountId,
                licenseNumber = licenseNumber,
                active = active
            )
            Log.d(TAG, "   Llamando a POST /fleet/companies/${companyId.value}/drivers...")
            Log.d(TAG, "   ⚠️ Requiere IAM token del admin")
            val response = service.createDriverFromAccount(companyId.value, dto)
            Log.d(TAG, "   ✅ Respuesta recibida: driverId=${response.driverId}")
            Log.d(TAG, "═══════════════════════════════════════")
            
            CreateDriverResult(response.driverId)
        }.getOrElse { e ->
            Log.e(TAG, "❌ Error al registrar conductor: ${e.message}", e)
            throw DomainException(e.toFleetDomainError(), e)
        }
    }
    
    override suspend fun registerDriverComplete(
        companyId: CompanyId,
        request: DriverFullRegistrationRequest
    ): DriverFullRegistrationResult = withContext(Dispatchers.IO) {
        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "🚀 [FLUJO COMPLETO] Registro de conductor")
        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "   CompanyId: ${companyId.value}")
        Log.d(TAG, "   Email: ${request.email.value}")
        Log.d(TAG, "   Username: ${request.username}")
        
        runCatching {
            // Paso 1: Crear cuenta básica
            Log.d(TAG, "")
            Log.d(TAG, "📝 PASO 1: Crear cuenta básica")
            val step1Request = DriverRegistrationStartRequest(
                email = request.email,
                username = request.username,
                password = request.password,
                platform = request.platform,
                idempotencyKey = request.idempotencyKey
            )
            val step1Result = registerDriverStart(step1Request)
            Log.d(TAG, "   ✅ Paso 1 completado: accountId=${step1Result.accountId}")
            Log.d(TAG, "   ⚠️ Estado: ${if (step1Result.emailVerified) "EMAIL_VERIFIED" else "PENDING_EMAIL_VERIFICATION"}")
            Log.d(TAG, "   ⚠️ El usuario debe verificar el email antes de continuar")
            
            // Verificar que el email esté verificado
            if (!step1Result.emailVerified) {
                Log.w(TAG, "   ⚠️ Email no verificado. Retornando resultado parcial...")
                return@withContext DriverFullRegistrationResult(
                    accountId = step1Result.accountId,
                    driverId = 0, // Aún no creado
                    email = step1Result.email,
                    emailVerified = false,
                    verificationLink = step1Result.verificationLink // Ya no es nullable
                )
            }
            
            // Paso 2: Verificar identidad (obtiene Firebase token internamente)
            Log.d(TAG, "")
            Log.d(TAG, "🆔 PASO 2: Verificar identidad")
            val step2Request = DriverIdentityVerificationRequest(
                accountId = step1Result.accountId,
                email = request.email,
                password = request.password,
                fullName = request.fullName,
                docTypeCode = request.docTypeCode,
                docNumber = request.docNumber,
                birthDate = request.birthDate,
                phone = request.phone,
                ruc = request.ruc
            )
            val step2Result = verifyDriverIdentity(step2Request)
            Log.d(TAG, "   ✅ Paso 2 completado: personId=${step2Result.personId}")
            Log.d(TAG, "   ⚠️ Estado actualizado a BASIC_PROFILE_COMPLETED (asíncrono)")
            
            // Esperar un momento para que el estado se actualice asíncronamente
            Log.d(TAG, "   ⏳ Esperando actualización asíncrona del estado...")
            delay(2000) // Esperar 2 segundos para que el estado se actualice
            
            // Paso 3: Asociar a compañía
            Log.d(TAG, "")
            Log.d(TAG, "🏢 PASO 3: Asociar a compañía")
            val step3Request = DriverCompanyAssociationRequest(
                operatorId = step1Result.accountId,
                roleId = 2 // DRIVER
            )
            associateDriverToCompany(companyId, step3Request)
            Log.d(TAG, "   ✅ Paso 3 completado")
            
            // Paso 4: Registrar como conductor
            Log.d(TAG, "")
            Log.d(TAG, "🚗 PASO 4: Registrar como conductor")
            val step4Result = createDriverFromAccount(
                companyId = companyId,
                accountId = step1Result.accountId,
                licenseNumber = request.licenseNumber,
                active = request.active
            )
            Log.d(TAG, "   ✅ Paso 4 completado: driverId=${step4Result.driverId}")
            
            Log.d(TAG, "")
            Log.d(TAG, "═══════════════════════════════════════")
            Log.d(TAG, "✅ FLUJO COMPLETO FINALIZADO EXITOSAMENTE")
            Log.d(TAG, "═══════════════════════════════════════")
            Log.d(TAG, "   AccountId: ${step1Result.accountId}")
            Log.d(TAG, "   DriverId: ${step4Result.driverId}")
            Log.d(TAG, "═══════════════════════════════════════")
            
            DriverFullRegistrationResult(
                accountId = step1Result.accountId,
                driverId = step4Result.driverId,
                email = step1Result.email,
                emailVerified = step1Result.emailVerified,
                verificationLink = step1Result.verificationLink
            )
        }.getOrElse { e ->
            Log.e(TAG, "❌ Error en flujo completo: ${e.message}", e)
            throw DomainException(e.toFleetDomainError(), e)
        }
    }
}


