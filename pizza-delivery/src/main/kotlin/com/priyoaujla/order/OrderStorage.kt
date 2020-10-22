package com.priyoaujla.order

interface OrderStorage {
    fun upsert(order: Order)
    fun get(orderId: OrderId): Order?
}

interface OrderStatusStorage {
    fun upsert(orderStatus: OrderStatus)
    fun get(orderId: OrderId): OrderStatus?

}