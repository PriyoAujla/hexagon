package com.priyoaujla.domain.order

interface OrderStorage {
    fun upsert(order: Order)
    fun get(orderId: OrderId): Order?
}

