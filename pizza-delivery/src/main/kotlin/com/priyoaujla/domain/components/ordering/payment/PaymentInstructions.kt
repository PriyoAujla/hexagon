package com.priyoaujla.domain.components.ordering.payment

import com.priyoaujla.domain.components.ordering.Order

sealed class PaymentInstructions {
    abstract val order: Order
    data class RedirectToPaypal(override val order: Order) : PaymentInstructions()
    data class NoInstructions(override val order: Order): PaymentInstructions()
}