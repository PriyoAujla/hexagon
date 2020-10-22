package com.priyoaujla.order

interface OrderStatusStorage {
    fun upsert(orderStatus: OrderStatus)
    fun get(orderId: OrderId): OrderStatus?

}

data class OrderStatus(val id: OrderId, val status: Status) {
    enum class Status {
        New, Paid, Cooked, Delivered
    }
}