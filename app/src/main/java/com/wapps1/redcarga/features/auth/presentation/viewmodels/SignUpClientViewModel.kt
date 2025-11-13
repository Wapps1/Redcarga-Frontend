package com.wapps1.redcarga.features.auth.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
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
import com.wapps1.redcarga.features.auth.domain.models.value.Email
import com.wapps1.redcarga.features.auth.domain.models.value.Password
import com.wapps1.redcarga.features.auth.domain.models.value.Platform
import com.wapps1.redcarga.features.auth.domain.models.value.RoleCode
import com.wapps1.redcarga.features.auth.domain.models.value.Username
import com.wapps1.redcarga.features.auth.domain.repositories.AuthRemoteRepository
import com.wapps1.redcarga.features.auth.domain.repositories.FirebaseAuthRepository
import com.wapps1.redcarga.features.auth.domain.repositories.IdentityRemoteRepository
import javax.inject.Inject

@HiltViewModel
class SignUpClientViewModel @Inject constructor(
    private val authRemote: AuthRemoteRepository,
    private val identityRemote: IdentityRemoteRepository,
    private val firebaseAuth: FirebaseAuthRepository,
    private val store: AuthSessionStore
) : ViewModel() {

    private companion object { const val TAG = "SignUpClientVM" }

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
        val fullName: String = "",
        val phone: String = "",
        val birthDate: String = "",
        val documentType: String = "",
        val documentNumber: String = "",
        val ruc: String = "",
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
                        roleCode = RoleCode.CLIENT,
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
                .replace("http://localhost:8080", "https://redcargabk-b4b7cng3ftb2bfea.canadacentral-01.azurewebsites.net")
                .replace("https://localhost:8080", "https://redcargabk-b4b7cng3ftb2bfea.canadacentral-01.azurewebsites.net")
            if (link.isNotBlank()) viewModelScope.launch { _effect.emit(Effect.OpenUrl(link)) }
    }

    fun onConfirmEmailVerified() {
        _ui.update { it.copy(emailVerified = true, step = 3) }
    }

    fun onCreatePersonAndLogin(platform: Platform = Platform.ANDROID, ip: String = "0.0.0.0") {
        val s = _ui.value
        val accId = s.accountId ?: return
        viewModelScope.launch {
            _ui.update { it.copy(loading = true, error = null) }
            runCatching {
                Log.d(TAG, "Step3: inicio - email=${s.email}, accountId=$accId")
                Log.d(TAG, "Step3: Firebase signInWithPassword")
                val fb = firebaseAuth.signInWithPassword(Email(s.email), s.password)  // ✅ String directo
                Log.d(TAG, "Step3: Firebase OK - idToken.len=${fb.idToken.length}")
                store.setFirebaseSession(fb)
                // Formatear fecha a yyyy-MM-dd si viene como dd/MM/yyyy
                val normalizedBirthDate = when {
                    s.birthDate.matches(Regex("\\d{2}/\\d{2}/\\d{4}")) -> {
                        val (dd, mm, yyyy) = s.birthDate.split('/')
                        "$yyyy-$mm-$dd"
                    }
                    else -> s.birthDate
                }
                Log.d(TAG, "Step3: normalizedBirthDate=$normalizedBirthDate")

                Log.d(TAG, "Step3: Identity verifyAndCreatePerson")
                val personRes = identityRemote.verifyAndCreatePerson(
                    PersonCreateRequest(
                        accountId = accId,
                        fullName = s.fullName,
                        docTypeCode = s.documentType,
                        docNumber = s.documentNumber,
                        birthDate = normalizedBirthDate,
                        phone = s.phone,
                        ruc = s.ruc
                    )
                )
                Log.d(TAG, "Step3: Identity OK - personId=${'$'}{personRes.personId}")

                Log.d(TAG, "Step3: App login")
                store.tryAppLogin(platform, ip)
                Log.d(TAG, "Step3: App login OK - navegar a Home")
                _effect.emit(Effect.NavigateToMain)
            }.onFailure { e ->
                Log.e(TAG, "Step3: fallo", e)
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


