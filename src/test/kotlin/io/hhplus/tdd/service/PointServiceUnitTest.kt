package io.hhplus.tdd.service

import org.junit.jupiter.api.Assertions.assertEquals
import io.hhplus.tdd.domain.point.PointService
import io.hhplus.tdd.domain.point.model.TransactionType
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.lang.AssertionError

@SpringBootTest
class PointServiceUnitTest @Autowired constructor (
    private val pointService: PointService,
) {
    /**
     * 포인트 조회 테스트
     */
    @Test
    fun 포인트_사용시_잔고가_부족하면_InterruptedException을_던진다() {
        // given: 잔고가 부족한 상황
        val id = 1L
        val amountToUse = 5000L

        // when & then: 초기 포인트 1000인 상황에서 5000 포인트 사용 시 InterruptedException 예외 발생 및 예외 메세지 검증
        pointService.chargePoints(id, 1000)
        val exception = assertThrows<InterruptedException> {
            pointService.usePoints(id, amountToUse)
        }
        assertEquals("잔고가 부족합니다.", exception.message)
    }

    @Test
    fun `포인트_충전_또는_사용_이력이_있는_경우_포인트_조회에_성공한다`() {
        // given
        val id: Long = 2L

        // when: 2번의 충전 이력이 있음을 가정
        pointService.chargePoints(id, 10000L)
        pointService.chargePoints(id, 20000L)
        val userPoint = pointService.findPoints(id)

        // then: 현재 포인트 조회 결과가 이전 2번 충전한 결과와 같음을 검증
        assertEquals(30000L, userPoint?.point)
    }

    @Test
    fun `포인트_충전_이력이_없는_경우_조회에_실패한다`() {
        // given: 유효한 아이디
        val id: Long = 1L

        // when: 한 번도 포인트를 충전한 적이 없는 아이디로 포인트 조회한 결과
        // IllegalArgumentException이 발생하고 예외 메세지가 예상한 바와 같음을 검증
        val exception = assertThrows<IllegalArgumentException> {
            pointService.findPoints(id)
        }
        assertEquals("${id}번 유저의 정보가 없습니다.", exception.message)
    }

    @Test
    fun `존재할_수_없는_아이디에_대한_포인트_조회는_실패한다`() {
        // given: 유효하지 않은 아이디
        val id: Long = 0L

        // when & then: 유효하지 않은 아이디로 포인트 조회한 결과
        // IllegalArgumentException이 발생하고 예외 메세지가 예상한 바와 같음을 검증
        val exception = assertThrows<IllegalArgumentException> {
            pointService.findPoints(id)
        }
        assertEquals("아이디가 유효하지 않습니다.", exception.message)
    }

    /**
     * 포인트 내역 조회 테스트
     */

    @Test
    fun `포인트_충전_또는_사용_이력이_있는_경우_포인트_내역_조회에_성공한다`() {
        // given: 유효한 아이디
        val id: Long = 5L

        // when: 3번 충전, 1번 사용의 이력이 있음을 가정
        pointService.chargePoints(id, 10000L)
        pointService.chargePoints(id, 20000L)
        pointService.usePoints(id, 20000L)
        pointService.chargePoints(id, 30000L)
        val pointHistory = pointService.findHistory(id) ?:
        throw AssertionError()

        // then: 잔고, 충전/사용 횟수, 충전/사용 타입 검증
        assertEquals(10000L, pointHistory[0].amount)
        assertEquals(4, pointHistory.size)
        assertEquals(TransactionType.USE, pointHistory[2].type)
    }

    @Test
    fun `포인트_충전_이력이_없는_경우_포인트_내역_조회에_실패한다`() {
        // given: 유효한 아이디
        val id: Long = 4L

        // when: 한 번도 포인트를 충전한 적이 없는 아이디로 포인트 내역 조회한 결과
        // IllegalArgumentException이 발생하고 예외 메세지가 예상한 바와 같음을 검증
        val exception = assertThrows<IllegalArgumentException> {
            pointService.findHistory(id)
        }
        assertEquals("${id}번 유저의 정보가 없습니다.", exception.message)
    }

    @Test
    fun `존재할_수_없는_아이디에_대한_포인트_내역_조회는_실패한다`() {
        // given: 유효하지 않은 아이디
        val id = -1L

        // when & then: 유효하지 않은 아이디로 포인트 내역 조회한 결과
        // IllegalArgumentException이 발생하고 예외 메세지가 예상한 바와 같음을 검증
        val exception = assertThrows<IllegalArgumentException> {
            pointService.findHistory(id)
        }
        assertEquals("아이디가 유효하지 않습니다.", exception.message)
    }

    /**
     * 포인트 충전 테스트
     */
    @Test
    fun `포인트_충전에_성공한다`() {
        // given: 유효한 아이디
        val id = 6L

        // when: 포인트 충전
        val userPoint = pointService.chargePoints(id, 10000L)

        // then: 충전 예상 결과값이 저장된 현재 포인트와 같은지 검증
        assertEquals(10000L, userPoint.point)
    }

    @Test
    fun `충전할_포인트가_음수이거나_0이면_충전에_실패한다`() {
        // given: 유효한 아이디와 유효하지 않은 충전 포인트
        val id = 7L
        val amountToCharge = -500L

        // when: 1000 포인트 충전에는 성공 후 -500 포인트 충전으로 예외 발생
        pointService.chargePoints(7L, 1000L)
        val exception = assertThrows<InterruptedException> {
            pointService.chargePoints(id, amountToCharge)
        }

        // then: InterrupedException 발생 메세지 및 예외 발생 전후 포인트 일치 상태 검증
        assertEquals("충전할 포인트는 양수여야 합니다.", exception.message)
        assertEquals(1000L, pointService.findPoints(7L)?.point)
    }

    @Test
    fun `존재할_수_없는_아이디에_대한_포인트_충전은_실패한다`() {
        // given: 유효하지 않은 아이디와 유효한 포인트
        val id = -1L
        val amount = 10000L

        // when & then: 유효하지 않은 아이디로 포인트 조회한 결과
        // InterruptedException이 발생하고 예외 메세지가 예상한 바와 같음을 검증
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
        // given: 유효한 아이디와 유효한 포인트
        val id = 8L
        val amountToUse = 5000L

        // when: 잔고에 100000 포인트가 있음을 가정하고 5000 포인트 사용
        pointService.chargePoints(id, 100000L)
        val userPoint = pointService.usePoints(id, amountToUse)

        // then: 잔고에서 사용한 포인트를 뺀 값이 저장된 현재 포인트와 같은지 검증
        assertEquals(95000, userPoint.point)

    }

    @Test
    fun `잔고_부족으로_포인트_사용에_실패한다`() {
        // given: 유효한 아이디와 사용할 포인트
        val id = 9L
        val amountToUse = 1000000L

        // when: 잔고가 10000 포인트 있음을 가정
        pointService.chargePoints(id, 10000L)

        // then: 잔고 이상의 포인트를 사용하여 IllegalArgumentException이 발생
        val exception = assertThrows<InterruptedException> {
            pointService.usePoints(id, amountToUse)
        }
        // 예외 메세지 및 예외 발생 전후 포인트 일치 상태 검증
        assertEquals("잔고가 부족합니다.", exception.message)
        assertEquals(10000, pointService.findPoints(9L)?.point)
    }

    @Test
    fun `사용할_포인트가_0이거나_음수이면_포인트_사용에_실패한다`() {
        // given: 유효한 아이디와 유효하지 않은 포인트
        val id = 10L
        val amountToUse = 0L

        // when & then: 유효하지 않은 포인트 사용을 시도한 결과
        // InterruptedExceptionn이 발생하고 예외 메세지가 예상한 바와 같음을 검증
        val exception = assertThrows<InterruptedException> {
            pointService.usePoints(id, amountToUse)
        }
        assertEquals("사용할 포인트는 양수여야 합니다.", exception.message)
    }

    @Test
    fun `존재할_수_없는_아이디에_대한_포인트_사용은_실패한다`() {
        // given: 유효하지 않은 아이디와 유효한 포인트
        val inValidId = -1L
        val amountToUse = 10000L

        // when & then: 유효하지 않은 아이디의 포인트 사용을 시도한 결과
        val exception = assertThrows<InterruptedException> {
            pointService.chargePoints(inValidId, amountToUse)
        }
        // InterruptedException이 발생하고 예외 메세지가 예상한 바와 같음을 검증
        assertEquals("아이디가 유효하지 않습니다.", exception.message)
    }
}