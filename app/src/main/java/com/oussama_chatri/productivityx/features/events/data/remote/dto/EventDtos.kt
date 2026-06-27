package com.oussama_chatri.productivityx.features.events.data.remote.dto

import com.google.gson.annotations.SerializedName

data class EventRequestDto(
    @SerializedName("title")            val title: String,
    @SerializedName("description")      val description: String?   = null,
    @SerializedName("location")         val location: String?      = null,
    @SerializedName("startAt")          val startAt: String,         // ISO-8601
    @SerializedName("endAt")            val endAt: String,           // ISO-8601
    @SerializedName("allDay")           val isAllDay: Boolean       = false,
    @SerializedName("color")            val color: String           = "#6366F1",
    @SerializedName("recurrenceRule")   val recurrenceRule: String? = null,
    @SerializedName("recurrenceEndAt")  val recurrenceEndAt: String? = null,
    @SerializedName("reminderMinutes")  val reminderMinutes: Int?   = null
)

data class EventResponseDto(
    @SerializedName("id")                   val id: String,
    @SerializedName("userId")               val userId: String?       = null,
    @SerializedName("recurrenceParentId")   val recurrenceParentId: String? = null,
    @SerializedName("title")                val title: String,
    @SerializedName("description")          val description: String?  = null,
    @SerializedName("location")             val location: String?     = null,
    @SerializedName("startAt")              val startAt: String,
    @SerializedName("endAt")                val endAt: String,
    @SerializedName("allDay")               val isAllDay: Boolean     = false,
    @SerializedName("color")                val color: String         = "#6366F1",
    @SerializedName("recurrenceRule")       val recurrenceRule: String? = null,
    @SerializedName("recurrenceEndAt")      val recurrenceEndAt: String? = null,
    @SerializedName("reminderMinutes")      val reminderMinutes: Int? = null,
    @SerializedName("deleted")              val deleted: Boolean      = false,
    @SerializedName("deletedAt")            val deletedAt: String?    = null,
    @SerializedName("version")              val version: Int          = 1,
    @SerializedName("createdAt")            val createdAt: String?    = null,
    @SerializedName("updatedAt")            val updatedAt: String?    = null
)
