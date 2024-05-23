package io.hhplus.tdd.infrastructure

import io.hhplus.tdd.common.PointHistoryTable
import io.hhplus.tdd.domain.point.model.PointHistory
import io.hhplus.tdd.domain.point.PointHistoryRepository
import io.hhplus.tdd.domain.point.model.TransactionType
import org.springframework.stereotype.Repository

@Repository
class PointHistoryRepositoryImplement(
    private val pointHistoryTable: PointHistoryTable
): PointHistoryRepository {
    override fun insert(id: Long,
                        amount: Long,
                        transactionType: TransactionType,
                        updateMillis: Long): PointHistory {
        return pointHistoryTable.insert(id, amount, transactionType, updateMillis)
    }

    override fun selectAllByUserId(userId: Long): List<PointHistory> {
        return pointHistoryTable.selectAllByUserId(userId)
    }
}