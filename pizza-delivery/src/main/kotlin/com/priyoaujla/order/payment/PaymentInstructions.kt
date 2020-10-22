package com.priyoaujla.order.payment

import com.priyoaujla.order.Order

sealed class PaymentInstructions {
    abstract val order: Order
    data class RedirectToPaypal(override val order: Order) : PaymentInstructions()
    data class NoInstructions(override val order: Order): PaymentInstructions()
}