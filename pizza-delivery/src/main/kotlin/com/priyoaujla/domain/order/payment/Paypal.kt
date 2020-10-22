package com.priyoaujla.domain.order.payment

import com.priyoaujla.domain.order.Money
import com.priyoaujla.domain.order.OrderId

interface Paypal {
    fun pay(orderId: OrderId, total: Money): PaymentId
}