package com.oussama_chatri.productivityx.features.tasks.data.remote.api

import com.oussama_chatri.productivityx.features.tasks.data.remote.dto.ApiResponseDto
import com.oussama_chatri.productivityx.features.tasks.data.remote.dto.PagedTaskResponseDto
import com.oussama_chatri.productivityx.features.tasks.data.remote.dto.ReorderRequestDto
import com.oussama_chatri.productivityx.features.tasks.data.remote.dto.TaskRequestDto
import com.oussama_chatri.productivityx.features.tasks.data.remote.dto.TaskResponseDto
import com.oussama_chatri.productivityx.features.tasks.data.remote.dto.UpdateStatusRequestDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface TaskApi {

    @POST("api/v1/tasks")
    suspend fun createTask(
        @Body request: TaskRequestDto
    ): ApiResponseDto<TaskResponseDto>

    @GET("api/v1/tasks")
    suspend fun listTasks(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50,
        @Query("status") status: String? = null,
        @Query("priority") priority: String? = null,
        @Query("parentId") parentId: String? = null
    ): ApiResponseDto<PagedTaskResponseDto>

    @GET("api/v1/tasks/trash")
    suspend fun listTrash(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50
    ): ApiResponseDto<PagedTaskResponseDto>

    @GET("api/v1/tasks/{id}")
    suspend fun getTaskById(
        @Path("id") id: String
    ): ApiResponseDto<TaskResponseDto>

    @PUT("api/v1/tasks/{id}")
    suspend fun updateTask(
        @Path("id") id: String,
        @Body request: TaskRequestDto
    ): ApiResponseDto<TaskResponseDto>

    @PATCH("api/v1/tasks/{id}/status")
    suspend fun updateStatus(
        @Path("id") id: String,
        @Body request: UpdateStatusRequestDto
    ): ApiResponseDto<TaskResponseDto>

    @PATCH("api/v1/tasks/reorder")
    suspend fun reorder(
        @Body request: ReorderRequestDto
    ): ApiResponseDto<Void>

    @PATCH("api/v1/tasks/{id}/restore")
    suspend fun restoreTask(
        @Path("id") id: String
    ): ApiResponseDto<TaskResponseDto>

    @DELETE("api/v1/tasks/{id}")
    suspend fun softDeleteTask(
        @Path("id") id: String
    ): ApiResponseDto<TaskResponseDto>

    @DELETE("api/v1/tasks/{id}/permanent")
    suspend fun hardDeleteTask(
        @Path("id") id: String
    ): ApiResponseDto<Void>
}
