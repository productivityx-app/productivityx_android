package com.oussama_chatri.productivityx.features.ai.data.remote.api

import com.oussama_chatri.productivityx.features.ai.data.remote.dto.request.CreateConversationRequest
import com.oussama_chatri.productivityx.features.ai.data.remote.dto.response.ConversationResponse
import com.oussama_chatri.productivityx.features.ai.data.remote.dto.response.ConversationsListResponse
import com.oussama_chatri.productivityx.features.ai.data.remote.dto.response.MessageResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface AiApiService {

    @GET("api/v1/ai/conversations")
    suspend fun getConversations(): ConversationsListResponse

    @GET("api/v1/ai/conversations/{id}")
    suspend fun getConversation(@Path("id") id: String): ConversationResponse

    @POST("api/v1/ai/conversations")
    suspend fun createConversation(
        @Body request: CreateConversationRequest,
    ): ConversationResponse

    @DELETE("api/v1/ai/conversations/{id}")
    suspend fun deleteConversation(@Path("id") id: String)

    // Message history is nested inside the ConversationResponse returned by getConversation().
    // No separate GET endpoint exists — messages are loaded from the conversation object.
    // SSE streaming is built raw via OkHttp + EventSource (see AiRepositoryImpl.sendMessage).
}
