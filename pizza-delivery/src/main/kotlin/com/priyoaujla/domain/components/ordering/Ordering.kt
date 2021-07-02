package com.priyoaujla.domain.components.ordering

import com.priyoaujla.domain.components.checkout.TransactionId
import com.priyoaujla.domain.components.kitchen.Ticket
import com.priyoaujla.domain.components.menu.Menu
import com.priyoaujla.domain.components.ordering.orderstatus.OrderStatus
import com.priyoaujla.domain.components.ordering.orderstatus.OrderStatusStorage
import com.priyoaujla.domain.components.ordering.payment.PaymentId
import com.priyoaujla.domain.components.ordering.payment.PaymentType
import com.priyoaujla.transaction.Transactor

class Ordering(
    private val transactor: Transactor<Triple<OrderStorage, OrderStatusStorage, NotifyOrderComplete>>
) {

    fun create(
        transactionId: TransactionId,
        items: List<Menu.MenuItem>,
        total: Money,
        paymentType: PaymentType
    ): Order {
        val order = Order(
            transactionId = transactionId,
            total = total,
            items = items,
            paymentType = paymentType,
            paymentStatus = PaymentStatus.PaymentRequired
        )
        transactor.perform { (orderStorage, orderStatusStorage) ->
            orderStorage.upsert(order)
            orderStatusStorage.upsert(
                OrderStatus(
                    order.id,
                    OrderStatus.Status.New
                )
            )
        }

        return order
    }

    fun retrieve(orderId: OrderId): Order? {
        return transactor.perform { (orderStorage) ->
            orderStorage.get(orderId)
        }
    }

    fun paymentConfirmed(transactionId: TransactionId, paymentConfirmationType: PaymentConfirmationType) {
        transactor.perform { (orderStorage, _, notifyOrderComplete) ->
            val order = orderStorage.findBy(transactionId)
            order?.let {
                when(paymentConfirmationType) {
                    is PaymentConfirmationType.Paid -> orderStorage.upsert(it.paid(paymentConfirmationType.paymentId))
                    is PaymentConfirmationType.Cash -> Unit
                }
                notifyOrderComplete(order)
            } ?: error("")
        }
    }

    fun list(): Set<Order> = transactor.perform { (orderStorage) ->
        orderStorage.all()
    }

}

sealed class PaymentConfirmationType {
    data class Paid(val paymentId: PaymentId): PaymentConfirmationType()
    object Cash: PaymentConfirmationType()
}

typealias NotifyOrderComplete = (Order) -> Unit

fun toTicket(order: Order): Ticket = Ticket(order.id, order.items.map { it.item })