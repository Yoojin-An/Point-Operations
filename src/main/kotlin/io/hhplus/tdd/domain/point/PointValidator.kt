package io.hhplus.tdd.domain.point

class PointValidator {
//    fun calcutateSum(amount: Long): Long {
//        return when {
//            amount > 0 ->
//            amount == 0L -> throw RuntimeException("충전할 포인트는 0보다 커야 합니다.")
//            amount < 0 -> throw RuntimeException("충전할 포인트는 0보다 커야 합니다.")
//            else -> throw RuntimeException("충전할 포인트는 0보다 커야 합니다.")
//        }
//    }

    fun isBalanceSufficient(amount: Long, balance: Long): Boolean {
        return when {
            ( balance - amount ) >= 0 -> true
            else -> false
        }
    }
}