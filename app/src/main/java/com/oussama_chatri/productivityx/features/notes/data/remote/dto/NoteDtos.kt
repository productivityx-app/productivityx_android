package com.oussama_chatri.productivityx.features.notes.data.remote.dto

import com.google.gson.annotations.SerializedName

data class NoteRequestDto(
    @SerializedName("title")   val title: String?       = null,
    @SerializedName("content") val content: String?     = null,
    @SerializedName("tagIds")  val tagIds: Set<String>? = null,
    @SerializedName("pinned")  val pinned: Boolean?     = null
)

data class AddTagToNoteRequestDto(
    @SerializedName("tagId") val tagId: String
)

data class TagRequestDto(
    @SerializedName("name")  val name: String,
    @SerializedName("color") val color: String? = null
)

data class TagResponseDto(
    @SerializedName("id")        val id: String,
    @SerializedName("userId")    val userId: String?  = null,
    @SerializedName("name")      val name: String,
    @SerializedName("color")     val color: String,
    @SerializedName("createdAt") val createdAt: String? = null
)

data class NoteResponseDto(
    @SerializedName("id")                 val id: String,
    @SerializedName("userId")             val userId: String?   = null,
    @SerializedName("title")              val title: String     = "",
    @SerializedName("content")            val content: String   = "",
    @SerializedName("plainTextContent")   val plainTextContent: String = "",
    @SerializedName("wordCount")          val wordCount: Int    = 0,
    @SerializedName("readingTimeSeconds") val readingTimeSeconds: Int = 0,
    @SerializedName("pinned")             val pinned: Boolean   = false,
    @SerializedName("deleted")            val deleted: Boolean  = false,
    @SerializedName("deletedAt")          val deletedAt: String?  = null,
    @SerializedName("version")            val version: Int      = 1,
    @SerializedName("syncStatus")         val syncStatus: String? = null,
    @SerializedName("tags")               val tags: Set<TagResponseDto> = emptySet(),
    @SerializedName("createdAt")          val createdAt: String?  = null,
    @SerializedName("updatedAt")          val updatedAt: String?  = null
)

data class PagedResponseDto<T>(
    @SerializedName("content")       val content: List<T>,
    @SerializedName("page")          val page: Int           = 0,
    @SerializedName("size")          val size: Int           = 20,
    @SerializedName("totalElements") val totalElements: Long = 0,
    @SerializedName("totalPages")    val totalPages: Int     = 0,
    @SerializedName("last")          val last: Boolean       = true,
    @SerializedName("first")         val first: Boolean      = true
)