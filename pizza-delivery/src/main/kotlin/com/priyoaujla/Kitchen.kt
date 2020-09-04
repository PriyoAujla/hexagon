package com.priyoaujla

import java.time.Clock
import java.time.Instant

class KitchenHub(
    private val chef: UserDetails,
    private val ticketStorage: TicketStorage,
    private val orders: Orders,
    private val clock: Clock = Clock.systemUTC(),
    private val cookingFinished: CookingFinished
) {

    fun createTicket(order: Order) {
        ticketStorage.add(Ticket.from(order))
    }

    fun tickets(): Set<Ticket> = ticketStorage.list()

    fun nextTicket(): Ticket = ticketStorage.take()

    fun ticketComplete(ticket: Ticket) {
        ticketStorage.update(ticket.copy(completedAt = clock.instant()))
        cookingFinished(ticket)
    }
}

typealias CookingFinished = (Ticket) -> Unit

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