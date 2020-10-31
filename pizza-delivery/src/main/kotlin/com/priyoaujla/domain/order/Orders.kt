package com.priyoaujla.domain.order

import com.priyoaujla.transaction.Transactor

class Orders(private val orderStorageTransactor: OrderStorage) {
    fun findBy(orderId: OrderId): Order? = orderStorageTransactor.get(orderId)
}