package com.priyoaujla

import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class KitchenHub(
    private val chef: UserDetails,
    private val ticketStorage: TicketStorage,
    private val clock: Clock = Clock.fixed(Instant.EPOCH, ZoneId.of("UTC"))
) {

    fun queue(order: Order) {
        ticketStorage.add(Ticket.from(order))
    }

    fun tickets(): Set<Ticket> = ticketStorage.list()

    fun ticketFor(orderId: OrderId): Ticket? = ticketStorage.findBy(orderId)

    fun nextTicket(): Ticket = ticketStorage.take()

    fun ticketComplete(ticket: Ticket) {
        ticketStorage.update(ticket.copy(completedAt = clock.instant()))
    }
}

data class Ticket(
    val orderId: OrderId,
    val items: List<Menu.Item> = emptyList(),
    val completedAt: Instant? = null
) {
    companion object {
        fun from(order: Order): Ticket =
            Ticket(order.id, order.items.map { it.item })
    }
}

interface TicketStorage {
    fun add(ticket: Ticket)

    fun list(): Set<Ticket>

    fun take(): Ticket

    fun update(ticket: Ticket)

    fun findBy(orderId: OrderId): Ticket?
}