package com.priyoaujla.domain.kitchen

import com.priyoaujla.domain.menu.Menu
import com.priyoaujla.domain.order.OrderId
import java.time.Instant

data class Ticket(
        val orderId: OrderId,
        val items: List<Menu.Item> = emptyList(),
        val completedAt: Instant? = null
)