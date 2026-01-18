package dev.ajithgoveas.khatape.domain.mappers

import dev.ajithgoveas.khatape.data.local.entity.TransactionEntity
import dev.ajithgoveas.khatape.domain.model.Transaction

fun TransactionEntity.toDomain(): Transaction = Transaction(
    id = id,
    friendId = friendId,
    amount = amount,
    direction = direction,
    description = description,
    isSettled = isSettled,
    dueDate = dueDate,
    timestamp = timestamp
)

fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
    id = id,
    friendId = friendId,
    amount = amount,
    direction = direction,
    description = description,
    isSettled = isSettled,
    dueDate = dueDate,
    timestamp = timestamp
)
