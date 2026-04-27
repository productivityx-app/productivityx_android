package com.oussama_chatri.productivityx.features.ai.domain.usecase

import com.oussama_chatri.productivityx.features.ai.domain.model.AiContext
import com.oussama_chatri.productivityx.features.ai.domain.repository.AiRepository
import javax.inject.Inject

class BuildAiContextUseCase @Inject constructor(
    private val repository: AiRepository,
) {
    suspend operator fun invoke(): AiContext = repository.buildContext()
}
