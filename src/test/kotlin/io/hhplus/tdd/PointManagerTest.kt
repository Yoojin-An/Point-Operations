package io.hhplus.tdd

import io.hhplus.tdd.common.UserPointManager
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class UserPointManagerTest @Autowired constructor(
    private val pointManager: UserPointManager
) {
    @Test
    fun 유효한_아이디가_아니면_IllegalArgumentException_을_던진다() {
        // given: 유효하지 않은 아이디
        val invalidUserId = -1L

        // when & then: IllegalArgumentException 예외 발생 및 예외 메세지 검증
        val exception = assertThrows<IllegalArgumentException> {
            pointManager.validateId(invalidUserId)
        }
        assertEquals("아이디가 유효하지 않습니다.", exception.message)
    }

    @Test
    fun 유효한_아이디에_대한_현재_포인트를_조회한다() {
        // given: 유효한 아이디
        val validId = 1L

        // when: 포인트 조회
        val point = pointManager.getPoints(validId).point

        // then: 최초 서비스를 이용하는 유저이므로 현재 포인트가 0인지 검증
        assertEquals(0, point)
    }
    
    @Test
    fun 유효하지_않은_아이디에_대한_현재_포인트를_조회하면_실패한다() {
        // given: 유효하지 않은 아이디
        val validId = 0L

        // when & then: 포인트 조회 시 IllegalArgumentException 발생 검증
        assertThrows<IllegalArgumentException> {
            pointManager.getPoints(validId).point
        }
    }

    @Test
    fun `포인트_충전_또는_사용_이력이_없어도_포인트_내역_조회에_성공한다`() {
        // given: 유효한 아이디
        val id: Long = 3L

        // when: 신규 유저의 pointHistory 조회
        val pointHistory = pointManager.getHistories(id)

        // then: 충전/사용 이력 0회의 내역 조회 검증
        assertEquals(0, pointHistory.size)
    }

    @Test
    fun `포인트_충전에_성공한다`() {
        // given: 유효한 아이디
        val id = 6L

        // when: 포인트 충전
        val userPoint = pointManager.chargePoints(id, 10000L)

        // then: 충전 예상 결과값이 저장된 현재 포인트와 같은지 검증
        assertEquals(10000L, userPoint.point)
    }

    @Test
    fun 포인트_사용에_성공한다() {
        // given: 유효한 아이디와 유효한 포인트
        val id = 8L
        val amountToUse = 5000L

        // when: 잔고에 100000 포인트가 있음을 가정하고 5000 포인트 사용
        pointManager.chargePoints(id, 100000L)
        val userPoint = pointManager.usePoints(id, amountToUse)

        // then: 잔고에서 사용한 포인트를 뺀 값이 저장된 현재 포인트와 같은지 검증
        assertEquals(95000, userPoint.point)
    }
}
