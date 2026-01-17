package dev.ajithgoveas.khatape.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import dev.ajithgoveas.khatape.domain.model.TransactionDirection

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = FriendEntity::class,
            parentColumns = ["id"],
            childColumns = ["friendId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["friendId"])]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val friendId: Long,
    val amount: Double,
    val direction: TransactionDirection,
    val description: String,
    val isSettled: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
