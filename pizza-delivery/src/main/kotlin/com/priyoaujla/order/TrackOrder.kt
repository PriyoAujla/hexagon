package com.priyoaujla.order

class TrackOrder(private val orderStatusStorage: OrderStatusStorage) {

    fun statusOf(orderId: OrderId): OrderStatus? {
        return orderStatusStorage.get(orderId)
    }
}