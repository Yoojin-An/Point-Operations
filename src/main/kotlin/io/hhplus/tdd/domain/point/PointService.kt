package io.hhplus.tdd.domain.point

import io.hhplus.tdd.common.LockManager
import io.hhplus.tdd.domain.point.model.PointHistory
import io.hhplus.tdd.domain.point.model.TransactionType
import io.hhplus.tdd.domain.point.model.UserPoint
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.locks.Lock
import javax.swing.plaf.RootPaneUI

@Service
class PointService(
    private val userPointRepository: UserPointRepository,
    private val pointHistoryRepository: PointHistoryRepository,
    private val lockManager: LockManager
) {
    // 특정 유저의 포인트 조회
    @Transactional
    fun getPoints(id: Long): UserPoint {
        if (id <= 0) {
            throw IllegalArgumentException("아이디가 유효하지 않습니다.")
        }
        val userPoint = userPointRepository.selectById(id)
        return userPoint
    }


    // 특정 유저의 포인트 충전/이용 내역 조회
    @Transactional
    fun getHistory(id: Long): List<PointHistory> {
        if (id <= 0) {
            throw IllegalArgumentException("아이디가 유효하지 않습니다.")
        }
        val userHistory = pointHistoryRepository.selectAllByUserId(id)
        return userHistory
    }

    // 특정 유저의 포인트 충전
    @Transactional
    fun chargePoints(id: Long, amountToCharge: Long): UserPoint {
        return lockManager.executeFunctionWithLock(id) {
            try {
                if (id <= 0) {
                    throw IllegalArgumentException("아이디가 유효하지 않습니다.")
                }
                if (amountToCharge <= 0) {
                    throw IllegalArgumentException("충전할 포인트는 양수여야 합니다.")
                }
                val currentBalance = userPointRepository.selectById(id).point
                val updatedUserPoint = userPointRepository.insertOrUpdate(id, currentBalance + amountToCharge)
                pointHistoryRepository.insert(id, updatedUserPoint.point, TransactionType.CHARGE, updatedUserPoint.updateMillis)
                updatedUserPoint
            } catch (e: InterruptedException) {
                throw RuntimeException(e.message)
            }
        }
    }

    // 특정 유저의 포인트 사용
    @Transactional
    fun usePoints(id: Long, amountToUse: Long): UserPoint {
        return lockManager.executeFunctionWithLock(id) {
            try {
                if (id <= 0) {
                    throw IllegalArgumentException("아이디가 유효하지 않습니다.")
                }
                if (amountToUse <= 0) {
                    throw IllegalArgumentException("사용할 포인트는 양수여야 합니다.")
                }
                val currentBalance = userPointRepository.selectById(id).point
                if (currentBalance < amountToUse) {
                    throw IllegalArgumentException("잔고가 부족합니다.")
                }
                val updatedBalance = userPointRepository.insertOrUpdate(id, currentBalance - amountToUse)
                pointHistoryRepository.insert(updatedBalance.id, updatedBalance.point, TransactionType.USE, updatedBalance.updateMillis)
                updatedBalance
            } catch (e: InterruptedException) {
                throw RuntimeException(e.message)
            }
        }
    }
}