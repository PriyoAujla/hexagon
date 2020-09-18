package com.priyoaujla.order

class Orders(private val orderStorage: OrderStorage) {
    fun get(orderId: OrderId): Order? = orderStorage.get(orderId)
}