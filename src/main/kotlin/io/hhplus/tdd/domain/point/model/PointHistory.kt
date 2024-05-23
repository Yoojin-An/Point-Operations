package io.hhplus.tdd.domain.point.model

data class PointHistory(
    val id: Long,
    val userId: Long,
    val type: TransactionType,
    val amount: Long,
    val timeMillis: Long,
)

