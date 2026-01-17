package dev.ajithgoveas.khatape.domain.mappers

import dev.ajithgoveas.khatape.data.local.entity.FriendEntity
import dev.ajithgoveas.khatape.domain.model.Friend
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

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
