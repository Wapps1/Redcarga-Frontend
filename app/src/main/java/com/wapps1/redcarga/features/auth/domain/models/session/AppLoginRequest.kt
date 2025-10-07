package com.wapps1.redcarga.features.auth.domain.models.session

import com.wapps1.redcarga.features.auth.domain.models.value.Platform

/**
 * Request para login en la app
 */
data class AppLoginRequest(
    val platform: Platform,
    val ip: String
)
