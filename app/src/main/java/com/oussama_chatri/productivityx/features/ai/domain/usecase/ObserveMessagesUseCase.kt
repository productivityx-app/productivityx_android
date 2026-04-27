package com.oussama_chatri.productivityx.features.ai.domain.usecase

import com.oussama_chatri.productivityx.features.ai.domain.model.Message
import com.oussama_chatri.productivityx.features.ai.domain.repository.AiRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

class ObserveMessagesUseCase @Inject constructor(
    private val repository: AiRepository,
) {
    operator fun invoke(conversationId: UUID): Flow<List<Message>> =
        repository.observeMessages(conversationId)
}
