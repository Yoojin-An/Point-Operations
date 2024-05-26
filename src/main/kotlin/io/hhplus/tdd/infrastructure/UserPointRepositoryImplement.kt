package io.hhplus.tdd.infrastructure

import io.hhplus.tdd.common.UserPointTable
import io.hhplus.tdd.domain.point.model.UserPoint
import io.hhplus.tdd.domain.point.UserPointRepository
import org.springframework.stereotype.Repository

@Repository
class UserPointRepositoryImplement(
    private val userPointTable: UserPointTable
): UserPointRepository {
    override fun selectById(id: Long): UserPoint? {
        return userPointTable.selectById(id)
    }

    override fun insertOrUpdate(id: Long, amount: Long): UserPoint {
        return userPointTable.insertOrUpdate(id, amount)
    }
}