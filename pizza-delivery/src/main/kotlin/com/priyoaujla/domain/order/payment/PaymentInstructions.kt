package com.priyoaujla.domain.order.payment

import com.priyoaujla.domain.order.Order

sealed class PaymentInstructions {
    abstract val order: Order
    data class RedirectToPaypal(override val order: Order) : PaymentInstructions()
    data class NoInstructions(override val order: Order): PaymentInstructions()
}