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

    @GET("api/v1/ai/conversations/{id}/messages")
    suspend fun getMessages(@Path("id") conversationId: String): List<MessageResponse>

    // SSE streaming endpoint — called via OkHttp EventSource, not Retrofit
    // This plain URL builder is kept here for discoverability
    fun streamUrl(conversationId: String): String =
        "api/v1/ai/conversations/$conversationId/messages"
}
