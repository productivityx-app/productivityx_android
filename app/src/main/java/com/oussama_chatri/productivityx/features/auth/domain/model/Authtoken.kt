package com.oussama_chatri.productivityx.features.auth.domain.model

data class AuthToken(
    val accessToken: String,
    val tokenType: String = "Bearer",
    val expiresInSeconds: Long
)