package com.oussama_chatri.productivityx.features.auth.domain.model

sealed class AuthResult {
    data class Success(val token: AuthToken, val user: AuthUser) : AuthResult()

    data class Error(val message: String, val code: String? = null) : AuthResult()
    data class Unverified(val email: String) : AuthResult()
}