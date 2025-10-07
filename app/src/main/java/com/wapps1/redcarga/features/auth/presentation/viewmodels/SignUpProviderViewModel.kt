package com.wapps1.redcarga.features.auth.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.wapps1.redcarga.core.session.AuthSessionStore
import com.wapps1.redcarga.features.auth.domain.models.iam.RegistrationRequest
import com.wapps1.redcarga.features.auth.domain.models.identity.PersonCreateRequest
import com.wapps1.redcarga.features.auth.domain.models.provider.CompanyRegisterRequest
import com.wapps1.redcarga.features.auth.domain.models.value.Email
import com.wapps1.redcarga.features.auth.domain.models.value.Password
import com.wapps1.redcarga.features.auth.domain.models.value.Platform
import com.wapps1.redcarga.features.auth.domain.models.value.RoleCode
import com.wapps1.redcarga.features.auth.domain.models.value.Username
import com.wapps1.redcarga.features.auth.domain.repositories.AuthRemoteRepository
import com.wapps1.redcarga.features.auth.domain.repositories.FirebaseAuthRepository
import com.wapps1.redcarga.features.auth.domain.repositories.IdentityRemoteRepository
import com.wapps1.redcarga.features.auth.domain.repositories.ProviderRemoteRepository
import javax.inject.Inject

@HiltViewModel
class SignUpProviderViewModel @Inject constructor(
    private val authRemote: AuthRemoteRepository,
    private val identityRemote: IdentityRemoteRepository,
    private val providerRemote: ProviderRemoteRepository,
    private val firebaseAuth: FirebaseAuthRepository,
    private val store: AuthSessionStore
) : ViewModel() {

    data class UiState(
        val step: Int = 1,
        val email: String = "",
        val username: String = "",
        val password: String = "",
        val confirmPassword: String = "",
        val verificationLink: String = "",
        val emailVerified: Boolean = false,
        val accountId: Long? = null,
        val signupIntentId: Long? = null,
        // persona
        val fullName: String = "",
        val phone: String = "",
        val birthDate: String = "",
        val documentType: String = "",
        val documentNumber: String = "",
        val ruc: String = "",
        // empresa
        val legalName: String = "",
        val commercialName: String = "",
        val companyRuc: String = "",
        val companyEmail: String = "",
        val companyPhone: String = "",
        val address: String = "",
        val loading: Boolean = false,
        val error: String? = null
    )

    sealed interface Effect {
        data class OpenUrl(val url: String) : Effect
        data object NavigateToMain : Effect
    }

    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui

    private val _effect = MutableSharedFlow<Effect>()
    val effect: SharedFlow<Effect> = _effect

    // setters
    fun updateEmail(v: String) = _ui.update { it.copy(email = v) }
    fun updateUsername(v: String) = _ui.update { it.copy(username = v) }
    fun updatePassword(v: String) = _ui.update { it.copy(password = v) }
    fun updateConfirmPassword(v: String) = _ui.update { it.copy(confirmPassword = v) }

    fun updateFullName(v: String) = _ui.update { it.copy(fullName = v) }
    fun updatePhone(v: String) = _ui.update { it.copy(phone = v) }
    fun updateBirthDate(v: String) = _ui.update { it.copy(birthDate = v) }
    fun updateDocumentType(v: String) = _ui.update { it.copy(documentType = v) }
    fun updateDocumentNumber(v: String) = _ui.update { it.copy(documentNumber = v) }
    fun updateRuc(v: String) = _ui.update { it.copy(ruc = v) }

    fun updateLegalName(v: String) = _ui.update { it.copy(legalName = v) }
    fun updateCommercialName(v: String) = _ui.update { it.copy(commercialName = v) }
    fun updateCompanyRuc(v: String) = _ui.update { it.copy(companyRuc = v) }
    fun updateCompanyEmail(v: String) = _ui.update { it.copy(companyEmail = v) }
    fun updateCompanyPhone(v: String) = _ui.update { it.copy(companyPhone = v) }
    fun updateAddress(v: String) = _ui.update { it.copy(address = v) }

    // actions
    fun onRegisterStart() {
        val s = _ui.value
        viewModelScope.launch {
            _ui.update { it.copy(loading = true, error = null) }
            runCatching {
                val res = authRemote.registerStart(
                    RegistrationRequest(
                        email = Email(s.email),
                        username = Username(s.username),
                        password = Password(s.password),
                        roleCode = RoleCode.PROVIDER,
                        platform = Platform.ANDROID
                    )
                )
                _ui.update {
                    it.copy(
                        verificationLink = res.verificationLink,
                        accountId = res.accountId,
                        signupIntentId = res.signupIntentId,
                        step = 2
                    )
                }
            }.onFailure { e ->
                _ui.update { it.copy(error = e.message ?: "Error") }
            }
            _ui.update { it.copy(loading = false) }
        }
    }

    fun onOpenVerificationLink() {
        val raw = _ui.value.verificationLink
        val link = raw
            .replace("http://localhost:8080", "http://10.0.2.2:8080")
            .replace("https://localhost:8080", "http://10.0.2.2:8080")
        if (link.isNotBlank()) viewModelScope.launch { _effect.emit(Effect.OpenUrl(link)) }
    }

    fun onConfirmEmailVerified() { _ui.update { it.copy(emailVerified = true, step = 3) } }

    fun onVerifyPerson() {
        val s = _ui.value
        val accId = s.accountId ?: return
        viewModelScope.launch {
            _ui.update { it.copy(loading = true, error = null) }
            runCatching {
                val fb = firebaseAuth.signInWithPassword(Email(s.email), Password(s.password))
                store.setFirebaseSession(fb)

                identityRemote.verifyAndCreatePerson(
                    PersonCreateRequest(
                        accountId = accId,
                        fullName = s.fullName,
                        docTypeCode = s.documentType,
                        docNumber = s.documentNumber,
                        birthDate = s.birthDate,
                        phone = s.phone,
                        ruc = s.ruc
                    )
                )

                _ui.update { it.copy(step = 4) }
            }.onFailure { e ->
                _ui.update { it.copy(error = e.message ?: "Error") }
            }
            _ui.update { it.copy(loading = false) }
        }
    }

    fun onRegisterCompanyAndLogin(platform: Platform = Platform.ANDROID, ip: String = "0.0.0.0") {
        val s = _ui.value
        val accId = s.accountId ?: return
        viewModelScope.launch {
            _ui.update { it.copy(loading = true, error = null) }
            runCatching {
                providerRemote.registerCompany(
                    CompanyRegisterRequest(
                        accountId = accId,
                        legalName = s.legalName,
                        tradeName = s.commercialName,
                        ruc = s.companyRuc,
                        email = Email(s.companyEmail),
                        phone = s.companyPhone,
                        address = s.address
                    )
                )

                store.tryAppLogin(platform, ip)
                _effect.emit(Effect.NavigateToMain)
            }.onFailure { e ->
                _ui.update { it.copy(error = e.message ?: "Error") }
            }
            _ui.update { it.copy(loading = false) }
        }
    }

    fun onBack() {
        val current = _ui.value.step
        if (current > 1) _ui.update { it.copy(step = current - 1) }
    }
}


