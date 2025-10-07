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
import com.wapps1.redcarga.features.auth.domain.models.value.Email
import com.wapps1.redcarga.features.auth.domain.models.value.Password
import com.wapps1.redcarga.features.auth.domain.models.value.Platform
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val store: AuthSessionStore
) : ViewModel() {

    data class UiState(
        val email: String = "",
        val password: String = "",
        val loading: Boolean = false,
        val error: String? = null
    )

    sealed interface Effect { data object NavigateToMain : Effect }

    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui

    private val _effect = MutableSharedFlow<Effect>()
    val effect: SharedFlow<Effect> = _effect

    fun updateEmail(v: String) = _ui.update { it.copy(email = v) }
    fun updatePassword(v: String) = _ui.update { it.copy(password = v) }

    fun onSignIn(platform: Platform = Platform.ANDROID, ip: String = "0.0.0.0") {
        val s = _ui.value
        viewModelScope.launch {
            _ui.update { it.copy(loading = true, error = null) }
            runCatching {
                store.signInManually(Email(s.email), Password(s.password), platform, ip)
                _effect.emit(Effect.NavigateToMain)
            }.onFailure { e ->
                _ui.update { it.copy(error = e.message ?: "Error") }
            }
            _ui.update { it.copy(loading = false) }
        }
    }
}


