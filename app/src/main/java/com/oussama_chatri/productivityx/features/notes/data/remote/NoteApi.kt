package com.oussama_chatri.productivityx.features.notes.data.remote

import com.oussama_chatri.productivityx.features.auth.data.remote.dto.response.ApiResponse
import com.oussama_chatri.productivityx.features.notes.data.remote.dto.AddTagToNoteRequestDto
import com.oussama_chatri.productivityx.features.notes.data.remote.dto.NoteRequestDto
import com.oussama_chatri.productivityx.features.notes.data.remote.dto.NoteResponseDto
import com.oussama_chatri.productivityx.features.notes.data.remote.dto.PagedResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface NoteApi {

    @POST("api/v1/notes")
    suspend fun createNote(
        @Body request: NoteRequestDto
    ): Response<ApiResponse<NoteResponseDto>>

    @GET("api/v1/notes/{id}")
    suspend fun getNoteById(
        @Path("id") id: String
    ): Response<ApiResponse<NoteResponseDto>>

    @GET("api/v1/notes")
    suspend fun listActiveNotes(
        @Query("page")   page: Int = 0,
        @Query("size")   size: Int = 20,
        @Query("tagId")  tagId: String? = null,
        @Query("pinned") pinned: Boolean? = null
    ): Response<ApiResponse<PagedResponseDto<NoteResponseDto>>>

    @GET("api/v1/notes/trash")
    suspend fun listTrash(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiResponse<PagedResponseDto<NoteResponseDto>>>

    @PUT("api/v1/notes/{id}")
    suspend fun updateNote(
        @Path("id") id: String,
        @Body request: NoteRequestDto
    ): Response<ApiResponse<NoteResponseDto>>

    @PATCH("api/v1/notes/{id}/pin")
    suspend fun pinNote(
        @Path("id") id: String
    ): Response<ApiResponse<NoteResponseDto>>

    @PATCH("api/v1/notes/{id}/unpin")
    suspend fun unpinNote(
        @Path("id") id: String
    ): Response<ApiResponse<NoteResponseDto>>

    @DELETE("api/v1/notes/{id}")
    suspend fun softDeleteNote(
        @Path("id") id: String
    ): Response<ApiResponse<NoteResponseDto>>

    @PATCH("api/v1/notes/{id}/restore")
    suspend fun restoreNote(
        @Path("id") id: String
    ): Response<ApiResponse<NoteResponseDto>>

    @DELETE("api/v1/notes/{id}/permanent")
    suspend fun hardDeleteNote(
        @Path("id") id: String
    ): Response<ApiResponse<Void>>

    @POST("api/v1/notes/{id}/tags")
    suspend fun addTagToNote(
        @Path("id") noteId: String,
        @Body request: AddTagToNoteRequestDto
    ): Response<ApiResponse<NoteResponseDto>>

    @DELETE("api/v1/notes/{noteId}/tags/{tagId}")
    suspend fun removeTagFromNote(
        @Path("noteId") noteId: String,
        @Path("tagId") tagId: String
    ): Response<ApiResponse<NoteResponseDto>>
}
