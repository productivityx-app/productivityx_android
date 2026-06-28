package com.oussama_chatri.productivityx.features.settings.domain.repository

import com.oussama_chatri.productivityx.core.util.Resource
import com.oussama_chatri.productivityx.features.settings.domain.model.ProfileModel

interface ProfileRepository {
    suspend fun getProfile(): Resource<ProfileModel>
    suspend fun updateProfile(
        firstName: String?,
        lastName: String?,
        bio: String?,
        timezone: String?,
        language: String?,
        theme: String?
    ): Resource<ProfileModel>
    suspend fun updateAvatar(avatarUrl: String): Resource<ProfileModel>
}
