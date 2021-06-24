package com.priyoaujla.domain.components.ordering

data class Money(val value: Double) {
    operator fun plus(increment: Money): Money {
        return Money(value + increment.value)
    }

    operator fun minus(decrement: Money): Money {
        return Money(value - decrement.value)
    }

    companion object {
        val ZERO = Money(0.0)
    }
}