package com.oussama_chatri.productivityx.features.profile.data.repository

import com.oussama_chatri.productivityx.core.network.safeApiCall
import com.oussama_chatri.productivityx.core.util.Resource
import com.oussama_chatri.productivityx.features.profile.data.remote.api.ProfileApi
import com.oussama_chatri.productivityx.features.profile.data.remote.dto.request.UpdateAvatarRequest
import com.oussama_chatri.productivityx.features.profile.data.remote.dto.request.UpdateProfileRequest
import com.oussama_chatri.productivityx.features.profile.domain.model.ProfileModel
import com.oussama_chatri.productivityx.features.profile.domain.repository.ProfileRepository
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val api: ProfileApi
) : ProfileRepository {

    override suspend fun getProfile(): Resource<ProfileModel> = safeApiCall {
        val response = api.getProfile()
        response.body()?.data?.toDomain()
            ?: error("Empty profile response")
    }

    override suspend fun updateProfile(
        firstName: String?,
        lastName: String?,
        bio: String?,
        timezone: String?,
        language: String?,
        theme: String?
    ): Resource<ProfileModel> = safeApiCall {
        val response = api.updateProfile(
            UpdateProfileRequest(
                firstName = firstName,
                lastName = lastName,
                bio = bio,
                timezone = timezone,
                language = language,
                theme = theme
            )
        )
        response.body()?.data?.toDomain()
            ?: error("Empty profile response")
    }

    override suspend fun updateAvatar(avatarUrl: String): Resource<ProfileModel> = safeApiCall {
        val response = api.updateAvatar(UpdateAvatarRequest(avatarUrl = avatarUrl))
        response.body()?.data?.toDomain()
            ?: error("Empty profile response")
    }
}
