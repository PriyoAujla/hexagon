package com.priyoaujla.order

interface OrderStorage {
    fun upsert(order: Order)
    fun get(orderId: OrderId): Order?
}

