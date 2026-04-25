package com.oussama_chatri.productivityx.features.notes.domain.model

import java.time.Instant

data class Tag(
    val id: String,
    val userId: String,
    val name: String,
    val color: String,
    val createdAt: Instant
)
