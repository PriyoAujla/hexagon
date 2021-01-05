package com.priyoaujla.domain.components.order

interface OrderStorage: OrderFinder {
    fun upsert(order: Order)
}

interface OrderFinder {
    fun get(orderId: OrderId): Order?
}

