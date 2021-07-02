package com.priyoaujla.domain.components.ordering

import com.priyoaujla.domain.components.checkout.TransactionId

interface OrderStorage: OrderFinder {
    fun upsert(order: Order)
}

interface OrderFinder {
    fun get(orderId: OrderId): Order?
    fun findBy(transactionId: TransactionId): Order?
    fun all(): Set<Order>
}

