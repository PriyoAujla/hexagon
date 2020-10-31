package com.priyoaujla.domain.order

import com.priyoaujla.domain.kitchen.Ticket
import com.priyoaujla.domain.menu.Menu
import com.priyoaujla.domain.order.payment.PaymentId
import com.priyoaujla.domain.order.payment.PaymentInstructions
import com.priyoaujla.domain.order.payment.PaymentType
import com.priyoaujla.transaction.Transactor

class Ordering(
    private val transactor: Transactor<Triple<OrderStorage, OrderStatusStorage, StartBaking>>
) {

    fun order(items: List<Menu.MenuItem>): Order {
        val order = items.fold(Order(total = Money(0.0))) { order, menuItem ->
            order.copy(
                total = order.total + menuItem.price,
                items = order.items + menuItem
            )
        }
        transactor.perform { (orderStorage, orderStatusStorage) ->
            orderStorage.upsert(order)
            orderStatusStorage.upsert(OrderStatus(order.id, OrderStatus.Status.New))
        }

        return order
    }

    fun retrieve(orderId: OrderId): Order? {
        return transactor.perform { (orderStorage) ->
            orderStorage.get(orderId)
        }
    }

    fun payment(paymentType: PaymentType, order: Order): PaymentInstructions {
        return when(paymentType) {
            PaymentType.Paypal -> PaymentInstructions.RedirectToPaypal(order)
            PaymentType.Cash -> PaymentInstructions.NoInstructions(order)
        }
    }

    fun paymentConfirmed(orderId: OrderId, paymentId: PaymentId) {
        transactor.perform { (orderStorage, orderStatusStorage, startBaking) ->
            val order = orderStorage.get(orderId)
            order?.let {
                orderStatusStorage.upsert(OrderStatus(order.id, OrderStatus.Status.Paid))
                startBaking(order)
            } ?: error("")
        }
    }

}

typealias StartBaking = (Order) -> Unit

fun toTicket(order: Order): Ticket = Ticket(order.id, order.items.map { it.item })