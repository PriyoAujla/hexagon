package com.priyoaujla.domain.order

import com.priyoaujla.transaction.Transactor

class Orders(private val orderStorageTransactor: Transactor<OrderStorage>) {
    fun get(orderId: OrderId): Order? = orderStorageTransactor.perform { it.get(orderId) }
}