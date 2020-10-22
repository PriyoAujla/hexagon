package com.priyoaujla.kitchen

import com.priyoaujla.order.OrderId

interface TicketStorage {
    fun add(ticket: Ticket)

    fun list(): Set<Ticket>

    fun take(): Ticket

    fun update(ticket: Ticket)

    fun findBy(orderId: OrderId): Ticket?
}