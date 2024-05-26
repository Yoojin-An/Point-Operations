package io.hhplus.tdd.common

import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

class AlwaysFailingLock : ReentrantLock() {
    override fun tryLock(timeout: Long, unit: TimeUnit?): Boolean {
        return false
    }
}
