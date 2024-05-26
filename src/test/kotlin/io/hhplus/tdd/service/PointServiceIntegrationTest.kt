package io.hhplus.tdd.service

import io.hhplus.tdd.domain.point.PointService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.concurrent.CompletableFuture

@SpringBootTest
class PointServiceIntegrationTest @Autowired constructor (
    private val pointService: PointService,
) {
    /**
     * 동일 유저에 대한 동시성 처리
     */
    @Test
    fun 동일_유저의_포인트_충전_사용_동시_요청_시_포인트_증감_상태_검증() {
        // given: 1번 유저의 초기 잔고를 500000으로 설정
        val id = 1L
        val initialPoint = pointService.chargePoints(id, 500000L).point
        val amountToUse1 = 1000L // 사용할 금액 1
        val amountToCharge1 = 3000L // 충전할 금액 1
        val amountToCharge2 = 10000L // 충전할 금액 2

        // when: 동시에 실행시킬 충전 2회, 사용 1회의 테스크 정의
        val chargeTask1 = CompletableFuture.runAsync {
            try {
                pointService.chargePoints(id, amountToCharge1)
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }
        val useTask1 = CompletableFuture.runAsync {
            try {
                pointService.usePoints(id, amountToUse1)
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }
        val chargeTask2 = CompletableFuture.runAsync {
            try {
                pointService.chargePoints(id, amountToCharge2)
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }

        val totalTasks = CompletableFuture.allOf(chargeTask1, chargeTask2, useTask1)
        totalTasks.join() // chargeTask1, chargeTask2, useTask1 동시 실행
        val result = pointService.findPoints(id)

        //then
        assertEquals(initialPoint - amountToUse1 + amountToCharge1 + amountToCharge2, result?.point)
    }

    /**
     * 다수 유저의 다양한 요청에 대한 동시성 처리
     */
    @Test
    fun 동시에_다수_유저의_포인트_충전_사용_조회_요청_시_포인트_증감_상태_검증() {
        // given: 3명의 유저 모두 초기 잔고 50000인 상태에서 각각 포인트 충전, 사용, 조회 요청
        val id1 = 1L // 1번 유저 아이디
        val id2 = 2L // 2번 유저 아이디
        val id3 = 3L // 3번 유저 아이디
        val initialPoint = 50000L // 초기 잔고
        val amountToUse = 1000L // 각 유저가 1회 사용할 금액
        val amountToCharge = 5000L // 각 유저가 1회 충전할 금액
        pointService.chargePoints(id1, initialPoint)
        pointService.chargePoints(id2, initialPoint)
        pointService.chargePoints(id3, initialPoint)

        // when: 동시에 실행시킬 테스크 정의
        val user1ChargeTask1 = CompletableFuture.runAsync {
            try {
                pointService.chargePoints(id1, amountToCharge)
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }
        val user1ChargeTask2 = CompletableFuture.runAsync {
            try {
                pointService.chargePoints(id1, amountToCharge)
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }
        val user1GetTask1 = CompletableFuture.runAsync {
            try {
                pointService.findPoints(id1)
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }
        val user2UseTask1 = CompletableFuture.runAsync {
            try {
                pointService.usePoints(id2, amountToUse)
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }
        val user2ChargeTask1 = CompletableFuture.runAsync {
            try {
                pointService.chargePoints(id2, amountToCharge)
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }
        val user2ChargeTask2 = CompletableFuture.runAsync {
            try {
                pointService.chargePoints(id2, amountToCharge)
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }
        val user3UseTask1 = CompletableFuture.runAsync {
            try {
                pointService.usePoints(id3, amountToUse)
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }
        val user3UseTask2 = CompletableFuture.runAsync {
            try {
                pointService.usePoints(id3, amountToUse)
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }
        val user3ChargeTask1 = CompletableFuture.runAsync {
            try {
                pointService.chargePoints(id3, amountToCharge)
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }

        val totalTasks = CompletableFuture.allOf(
            user1ChargeTask1, user1ChargeTask2, user1GetTask1,
            user2UseTask1, user2ChargeTask1, user2ChargeTask2,
            user3UseTask1, user3UseTask2, user3ChargeTask1
        )
        totalTasks.join() // 모든 태스크 동시 실행

        val user1Result = pointService.findPoints(id1)
        val user2Result = pointService.findPoints(id2)
        val user3Result = pointService.findPoints(id3)

        //then
        assertEquals(initialPoint + amountToCharge + amountToCharge, user1Result?.point)
        assertEquals(initialPoint - amountToUse + amountToCharge + amountToCharge, user2Result?.point)
        assertEquals(initialPoint - amountToUse - amountToUse + amountToCharge, user3Result?.point)
    }
}