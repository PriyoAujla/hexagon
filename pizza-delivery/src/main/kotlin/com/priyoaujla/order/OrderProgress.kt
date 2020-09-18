package com.priyoaujla.order

class OrderProgress(private val orderStorage: OrderStorage) {

    fun update(orderId: OrderId, newStatus: Order.Status) {
        val order = orderStorage.get(orderId)
        order?.let {
            orderStorage.upsert(it.copy(status = newStatus))
        } ?: error("")
    }
}