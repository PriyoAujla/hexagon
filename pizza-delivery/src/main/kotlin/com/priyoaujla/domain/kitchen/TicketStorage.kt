package com.priyoaujla.domain.kitchen

import com.priyoaujla.domain.order.OrderId

interface TicketStorage {
    fun add(ticket: Ticket)

    fun list(): Set<Ticket>

    fun take(): Ticket

    fun update(ticket: Ticket)

    fun findBy(orderId: OrderId): Ticket?
}