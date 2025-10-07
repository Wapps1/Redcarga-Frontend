package com.wapps1.redcarga.features.auth.domain.models.value

@JvmInline
value class Username(val value: String) {
    init {
        require(value.isNotBlank()) { "Username cannot be blank" }
        require(value.length >= 3) { "Username must be at least 3 characters" }
        require(value.length <= 50) { "Username must be at most 50 characters" }
    }
    
    override fun toString(): String = value
}
