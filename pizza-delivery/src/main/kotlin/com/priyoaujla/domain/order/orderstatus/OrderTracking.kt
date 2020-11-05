package com.priyoaujla.domain.order.orderstatus

import com.priyoaujla.domain.order.OrderId

class OrderTracking(private val orderStatusStorage: OrderStatusStorage) {

    fun of(orderId: OrderId): OrderStatus? {
        return orderStatusStorage.get(orderId)
    }
}