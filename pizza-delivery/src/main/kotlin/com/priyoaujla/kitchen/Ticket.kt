package com.priyoaujla.kitchen

import com.priyoaujla.menu.Menu
import com.priyoaujla.order.OrderId
import java.time.Instant

data class Ticket(
        val orderId: OrderId,
        val items: List<Menu.Item> = emptyList(),
        val completedAt: Instant? = null
)