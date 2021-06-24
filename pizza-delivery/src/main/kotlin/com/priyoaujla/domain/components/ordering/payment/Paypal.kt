package com.priyoaujla.domain.components.ordering.payment

import com.priyoaujla.domain.components.ordering.Money
import com.priyoaujla.domain.components.ordering.OrderId

interface Paypal {
    fun pay(orderId: OrderId, total: Money): PaymentId
}