package com.priyoaujla.kitchen

import com.priyoaujla.delivery.DeliveryNote
import com.priyoaujla.menu.Menu
import com.priyoaujla.order.Order
import com.priyoaujla.order.OrderId
import com.priyoaujla.order.Orders
import java.time.Clock
import java.time.Instant

class Kitchen(
        private val ticketStorage: TicketStorage,
        private val clock: Clock = Clock.systemUTC(),
        private val notifyTicketComplete: NotifyTicketComplete
) {

    fun createTicket(ticket: Ticket) = ticketStorage.add(ticket)

    fun tickets(): Set<Ticket> = ticketStorage.list()

    fun nextTicket(): Ticket = ticketStorage.take()

    fun ticketComplete(ticket: Ticket) {
        ticketStorage.update(ticket.copy(completedAt = clock.instant()))
        notifyTicketComplete(ticket)
    }
}

class NotifyTicketComplete(
        val updateOrderStatus: (OrderId, Order.Status) -> Unit,
        val createDelivery: CreateDelivery
) : (Ticket) -> Unit {
    override fun invoke(ticket: Ticket) {
        updateOrderStatus(ticket.orderId, Order.Status.Cooked)
        createDelivery(ticket)
    }
}

class CreateDelivery(private val orders: Orders, private val sendToDelivery: (DeliveryNote) -> Unit) : (Ticket) -> Unit {
    override fun invoke(ticket: Ticket) {
        val order = orders.get(ticket.orderId) ?: error("Implement me!")
        sendToDelivery(DeliveryNote(orderId = ticket.orderId, menuItem = order.items, total = order.total))
    }
}

data class Ticket(
        val orderId: OrderId,
        val items: List<Menu.Item> = emptyList(),
        val completedAt: Instant? = null
)

interface TicketStorage {
    fun add(ticket: Ticket)

    fun list(): Set<Ticket>

    fun take(): Ticket

    fun update(ticket: Ticket)

    fun findBy(orderId: OrderId): Ticket?
}