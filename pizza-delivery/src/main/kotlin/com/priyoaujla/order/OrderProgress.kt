package com.priyoaujla.order

class OrderProgress(private val orderStatusStorage: OrderStatusStorage) {

    fun update(orderId: OrderId, newStatus: OrderStatus.Status) {
        val order = orderStatusStorage.get(orderId)
        order?.let {
            orderStatusStorage.upsert(it.copy(status = newStatus))
        } ?: error("")
    }
}