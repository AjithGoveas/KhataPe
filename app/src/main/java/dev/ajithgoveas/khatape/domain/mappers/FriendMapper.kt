package dev.ajithgoveas.khatape.domain.mappers

import dev.ajithgoveas.khatape.data.local.entity.FriendEntity
import dev.ajithgoveas.khatape.domain.model.Friend

fun FriendEntity.toDomain(): Friend = Friend(
    id = id,
    name = name,
    avatarUrl = avatarUrl,
    createdAt = createdAt
)

fun Friend.toEntity(): FriendEntity = FriendEntity(
    id = id,
    name = name,
    avatarUrl = avatarUrl,
    createdAt = createdAt
)
