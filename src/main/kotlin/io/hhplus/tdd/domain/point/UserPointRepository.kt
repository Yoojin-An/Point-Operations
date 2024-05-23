package io.hhplus.tdd.domain.point

import io.hhplus.tdd.domain.point.model.UserPoint

interface UserPointRepository {
    fun selectById(id: Long): UserPoint

    fun insertOrUpdate(id: Long, amount: Long): UserPoint
}