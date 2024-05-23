package io.hhplus.tdd.common

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import java.util.function.Supplier

@Component
class LockManager(@Value("\${lock.timeout}") private val timeout: Long) {
    private val lockMap: MutableMap<String, Lock> = ConcurrentHashMap()

    fun <T> executeFunctionWithLock(key: Long, function: Supplier<T>): T {
        // 신규 유저라면 ReentrantLock 객체를 새로 생성해서 사용
        // 기존 유저라면 lockMap에 저장된 ReentrantLock 객체 사용
        val lock: Lock = lockMap.computeIfAbsent(key.toString()) { ReentrantLock() }

        // 락 획득 성공 시 파라미터로 받은 함수의 실행 결과를 반환 후 락 해제
        try {
            val acquired: Boolean = lock.tryLock(timeout, TimeUnit.SECONDS)
            if (!acquired) {
                throw RuntimeException("지정된 락 획득 시도 시간을 초과했습니다.")
            }
            try {
                return function.get()
            } finally {
                lock.unlock()
            }
        } catch (e: Exception) {
            throw InterruptedException(e.message)
        }
    }
}