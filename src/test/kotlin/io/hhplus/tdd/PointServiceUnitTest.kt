package io.hhplus.tdd

import org.junit.jupiter.api.Assertions.assertEquals
import io.hhplus.tdd.domain.point.PointService
import io.hhplus.tdd.domain.point.model.TransactionType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class PointServiceUnitTest @Autowired constructor (
    private val pointService: PointService,
) {
    /**
     * 포인트 조회 테스트
     */
    @Test
    fun `포인트_충전_또는_사용_이력이_없는_경우에도_조회에_성공한다`() {
        // given
        val id: Long = 1L

        // when
        val userPoint = pointService.getPoints(id)

        // then
        assertEquals(0, userPoint.point)
    }

    @Test
    fun `포인트_충전_또는_사용_이력이_있는_경우_포인트_조회에_성공한다`() {
        // given
        val id: Long = 2L

        // when: 2번 충전했음을 가정
        pointService.chargePoints(id, 10000L)
        pointService.chargePoints(id, 20000L)
        val userPoint = pointService.getPoints(id)

        // then
        assertEquals(30000L, userPoint.point)
    }

    @Test
    fun `존재할_수_없는_아이디에_대한_포인트_조회는_실패한다`() {
        // given
        val id: Long = 0L

        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            pointService.getPoints(id)
        }

        // then
        assertEquals("아이디가 유효하지 않습니다.", exception.message)
    }

    /**
     * 포인트 내역 조회 테스트
     */
    @Test
    fun `포인트_충전_또는_사용_이력이_없어도_포인트_내역_조회에_성공한다`() {
        // given
        val id: Long = 4L

        // when: 신규 유저의 pointHistory 조회
        val pointHistory = pointService.getHistory(id)

        // then
        assertEquals(0, pointHistory.size)
    }

    @Test
    fun `포인트_충전_또는_사용_이력이_있는_경우_포인트_내역_조회에_성공한다`() {
        // given
        val id: Long = 5L

        // when: 3번 충전, 1번 사용했음을 가정
        pointService.chargePoints(id, 10000L)
        pointService.chargePoints(id, 20000L)
        pointService.usePoints(id, 20000L)
        pointService.chargePoints(id, 30000L)
        val pointHistory = pointService.getHistory(id)

        // then
        assertEquals(10000L, pointHistory[0].amount)
        assertEquals(4, pointHistory.size)
        assertEquals(TransactionType.USE, pointHistory[2].type)
    }

    @Test
    fun `존재할_수_없는_아이디에_대한_포인트_내역_조회는_실패한다`() {
        // given
        val id = -1L

        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            pointService.getHistory(id)
        }
        assertEquals("아이디가 유효하지 않습니다.", exception.message)
    }

    /**
     * 포인트 충전 테스트
     */
    @Test
    fun `포인트_충전에_성공한다`() {
        // given
        val id = 6L

        // when
        val userPoint = pointService.chargePoints(id, 10000L)

        // then
        assertEquals(10000L, userPoint.point)
    }

    @Test
    fun `충전할_포인트가_음수이거나_0이면_충전에_실패한다`() {
        // given
        val id = 7L
        val amount = -500L

        // when & then
        val exception = assertThrows<InterruptedException> {
            pointService.chargePoints(id, amount)
        }
        assertEquals("충전할 포인트는 양수여야 합니다.", exception.message)
    }

    @Test
    fun `존재할_수_없는_아이디에_대한_포인트_충전은_실패한다`() {
        // given
        val id = -1L
        val amount = 10000L

        // when & then
        val exception = assertThrows<InterruptedException> {
            pointService.chargePoints(id, amount)
        }
        assertEquals("아이디가 유효하지 않습니다.", exception.message)
    }

    /**
     * 포인트 사용 테스트
     */
    @Test
    fun 포인트_사용에_성공한다() {
        // given
        val id = 8L
        val amountToUse = 5000L

        // when: 잔고에 100000 포인트가 있음을 가정
        pointService.chargePoints(id, 100000L)
        val userPoint = pointService.usePoints(id, amountToUse)

        // then
        assertEquals(95000, userPoint.point)

    }

    @Test
    fun `잔고_부족으로_포인트_사용에_실패한다`() {
        // given
        val id = 9L
        val amountToUse = 1000000L

        // when: 잔고가 10000 포인트 있는 상황을 가정
        pointService.chargePoints(id, 10000L)

        // then
        val exception = assertThrows<InterruptedException> {
            pointService.usePoints(id, amountToUse)
        }
        assertEquals("잔고가 부족합니다.", exception.message)
    }

    @Test
    fun `사용할_포인트가_0이거나_음수이면_포인트_사용에_실패한다`() {
        // given
        val id = 10L
        val amountToUse = 0L

        // when & then
        val exception = assertThrows<InterruptedException> {
            pointService.usePoints(id, amountToUse)
        }
        assertEquals("사용할 포인트는 양수여야 합니다.", exception.message)
    }

    @Test
    fun `존재할_수_없는_아이디에_대한_포인트_사용은_실패한다`() {
        // given
        val inValidId = -1L
        val amountToUse = 10000L

        // when & then
        val exception = assertThrows<InterruptedException> {
            pointService.chargePoints(inValidId, amountToUse)
        }
        assertEquals("아이디가 유효하지 않습니다.", exception.message)
    }
}