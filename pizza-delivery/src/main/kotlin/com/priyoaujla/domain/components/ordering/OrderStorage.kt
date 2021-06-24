package com.priyoaujla.domain.components.ordering

interface OrderStorage: OrderFinder {
    fun upsert(order: Order)
}

interface OrderFinder {
    fun get(orderId: OrderId): Order?
}

