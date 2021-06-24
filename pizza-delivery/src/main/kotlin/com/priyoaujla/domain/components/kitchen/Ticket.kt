package com.priyoaujla.domain.components.kitchen

import com.priyoaujla.domain.components.menu.Menu
import com.priyoaujla.domain.components.ordering.OrderId
import java.time.Instant

data class Ticket(
        val orderId: OrderId,
        val items: List<Menu.Item> = emptyList(),
        val completedAt: Instant? = null
)