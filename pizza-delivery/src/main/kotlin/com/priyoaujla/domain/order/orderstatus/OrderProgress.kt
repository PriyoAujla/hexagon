package com.priyoaujla.domain.order.orderstatus

import com.priyoaujla.domain.order.OrderId

class OrderProgress(private val orderStatusStorage: OrderStatusStorage) {

    fun update(orderId: OrderId, newStatus: OrderStatus.Status) {
        val order = orderStatusStorage.get(orderId)
        order?.let {
            orderStatusStorage.upsert(it.copy(status = newStatus))
        } ?: error("")
    }
}