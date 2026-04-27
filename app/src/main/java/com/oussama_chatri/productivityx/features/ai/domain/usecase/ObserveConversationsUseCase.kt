package com.oussama_chatri.productivityx.features.ai.domain.usecase

import com.oussama_chatri.productivityx.features.ai.domain.model.Conversation
import com.oussama_chatri.productivityx.features.ai.domain.repository.AiRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveConversationsUseCase @Inject constructor(
    private val repository: AiRepository,
) {
    operator fun invoke(): Flow<List<Conversation>> = repository.observeConversations()
}
