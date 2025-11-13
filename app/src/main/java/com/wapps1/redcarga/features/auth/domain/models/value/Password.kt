package com.wapps1.redcarga.features.auth.domain.models.value

@JvmInline
value class Password(val value: String) {
    init {
        require(value.isNotBlank()) { "Password cannot be blank" }
        require(value.length >= 8) { "Password must be at least 8 characters" }
        require(value.any { it.isDigit() }) { "Password must contain at least one digit" }
        require(value.any { it.isLetter() }) { "Password must contain at least one letter" }
    }
    
    override fun toString(): String = value
}
