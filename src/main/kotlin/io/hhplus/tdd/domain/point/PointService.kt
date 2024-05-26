package io.hhplus.tdd.domain.point

import io.hhplus.tdd.common.LockManager
import io.hhplus.tdd.common.PointManager
import io.hhplus.tdd.domain.point.model.PointHistory
import io.hhplus.tdd.domain.point.model.UserPoint
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PointService(
    private val lockManager: LockManager,
    private val pointManager: PointManager
) {
    // 특정 유저의 포인트 조회
    @Transactional
    fun findPoints(userId: Long): UserPoint? {
        return pointManager.findPoints(userId)
    }

    // 특정 유저의 포인트 충전/이용 내역 조회
    @Transactional
    fun findHistory(userId: Long): List<PointHistory>? {
        return pointManager.findHistory(userId)
    }

    // 특정 유저의 포인트 충전
    @Transactional
    fun chargePoints(userId: Long, amountToCharge: Long): UserPoint {
        return lockManager.executeFunctionWithLock(userId) {
            try {
                val updatedUserPoint = pointManager.chargePoints(userId, amountToCharge)
                updatedUserPoint
            } catch (e: InterruptedException) {
                throw RuntimeException(e.message)
            }
        }
    }

    // 특정 유저의 포인트 사용
    @Transactional
    fun usePoints(userId: Long, amountToUse: Long): UserPoint {
        return lockManager.executeFunctionWithLock(userId) {
            try {
                val updatedBalance = pointManager.usePoints(userId, amountToUse)
                updatedBalance
            } catch (e: InterruptedException) {
                throw RuntimeException(e.message)
            }
        }
    }
}