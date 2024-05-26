package io.hhplus.tdd

import io.hhplus.tdd.common.AlwaysFailingLock
import io.hhplus.tdd.common.LockManager
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.concurrent.*
import java.util.concurrent.locks.ReentrantLock
import java.util.function.Supplier

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
        val userId = 1L
        val result = "Fail"
        val function = Supplier { result }

        // userId에 무조건 락 획득을 실패하는 ReentrantLock 매핑
        val lockMapFieldOfLockManager = LockManager::class.java.getDeclaredField("lockMap")
        lockMapFieldOfLockManager.isAccessible = true
        val lockMap = lockMapFieldOfLockManager.get(lockManager) as ConcurrentHashMap<String, ReentrantLock>
        lockMap[userId.toString()] = AlwaysFailingLock()

        // when
        val exception = assertThrows<RuntimeException> {
            lockManager.executeFunctionWithLock(userId, function)
        }

        // then
        assertEquals("지정된 락 획득 시도 시간을 초과했습니다.", exception.message)
    }

    @Test
    fun 동시에_여러_요청이_들어오면_각_요청에_대해_락을_획득_후_순차적으로_처리한다() {
        // given
        val userId = 1L
        val function = Supplier { "Success" }
        val threadCount = 10
        val executorService = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)
        val results = mutableListOf<Future<String>>()

        // 10개의 task를 병렬로 동시에 실행
        try {
            (0 until threadCount).forEach { i ->
                val futureResult = executorService.submit(Callable<String> {
                    latch.countDown()  // 스레드 시작을 알림
                    latch.await()      // 모든 스레드가 시작할 때까지 대기
                    println("Task $i start")
                    val taskResult = lockManager.executeFunctionWithLock(userId, function)
                    println("Task $i end")
                    taskResult
                })
                results.add(futureResult)
            }
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
        // then
        results.forEach {
            assertEquals("Success", it.get())
        }

        executorService.shutdown()
        executorService.awaitTermination(1, TimeUnit.MINUTES)
    }
}