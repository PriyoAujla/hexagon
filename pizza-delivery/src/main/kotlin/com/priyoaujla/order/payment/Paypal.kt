package com.priyoaujla.order.payment

import com.priyoaujla.order.Money
import com.priyoaujla.order.OrderId

interface Paypal {
    fun pay(orderId: OrderId, total: Money): PaymentId
}