package com.oussama_chatri.productivityx.features.profile.domain.model

data class ProfileModel(
    val id: String,
    val userId: String,
    val firstName: String,
    val lastName: String,
    val fullName: String,
    val avatarUrl: String?,
    val bio: String?,
    val timezone: String,
    val language: String,
    val theme: String,
    val updatedAt: String?
)
