package com.wapps1.redcarga.features.auth.domain.models.value

@JvmInline
value class Email(val value: String) {
    init {
        require(value.isNotBlank()) { "Email cannot be blank" }
        require(value.contains("@")) { "Email must contain @ symbol" }
    }
    
    override fun toString(): String = value
}
