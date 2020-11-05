package com.priyoaujla.domain.order

class OrderTracking(private val orderStatusStorage: OrderStatusStorage) {

    fun of(orderId: OrderId): OrderStatus? {
        return orderStatusStorage.get(orderId)
    }
}