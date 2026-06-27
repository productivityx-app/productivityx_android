package com.oussama_chatri.productivityx.features.ai.domain.repository

import com.oussama_chatri.productivityx.features.ai.domain.model.Conversation
import com.oussama_chatri.productivityx.features.ai.domain.model.Message
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface AiRepository {

    // Conversations
    fun observeConversations(): Flow<List<Conversation>>
    suspend fun getConversation(id: UUID): Conversation?
    suspend fun createConversation(title: String? = null): Conversation
    suspend fun deleteConversation(id: UUID)
    suspend fun archiveConversation(id: UUID)

    // Messages
    fun observeMessages(conversationId: UUID): Flow<List<Message>>
    suspend fun getMessages(conversationId: UUID): List<Message>

    /**
     * Sends a user message and streams the assistant reply token-by-token.
     * Emits each partial token as it arrives from the SSE endpoint.
     * The repository persists the final assembled message locally on completion.
     */
    fun sendMessage(
        conversationId: UUID,
        content: String,
    ): Flow<StreamChunk>
}

sealed class StreamChunk {
    data class Token(val text: String) : StreamChunk()
    data class Done(val message: Message) : StreamChunk()
    data class Error(val cause: Throwable) : StreamChunk()
}
