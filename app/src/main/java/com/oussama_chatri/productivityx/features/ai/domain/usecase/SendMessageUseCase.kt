package com.oussama_chatri.productivityx.features.ai.domain.usecase

import com.oussama_chatri.productivityx.features.ai.domain.repository.AiRepository
import com.oussama_chatri.productivityx.features.ai.domain.repository.StreamChunk
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val repository: AiRepository,
) {
    operator fun invoke(
        conversationId: UUID,
        content: String,
    ): Flow<StreamChunk> = repository.sendMessage(conversationId, content)
}
