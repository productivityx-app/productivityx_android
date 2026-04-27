package com.oussama_chatri.productivityx.features.ai.domain.usecase

import com.oussama_chatri.productivityx.features.ai.domain.model.Conversation
import com.oussama_chatri.productivityx.features.ai.domain.repository.AiRepository
import javax.inject.Inject

class CreateConversationUseCase @Inject constructor(
    private val repository: AiRepository,
) {
    suspend operator fun invoke(title: String? = null): Conversation =
        repository.createConversation(title)
}
