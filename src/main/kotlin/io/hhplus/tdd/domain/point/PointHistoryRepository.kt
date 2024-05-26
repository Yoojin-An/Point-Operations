package io.hhplus.tdd.domain.point

import io.hhplus.tdd.domain.point.model.PointHistory
import io.hhplus.tdd.domain.point.model.TransactionType

interface PointHistoryRepository {
    fun insert(id: Long,
               amount: Long,
               transactionType: TransactionType,
               updateMillis: Long): PointHistory
    fun selectAllByUserId(userId: Long): List<PointHistory>?
}