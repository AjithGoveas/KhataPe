package dev.ajithgoveas.khatape.data.local.converter

import androidx.room.TypeConverter
import dev.ajithgoveas.khatape.domain.model.TransactionDirection

class TransactionDirectionConverter {
    @TypeConverter
    fun fromDirection(direction: TransactionDirection): String = direction.name

    @TypeConverter
    fun toDirection(value: String): TransactionDirection = TransactionDirection.valueOf(value)
}
