package io.hhplus.tdd.controller

import io.hhplus.tdd.domain.point.model.PointHistory
import io.hhplus.tdd.domain.point.PointService
import io.hhplus.tdd.domain.point.dto.ChargeRequest
import io.hhplus.tdd.domain.point.dto.UseRequest
import io.hhplus.tdd.domain.point.model.UserPoint
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/points")
class PointController(
    private val pointService: PointService
) {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    // 특정 유저의 포인트 조회
    @GetMapping("{id}")
    fun getPoints(
        @PathVariable id: Long,
    ): UserPoint? {
        val userPoint = pointService.findPoints(id)
        logger.info("id ${id}번 유저가 잔고(${userPoint?.point}p)를 조회했습니다.")
        return userPoint
    }

    // 특정 유저의 포인트 충전/사용 내역 조회
    @GetMapping("{id}/histories")
    fun getHistory(
        @PathVariable id: Long,
    ): List<PointHistory>? {
        val pointHistory = pointService.findHistory(id)
        logger.info("id ${id}번 유저가 포인트 충전/사용 내역을 조회했습니다. :: $pointHistory")
        return pointHistory
    }

    // 특정 유저의 포인트 충전
    @PatchMapping("{id}/charge")
    fun chargePoints(
        @PathVariable id: Long,
        @RequestBody chargeRequest: ChargeRequest
    ): UserPoint {
        val userPoint = pointService.chargePoints(id, chargeRequest.amount)
        logger.info("id ${id}번 유저가 ${chargeRequest.amount}포인트를 충전했습니다.")
        return userPoint
    }

    // 특정 유저의 포인트 사용
    @PatchMapping("{id}/use")
    fun usePoints(
        @PathVariable id: Long,
        @RequestBody useRequest: UseRequest,
    ): UserPoint {
        val userPoint = pointService.usePoints(id, useRequest.amount)
        logger.info("id ${id}번 유저가 ${useRequest.amount}포인트를 사용했습니다.")
        return userPoint
    }
}
