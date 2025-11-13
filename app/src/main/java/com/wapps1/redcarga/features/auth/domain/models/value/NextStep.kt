package com.wapps1.redcarga.features.auth.domain.models.value

/**
 * Hint de orquestación para que el ViewModel decida la navegación
 * sin necesidad de casos de uso
 */
enum class NextStep {
    AUTO_LOGIN_TRY,
    MANUAL_LOGIN_NEEDED,
    WAIT_EMAIL_VERIFICATION,
    FETCH_FIREBASE_TOKEN,
    NEED_IDENTITY_PROFILE,
    NEED_PROVIDER_COMPANY,
    READY_TO_APP_LOGIN,
    GO_HOME
}
