package com.wapps1.redcarga.features.fleet.domain

/**
 * Errores de dominio normalizados para el módulo Fleet
 * La capa data mapeará Retrofit/HTTP exceptions → DomainError
 */
sealed interface DomainError {
    val message: String

    data object Network : DomainError {
        override val message: String = "Sin conexión a internet"
    }

    data class Http(val code: Int, val body: String?) : DomainError {
        override val message: String = body ?: "Error HTTP $code"
    }

    data object Unauthorized : DomainError {
        override val message: String = "No autorizado"
    }

    data object Forbidden : DomainError {
        override val message: String = "Acceso prohibido"
    }

    data class InvalidData(val reason: String) : DomainError {
        override val message: String = reason
    }

    data object NotFound : DomainError {
        override val message: String = "Recurso no encontrado"
    }

    data object Unknown : DomainError {
        override val message: String = "Error desconocido"
    }
}

