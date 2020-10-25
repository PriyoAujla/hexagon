package com.priyoaujla.domain.kitchen

import com.priyoaujla.domain.delivery.DeliveryNote
import com.priyoaujla.domain.order.OrderId
import com.priyoaujla.domain.order.OrderStatus
import com.priyoaujla.domain.order.Orders
import java.time.Clock

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
        val updateOrderStatus: (OrderId, OrderStatus.Status) -> Unit,
        val createDelivery: CreateDelivery
) : (Ticket) -> Unit {
    override fun invoke(ticket: Ticket) {
        updateOrderStatus(ticket.orderId, OrderStatus.Status.Cooked)
        createDelivery(ticket)
    }
}

class CreateDelivery(private val orders: Orders, private val sendToDelivery: (DeliveryNote) -> Unit) : (Ticket) -> Unit {
    override fun invoke(ticket: Ticket) {
        val order = orders.get(ticket.orderId) ?: error("Order not found")
        sendToDelivery(DeliveryNote(orderId = ticket.orderId, menuItem = order.items, total = order.total))
    }
}