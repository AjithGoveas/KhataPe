package dev.ajithgoveas.khatape.domain.model

data class Friend(
    val id: Long,
    val name: String,
    val avatarUrl: String?,
    val createdAt: Long
)
