package com.oussama_chatri.productivityx.features.profile.data.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateProfileRequest(
    @SerialName("firstName") val firstName: String? = null,
    @SerialName("lastName") val lastName: String? = null,
    @SerialName("bio") val bio: String? = null,
    @SerialName("timezone") val timezone: String? = null,
    @SerialName("language") val language: String? = null,
    @SerialName("theme") val theme: String? = null
)

@Serializable
data class UpdateAvatarRequest(
    @SerialName("avatarUrl") val avatarUrl: String
)
