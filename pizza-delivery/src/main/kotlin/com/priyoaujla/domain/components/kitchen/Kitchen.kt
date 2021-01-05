package com.priyoaujla.domain.components.kitchen

import com.priyoaujla.domain.components.delivery.DeliveryNote
import com.priyoaujla.domain.components.order.OrderFinder
import com.priyoaujla.domain.components.order.OrderId
import com.priyoaujla.domain.components.order.PaymentStatus
import com.priyoaujla.domain.components.order.orderstatus.OrderStatus
import com.priyoaujla.transaction.Transactor
import java.time.Clock

class Kitchen(
    private val transactor: Transactor<Pair<TicketStorage, NotifyTicketComplete>>,
    private val clock: Clock = Clock.systemUTC()
) {

    fun createTicket(ticket: Ticket) = transactor.perform { (ticketStorage) ->
        ticketStorage.add(ticket)
    }

    fun tickets(): Set<Ticket> = transactor.perform { (ticketStorage) ->
        ticketStorage.list()
    }

    fun nextTicket(): Ticket = transactor.perform { (ticketStorage) ->
        ticketStorage.take()
    }

    fun ticketComplete(ticket: Ticket) {
        transactor.perform { (ticketStorage, notifyTicketComplete) ->
            ticketStorage.update(ticket.copy(completedAt = clock.instant()))
            notifyTicketComplete(ticket)
        }
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

class CreateDelivery(private val orderFinder: OrderFinder, private val sendToDelivery: (DeliveryNote) -> Unit) : (Ticket) -> Unit {
    override fun invoke(ticket: Ticket) {
        val order = orderFinder.get(ticket.orderId) ?: error("Order not found")
        sendToDelivery(DeliveryNote(
            orderId = ticket.orderId,
            menuItem = order.items,
            total = order.total,
            paymentStatus = when(order.paymentStatus) {
                is PaymentStatus.PaymentRequired -> DeliveryNote.PaymentStatus.PaymentRequired
                is PaymentStatus.Paid -> DeliveryNote.PaymentStatus.Paid
            }
        ))
    }
}