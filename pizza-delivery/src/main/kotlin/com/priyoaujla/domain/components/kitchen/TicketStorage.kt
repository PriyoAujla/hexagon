package com.priyoaujla.domain.components.kitchen

import com.priyoaujla.domain.components.order.OrderId

interface TicketStorage {
    fun add(ticket: Ticket)

    fun list(): Set<Ticket>

    fun take(): Ticket

    fun update(ticket: Ticket)

    fun findBy(orderId: OrderId): Ticket?
}