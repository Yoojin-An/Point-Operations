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

    fun validateUserId(userId: Long) {
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
        val currentBalance = userPointRepository.selectById(userId)
            ?: throw IllegalArgumentException("${userId}번 유저의 정보가 없습니다.")
        if (amountToUse > currentBalance.point) {
            throw IllegalArgumentException("잔고가 부족합니다.")
        }
    }

    fun findPoints(userId: Long): UserPoint? {
        validateUserId(userId)

        return userPointRepository.selectById(userId)
            ?: throw IllegalArgumentException("${userId}번 유저의 정보가 없습니다.")
    }

    fun findHistory(userId: Long): List<PointHistory>? {
        validateUserId(userId)

        return pointHistoryRepository.selectAllByUserId(userId)
            ?: throw IllegalArgumentException("${userId}번 유저의 정보가 없습니다.")
    }

    fun chargePoints(userId: Long, amountToCharge: Long): UserPoint {
        validateUserId(userId)
        validateAmount(amountToCharge, TransactionType.CHARGE)

        val currentBalance = userPointRepository.selectById(userId)?.point ?: 0 // 최초 충전 시에는 잔고가 없으므로 0으로 초기화
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
        validateUserId(userId)
        validateAmount(amountToUse, TransactionType.USE)
        checkBalanceSufficient(userId, amountToUse)

        val currentBalance = userPointRepository.selectById(userId)?.point
            ?: throw IllegalArgumentException("{$userId}번 유저의 정보가 없습니다.")
        val updatedBalance = userPointRepository.insertOrUpdate(userId, currentBalance - amountToUse)
        pointHistoryRepository.insert(updatedBalance.id, updatedBalance.point, TransactionType.USE, updatedBalance.updateMillis)

        return updatedBalance
    }
}
