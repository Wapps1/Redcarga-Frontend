package com.wapps1.redcarga.features.fleet.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wapps1.redcarga.core.session.AuthSessionStore
import com.wapps1.redcarga.features.auth.domain.models.value.Email
import com.wapps1.redcarga.features.fleet.domain.models.common.CompanyId
import com.wapps1.redcarga.features.fleet.domain.models.common.DriverId
import com.wapps1.redcarga.features.fleet.domain.models.drivers.Driver
import com.wapps1.redcarga.features.fleet.domain.models.drivers.DriverCompanyAssociationRequest
import com.wapps1.redcarga.features.fleet.domain.models.drivers.DriverFullRegistrationRequest
import com.wapps1.redcarga.features.fleet.domain.models.drivers.DriverIdentityVerificationRequest
import com.wapps1.redcarga.features.fleet.domain.models.drivers.DriverRegistrationStartRequest
import com.wapps1.redcarga.features.fleet.domain.models.drivers.DriverUpsert
import com.wapps1.redcarga.features.fleet.domain.repositories.FleetDriversRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

private const val TAG = "DriversManagementVM"



@HiltViewModel
class DriversManagementViewModel @Inject constructor(
    private val repo: FleetDriversRepository,
    private val sessionStore: AuthSessionStore
) : ViewModel() {

    data class DriverItemUi(
        val id: Long,
        val firstName: String,
        val lastName: String,
        val email: String,
        val phone: String,
        val licenseNumber: String,
        val active: Boolean
    )

    data class Filters(
        val activeOnly: Boolean? = null,
        val query: String? = null
    )

    data class UiState(
        val isInitializing: Boolean = true,
        val isRefreshing: Boolean = false,
        val isSubmitting: Boolean = false,
        val companyId: Long? = null,
        val all: List<DriverItemUi> = emptyList(),
        val items: List<DriverItemUi> = emptyList(),
        val filters: Filters = Filters(),
        val empty: Boolean = false
    ) {
        val hasActiveFilters: Boolean
            get() = filters.activeOnly != null || !filters.query.isNullOrBlank()

        val activeFiltersCount: Int
            get() = listOfNotNull(
                filters.activeOnly,
                filters.query?.takeIf { it.isNotBlank() }
            ).size
    }

    sealed interface Effect { 
        data class Message(val text: String): Effect
        data class OpenUrl(val url: String): Effect
    }

    // ========== ESTADOS PARA REGISTRO DE CONDUCTOR ==========
    
    sealed interface RegistrationState {
        data object Idle : RegistrationState
        // ✅ NUEVO: Estado para cuando el usuario está llenando el formulario (sin carga)
        data class Step1Form(val form: DriverRegistrationForm) : RegistrationState
        // ✅ Estado para cuando se está procesando la creación (con carga)
        data class Step1Creating(val form: DriverRegistrationForm) : RegistrationState
        data class Step1EmailVerification(
            val accountId: Long,
            val email: String,
            val verificationLink: String,
            val form: DriverRegistrationForm
        ) : RegistrationState
        data class Step2Form(
            val accountId: Long,
            val form: DriverRegistrationForm
        ) : RegistrationState
        data class Step2Verifying(
            val accountId: Long,
            val form: DriverRegistrationForm
        ) : RegistrationState
        data class Step3Associating(
            val accountId: Long,
            val form: DriverRegistrationForm
        ) : RegistrationState
        data class Step4Form(
            val accountId: Long,
            val form: DriverRegistrationForm
        ) : RegistrationState
        data class Step4Creating(
            val accountId: Long,
            val form: DriverRegistrationForm
        ) : RegistrationState
        data class Success(val driverId: Long) : RegistrationState
        data class Error(val message: String) : RegistrationState
    }

    data class DriverRegistrationForm(
        // Paso 1: Cuenta básica
        val email: String = "",
        val username: String = "",
        val password: String = "",
        // Paso 2: Identidad
        val fullName: String = "",
        val docTypeCode: String = "DNI", // "DNI" | "CE" | "PAS"
        val docNumber: String = "",
        val birthDate: String = "", // "yyyy-MM-dd"
        val phone: String = "",
        val ruc: String = "",
        // Paso 4: Conductor
        val licenseNumber: String = "",
        val active: Boolean = true
    ) {
        fun isStep1Valid(): Boolean {
            return email.isNotBlank() &&
                   android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() &&
                   username.isNotBlank() &&
                   password.length >= 8
        }
        
        fun isStep2Valid(): Boolean {
            return fullName.isNotBlank() &&
                   docTypeCode in listOf("DNI", "CE", "PAS") &&
                   docNumber.isNotBlank() &&
                   birthDate.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) &&
                   phone.isNotBlank() &&
                   ruc.isNotBlank()
        }
        
        fun isStep4Valid(): Boolean {
            return true // licenseNumber es opcional
        }
    }

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()
    
    private val _registrationState = MutableStateFlow<RegistrationState>(RegistrationState.Idle)
    val registrationState: StateFlow<RegistrationState> = _registrationState.asStateFlow()
    
    private val _effects = Channel<Effect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun bootstrap() {
        if (!_state.value.isInitializing) return
        viewModelScope.launch {
            sessionStore.currentCompanyId.collectLatest { cid ->
                if (cid == null) {
                    _state.value = _state.value.copy(isInitializing = false, companyId = null, items = emptyList(), empty = true)
                    return@collectLatest
                }
                _state.value = _state.value.copy(companyId = cid)
                repo.observeDrivers(CompanyId(cid)).collectLatest { list ->
                    val ui = list.map { it.toUi() }
                    val filtered = applyFilters(ui, _state.value.filters)
                    _state.value = _state.value.copy(isInitializing = false, all = ui, items = filtered, empty = filtered.isEmpty())
                }
            }
        }
    }

    private fun Driver.toUi() = DriverItemUi(
        id = driverId.value,
        firstName = firstName,
        lastName = lastName,
        email = email.value,
        phone = phone,
        licenseNumber = licenseNumber,
        active = active
    )

    private fun applyFilters(items: List<DriverItemUi>, f: Filters): List<DriverItemUi> {
        val q = f.query?.trim()?.lowercase().orEmpty()
        return items.asSequence()
            .filter { f.activeOnly == null || it.active == f.activeOnly }
            .filter {
                if (q.isEmpty()) true else
                    it.firstName.lowercase().contains(q) ||
                            it.lastName.lowercase().contains(q) ||
                            it.email.lowercase().contains(q) ||
                            it.phone.lowercase().contains(q) ||
                            it.licenseNumber.lowercase().contains(q)
            }
            .toList()
    }

    fun onFiltersChanged(filters: Filters) {
        val filtered = applyFilters(_state.value.all, filters)
        _state.value = _state.value.copy(filters = filters, items = filtered, empty = filtered.isEmpty())
    }

    fun onRefresh() {
        val cid = _state.value.companyId ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(isRefreshing = true)
            runCatching { repo.refreshDrivers(CompanyId(cid)) }
                .onFailure { _effects.send(Effect.Message(it.message ?: "Error al actualizar")) }
            _state.value = _state.value.copy(isRefreshing = false)
        }
    }

    fun onCreate(firstName: String, lastName: String, email: String, phone: String, license: String, active: Boolean) {
        val cid = _state.value.companyId ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(isSubmitting = true)
            runCatching { repo.createDriver(CompanyId(cid), DriverUpsert(firstName, lastName, com.wapps1.redcarga.features.auth.domain.models.value.Email(email), phone, license, active)) }
                .onSuccess { _effects.send(Effect.Message("Conductor creado")) }
                .onFailure { _effects.send(Effect.Message(it.message ?: "Error al crear conductor")) }
            _state.value = _state.value.copy(isSubmitting = false)
        }
    }

    fun onUpdate(driverId: Long, firstName: String, lastName: String, email: String, phone: String, license: String, active: Boolean) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSubmitting = true)
            runCatching { repo.updateDriver(DriverId(driverId), DriverUpsert(firstName, lastName, com.wapps1.redcarga.features.auth.domain.models.value.Email(email), phone, license, active)) }
                .onSuccess { _effects.send(Effect.Message("Conductor actualizado")) }
                .onFailure { _effects.send(Effect.Message(it.message ?: "Error al actualizar conductor")) }
            _state.value = _state.value.copy(isSubmitting = false)
        }
    }

    fun onDelete(driverId: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSubmitting = true)
            runCatching { repo.deleteDriver(DriverId(driverId)) }
                .onSuccess { _effects.send(Effect.Message("Conductor eliminado")) }
                .onFailure { _effects.send(Effect.Message(it.message ?: "Error al eliminar conductor")) }
            _state.value = _state.value.copy(isSubmitting = false)
        }
    }
    
    // ========== MÉTODOS PARA REGISTRO DE CONDUCTOR ==========
    
    fun startDriverRegistration() {
        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "🚀 [UI] Iniciando registro de conductor")
        Log.d(TAG, "═══════════════════════════════════════")
        _registrationState.value = RegistrationState.Step1Form(DriverRegistrationForm())
        Log.d(TAG, "✅ Estado cambiado a Step1Form (formulario vacío, sin carga)")
    }
    
    fun cancelDriverRegistration() {
        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "❌ [UI] Cancelando registro de conductor")
        Log.d(TAG, "═══════════════════════════════════════")
        _registrationState.value = RegistrationState.Idle
        Log.d(TAG, "✅ Estado cambiado a Idle")
    }
    
    fun onRegistrationStep1Next(form: DriverRegistrationForm) {
        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "📝 [PASO 1] Usuario presionó 'Continuar'")
        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "   Email: ${form.email}")
        Log.d(TAG, "   Username: ${form.username}")
        Log.d(TAG, "   Password length: ${form.password.length}")
        Log.d(TAG, "   Form válido: ${form.isStep1Valid()}")
        
        val companyId = _state.value.companyId ?: run {
            Log.e(TAG, "❌ No hay compañía seleccionada")
            _registrationState.value = RegistrationState.Error("No hay compañía seleccionada")
            return
        }
        
        Log.d(TAG, "   CompanyId: $companyId")
        
        viewModelScope.launch {
            Log.d(TAG, "   Cambiando estado a Step1Creating (procesando)...")
            _registrationState.value = RegistrationState.Step1Creating(form)
            
            runCatching {
                Log.d(TAG, "   Creando DriverRegistrationStartRequest...")
                val request = DriverRegistrationStartRequest(
                    email = Email(form.email),
                    username = form.username,
                    password = form.password,
                    platform = "ANDROID"
                )
                
                Log.d(TAG, "   Llamando a repo.registerDriverStart()...")
                val result = repo.registerDriverStart(request)
                Log.d(TAG, "   ✅ Respuesta recibida:")
                Log.d(TAG, "      accountId: ${result.accountId}")
                Log.d(TAG, "      email: ${result.email}")
                Log.d(TAG, "      emailVerified: ${result.emailVerified}")
                Log.d(TAG, "      verificationLink: ${result.verificationLink.take(50)}...")
                
                if (!result.emailVerified) {
                    Log.d(TAG, "   Email NO verificado, mostrando pantalla de verificación")
                    _registrationState.value = RegistrationState.Step1EmailVerification(
                        accountId = result.accountId,
                        email = result.email,
                        verificationLink = result.verificationLink,
                        form = form
                    )
                } else {
                    Log.d(TAG, "   Email YA verificado, continuando directamente al paso 2")
                    // Si el email ya está verificado, continuar directamente al paso 2
                    continueAfterEmailVerification(result.accountId, form)
                }
            }.onFailure { e ->
                Log.e(TAG, "❌ Error al crear cuenta básica: ${e.message}", e)
                _registrationState.value = RegistrationState.Error(
                    e.message ?: "Error al crear cuenta básica"
                )
            }
        }
    }
    
    fun onOpenVerificationLink(verificationLink: String) {
        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "🔗 [UI] Usuario presionó 'Abrir Enlace de Verificación'")
        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "   Link original: ${verificationLink.take(100)}...")
        viewModelScope.launch {
            // Reemplazar localhost por el backend real si es necesario
            val link = verificationLink
                .replace("http://localhost:8080", "https://redcargabk-b4b7cng3ftb2bfea.canadacentral-01.azurewebsites.net")
                .replace("https://localhost:8080", "https://redcargabk-b4b7cng3ftb2bfea.canadacentral-01.azurewebsites.net")
            Log.d(TAG, "   Link procesado: ${link.take(100)}...")
            Log.d(TAG, "   Enviando efecto OpenUrl...")
            _effects.send(Effect.OpenUrl(link))
            Log.d(TAG, "   ✅ Efecto enviado")
        }
    }
    
    fun continueAfterEmailVerification(accountId: Long, form: DriverRegistrationForm) {
        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "✅ [UI] Continuando después de verificación de email")
        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "   AccountId: $accountId")
        // Mostrar formulario del paso 2
        _registrationState.value = RegistrationState.Step2Form(accountId, form)
        Log.d(TAG, "   Estado cambiado a Step2Form")
    }
    
    fun onRegistrationStep2Next(form: DriverRegistrationForm) {
        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "🆔 [PASO 2] Usuario presionó 'Continuar'")
        Log.d(TAG, "═══════════════════════════════════════")
        
        val currentState = _registrationState.value
        val accountId = when (currentState) {
            is RegistrationState.Step1EmailVerification -> {
                Log.d(TAG, "   AccountId desde Step1EmailVerification: ${currentState.accountId}")
                currentState.accountId
            }
            is RegistrationState.Step2Form -> {
                Log.d(TAG, "   AccountId desde Step2Form: ${currentState.accountId}")
                currentState.accountId
            }
            else -> {
                Log.e(TAG, "❌ Estado inválido: $currentState")
                return
            }
        }
        
        Log.d(TAG, "   FullName: ${form.fullName}")
        Log.d(TAG, "   DocType: ${form.docTypeCode}")
        Log.d(TAG, "   DocNumber: ${form.docNumber}")
        Log.d(TAG, "   BirthDate: ${form.birthDate}")
        Log.d(TAG, "   Phone: ${form.phone}")
        Log.d(TAG, "   RUC: ${form.ruc}")
        Log.d(TAG, "   Form válido: ${form.isStep2Valid()}")
        
        viewModelScope.launch {
            Log.d(TAG, "   Cambiando estado a Step2Verifying (procesando)...")
            _registrationState.value = RegistrationState.Step2Verifying(accountId, form)
            
            runCatching {
                Log.d(TAG, "   Creando DriverIdentityVerificationRequest...")
                val request = DriverIdentityVerificationRequest(
                    accountId = accountId,
                    email = Email(form.email),
                    password = form.password,
                    fullName = form.fullName,
                    docTypeCode = form.docTypeCode,
                    docNumber = form.docNumber,
                    birthDate = form.birthDate,
                    phone = form.phone,
                    ruc = form.ruc
                )
                
                Log.d(TAG, "   Llamando a repo.verifyDriverIdentity()...")
                val result = repo.verifyDriverIdentity(request)
                Log.d(TAG, "   ✅ Respuesta recibida:")
                Log.d(TAG, "      passed: ${result.passed}")
                Log.d(TAG, "      personId: ${result.personId}")
                
                // Esperar 2 segundos para que el estado se actualice asíncronamente
                Log.d(TAG, "   ⏳ Esperando 2 segundos para actualización asíncrona del estado...")
                delay(2000)
                Log.d(TAG, "   ✅ Espera completada")
                
                // Paso 3: Asociar a compañía
                val companyId = _state.value.companyId ?: throw IllegalStateException("No hay compañía seleccionada")
                Log.d(TAG, "═══════════════════════════════════════")
                Log.d(TAG, "🏢 [PASO 3] Asociando conductor a compañía")
                Log.d(TAG, "═══════════════════════════════════════")
                Log.d(TAG, "   CompanyId: $companyId")
                Log.d(TAG, "   Cambiando estado a Step3Associating (procesando)...")
                _registrationState.value = RegistrationState.Step3Associating(accountId, form)
                
                val associationRequest = DriverCompanyAssociationRequest(
                    operatorId = accountId,
                    roleId = 2 // DRIVER
                )
                
                Log.d(TAG, "   Llamando a repo.associateDriverToCompany()...")
                repo.associateDriverToCompany(CompanyId(companyId), associationRequest)
                Log.d(TAG, "   ✅ Conductor asociado exitosamente")
                
                // Paso 4: Mostrar formulario de licencia
                Log.d(TAG, "   Cambiando estado a Step4Form...")
                _registrationState.value = RegistrationState.Step4Form(accountId, form)
                
            }.onFailure { e ->
                Log.e(TAG, "❌ Error en el proceso de registro: ${e.message}", e)
                _registrationState.value = RegistrationState.Error(
                    e.message ?: "Error en el proceso de registro"
                )
            }
        }
    }
    
    fun onRegistrationStep4Next(form: DriverRegistrationForm) {
        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "🚗 [PASO 4] Usuario presionó 'Finalizar Registro'")
        Log.d(TAG, "═══════════════════════════════════════")
        
        val currentState = _registrationState.value
        val accountId = when (currentState) {
            is RegistrationState.Step4Form -> {
                Log.d(TAG, "   AccountId desde Step4Form: ${currentState.accountId}")
                currentState.accountId
            }
            else -> {
                Log.e(TAG, "❌ Estado inválido: $currentState")
                return
            }
        }
        
        Log.d(TAG, "   LicenseNumber: ${form.licenseNumber}")
        Log.d(TAG, "   Active: ${form.active}")
        
        viewModelScope.launch {
            Log.d(TAG, "   Cambiando estado a Step4Creating (procesando)...")
            _registrationState.value = RegistrationState.Step4Creating(accountId, form)
            
            runCatching {
                val companyId = _state.value.companyId ?: throw IllegalStateException("No hay compañía seleccionada")
                Log.d(TAG, "   CompanyId: $companyId")
                
                Log.d(TAG, "   Llamando a repo.createDriverFromAccount()...")
                val createResult = repo.createDriverFromAccount(
                    companyId = CompanyId(companyId),
                    accountId = accountId,
                    licenseNumber = form.licenseNumber.takeIf { it.isNotBlank() },
                    active = form.active
                )
                
                Log.d(TAG, "   ✅ Respuesta recibida:")
                Log.d(TAG, "      driverId: ${createResult.driverId}")
                
                Log.d(TAG, "   Cambiando estado a Success...")
                _registrationState.value = RegistrationState.Success(createResult.driverId)
                _effects.send(Effect.Message("Conductor registrado exitosamente"))
                
                // Refrescar la lista de conductores
                Log.d(TAG, "   Refrescando lista de conductores...")
                repo.refreshDrivers(CompanyId(companyId))
                Log.d(TAG, "   ✅ Lista refrescada")
                
            }.onFailure { e ->
                Log.e(TAG, "❌ Error al registrar conductor: ${e.message}", e)
                _registrationState.value = RegistrationState.Error(
                    e.message ?: "Error al registrar conductor"
                )
            }
        }
    }
}


