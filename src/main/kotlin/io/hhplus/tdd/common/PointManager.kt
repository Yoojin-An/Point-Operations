package io.hhplus.tdd.common

import io.hhplus.tdd.domain.point.PointHistoryRepository
import io.hhplus.tdd.domain.point.UserPointRepository
import io.hhplus.tdd.domain.point.model.PointHistory
import io.hhplus.tdd.domain.point.model.TransactionType
import io.hhplus.tdd.domain.point.model.UserPoint
import org.springframework.stereotype.Component

@Component
class PointManager (
    private val userPointRepository: UserPointRepository,
    private val pointHistoryRepository: PointHistoryRepository,
    ) {

    fun validateId(userId: Long) {
        if (userId <= 0) {
            throw IllegalArgumentException("아이디가 유효하지 않습니다.")
        }
    }

    fun validateAmount(amount: Long, transactionType: TransactionType) {
        val action = when (transactionType) {
            TransactionType.USE -> "사용"
            TransactionType.CHARGE -> "충전"
        }

        if (amount <= 0) {
            throw IllegalArgumentException("${action}할 포인트는 양수여야 합니다.")
        }
    }

    fun checkBalanceSufficient(userId: Long, amountToUse: Long) {
        val currentBalance = userPointRepository.selectById(userId).point
        if (amountToUse > currentBalance) {
            throw IllegalArgumentException("잔고가 부족합니다.")
        }
    }

    fun getPoints(userId: Long): UserPoint {
        validateId(userId)

        val userPoint = userPointRepository.selectById(userId)
        return userPoint
    }

    fun getHistory(userId: Long): List<PointHistory> {
        validateId(userId)
        val userHistory = pointHistoryRepository.selectAllByUserId(userId)
        return userHistory
    }

    fun chargePoints(userId: Long, amountToCharge: Long): UserPoint {
        validateId(userId)
        validateAmount(amountToCharge, TransactionType.CHARGE)

        val currentBalance = userPointRepository.selectById(userId).point
        val updatedUserPoint = userPointRepository.insertOrUpdate(userId, currentBalance + amountToCharge)
        pointHistoryRepository.insert(
            userId,
            updatedUserPoint.point,
            TransactionType.CHARGE,
            updatedUserPoint.updateMillis
        )
        return updatedUserPoint
    }

    fun usePoints(userId: Long, amountToUse: Long): UserPoint {
        validateId(userId)
        validateAmount(amountToUse, TransactionType.USE)
        checkBalanceSufficient(userId, amountToUse)

        val currentBalance = userPointRepository.selectById(userId).point
        val updatedBalance = userPointRepository.insertOrUpdate(userId, currentBalance - amountToUse)
        pointHistoryRepository.insert(updatedBalance.id, updatedBalance.point, TransactionType.USE, updatedBalance.updateMillis)

        return updatedBalance
    }
}