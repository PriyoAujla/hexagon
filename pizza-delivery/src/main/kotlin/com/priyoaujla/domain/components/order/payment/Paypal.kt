package com.priyoaujla.domain.components.order.payment

import com.priyoaujla.domain.components.order.Money
import com.priyoaujla.domain.components.order.OrderId

interface Paypal {
    fun pay(orderId: OrderId, total: Money): PaymentId
}