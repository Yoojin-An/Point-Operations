package io.hhplus.tdd

import io.hhplus.tdd.common.LockManager
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.concurrent.*
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import java.util.function.Supplier
import java.util.stream.IntStream

@SpringBootTest
class LockManagerTest @Autowired constructor(
    private val lockManager: LockManager
) {
    @Test
    fun 락_획득_시_파라미터로_받은_함수를_실행시킨다() {
        // given
        val key = 1L
        val result = "Success"
        val function = Supplier { result }

        // when
        val actualResult = lockManager.executeFunctionWithLock(key, function)

        // then
        assertEquals(result, actualResult)
    }

    @Test
    fun 락_획득_실패_시_InterruptedException을_던진다() {
        // given

        // when

        // then
    }

    @Test
    fun 동시에_여러_요청이_들어오면_각_요청에_대해_락을_획득_후_순차적으로_처리한다() {
        // given

        // when

        // then
    }
}