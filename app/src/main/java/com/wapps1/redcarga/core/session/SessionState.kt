package com.wapps1.redcarga.core.session

import com.wapps1.redcarga.features.auth.domain.models.firebase.FirebaseSession
import com.wapps1.redcarga.features.auth.domain.models.session.AppSession

/**
 * Estados de sesión de la aplicación
 * Fuente única de verdad para toda la app
 */
sealed interface SessionState {
    data object SignedOut : SessionState
    data class FirebaseOnly(val fb: FirebaseSession) : SessionState
    data class AppSignedIn(val app: AppSession) : SessionState
}
