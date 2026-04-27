package com.oussama_chatri.productivityx.features.ai.domain.usecase

import com.oussama_chatri.productivityx.features.ai.domain.repository.AiRepository
import javax.inject.Inject

class DeleteConversationUseCase @Inject constructor(
    private val repository: AiRepository,
) {
    suspend operator fun invoke(conversationId: java.util.UUID) =
        repository.deleteConversation(conversationId)
}
