package com.oussama_chatri.productivityx.features.settings.domain.usecase

import com.oussama_chatri.productivityx.core.util.Resource
import com.oussama_chatri.productivityx.features.settings.domain.model.ProfileModel
import com.oussama_chatri.productivityx.features.settings.domain.repository.ProfileRepository
import javax.inject.Inject

class GetProfileUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(): Resource<ProfileModel> = repository.getProfile()
}

class UpdateProfileUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(
        firstName: String?,
        lastName: String?,
        bio: String?,
        timezone: String?,
        language: String?,
        theme: String?
    ): Resource<ProfileModel> = repository.updateProfile(
        firstName, lastName, bio, timezone, language, theme
    )
}

class UpdateAvatarUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(avatarUrl: String): Resource<ProfileModel> =
        repository.updateAvatar(avatarUrl)
}
