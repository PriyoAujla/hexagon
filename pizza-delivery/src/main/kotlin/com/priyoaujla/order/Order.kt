package com.priyoaujla.order

import com.priyoaujla.menu.Menu
import java.util.*

data class Order(
        val id: OrderId = OrderId.mint(),
        val items: List<Menu.MenuItem> = emptyList(),
        val total: Money
)

data class OrderId(val uuid: UUID) {
    companion object {
        fun mint(): OrderId {
            return OrderId(UUID.randomUUID())
        }
    }
}

data class OrderStatus(val id: OrderId, val status: Status) {
    enum class Status {
        New, Paid, Cooked, Delivered
    }
}