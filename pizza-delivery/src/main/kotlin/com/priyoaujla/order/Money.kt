package com.priyoaujla.order

data class Money(val value: Double) {
    operator fun plus(increment: Money): Money {
        return Money(value + increment.value)
    }

    operator fun minus(decrement: Money): Money {
        return Money(value - decrement.value)
    }
}