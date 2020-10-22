package com.priyoaujla.order

import com.priyoaujla.kitchen.Ticket
import com.priyoaujla.menu.Menu
import com.priyoaujla.order.payment.PaymentId
import com.priyoaujla.order.payment.PaymentInstructions
import com.priyoaujla.order.payment.PaymentType

class Ordering(
        private val orderStorage: OrderStorage,
        private val orderStatusStorage: OrderStatusStorage,
        private val startBaking: StartBaking
) {

    fun order(items: List<Menu.MenuItem>): Order {
        val order = items.fold(Order(total = Money(0.0))) { order, menuItem ->
            order.copy(
                total = order.total + menuItem.price,
                items = order.items + menuItem
            )
        }
        orderStorage.upsert(order)
        orderStatusStorage.upsert(OrderStatus(order.id, OrderStatus.Status.New))
        return order
    }

    fun retrieve(orderId: OrderId): Order? {
        return orderStorage.get(orderId)
    }

    fun payment(paymentType: PaymentType, order: Order): PaymentInstructions {
        return when(paymentType) {
            PaymentType.Paypal -> PaymentInstructions.RedirectToPaypal(order)
            PaymentType.Cash -> PaymentInstructions.NoInstructions(order)
        }
    }

    fun paymentConfirmed(orderId: OrderId, paymentId: PaymentId) {
        val order = orderStorage.get(orderId)
        order?.let {
            orderStatusStorage.upsert(OrderStatus(order.id, OrderStatus.Status.Paid))
            startBaking(order)
        } ?: error("")
    }

}

typealias StartBaking = (Order) -> Unit

fun toTicket(order: Order): Ticket = Ticket(order.id, order.items.map { it.item })