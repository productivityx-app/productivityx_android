package com.oussama_chatri.productivityx.features.profile.domain.usecase

import com.oussama_chatri.productivityx.core.util.Resource
import com.oussama_chatri.productivityx.features.profile.domain.model.UserPreferencesModel
import com.oussama_chatri.productivityx.features.profile.domain.repository.PreferencesRepository
import com.oussama_chatri.productivityx.features.profile.domain.repository.UpdatePreferencesParams
import javax.inject.Inject

class GetPreferencesUseCase @Inject constructor(
    private val repository: PreferencesRepository
) {
    suspend operator fun invoke(): Resource<UserPreferencesModel> = repository.getPreferences()
}

class UpdatePreferencesUseCase @Inject constructor(
    private val repository: PreferencesRepository
) {
    suspend operator fun invoke(params: UpdatePreferencesParams): Resource<UserPreferencesModel> =
        repository.updatePreferences(params)
}
