package dev.ajithgoveas.khatape.domain.mappers

import dev.ajithgoveas.khatape.domain.model.FriendSummary

// No entity mapping needed unless you store summaries
fun FriendSummary.toDisplayString(): String =
    "$name → Credit ₹$totalCredit | Debit ₹$totalDebit"
