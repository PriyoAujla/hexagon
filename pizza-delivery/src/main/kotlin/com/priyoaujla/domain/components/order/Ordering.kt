package com.priyoaujla.domain.components.order

import com.priyoaujla.domain.components.kitchen.Ticket
import com.priyoaujla.domain.components.menu.Menu
import com.priyoaujla.domain.components.order.Money.Companion.ZERO
import com.priyoaujla.domain.components.order.orderstatus.OrderStatus
import com.priyoaujla.domain.components.order.orderstatus.OrderStatusStorage
import com.priyoaujla.domain.components.order.payment.PaymentId
import com.priyoaujla.domain.components.order.payment.PaymentInstructions
import com.priyoaujla.domain.components.order.payment.PaymentType
import com.priyoaujla.transaction.Transactor

class Ordering(
    private val transactor: Transactor<Triple<OrderStorage, OrderStatusStorage, NotifyOrderComplete>>
) {

    fun order(items: List<Menu.MenuItem>): Order {
        val order = items.fold(Order(total = ZERO)) { order, menuItem ->
            order.addItem(menuItem)
        }
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

    fun payment(paymentType: PaymentType, order: Order): PaymentInstructions {
        return when (paymentType) {
            PaymentType.Paypal -> PaymentInstructions.RedirectToPaypal(order)
            PaymentType.Cash -> {
                transactor.perform { (_, _, notifyOrderComplete) ->
                    notifyOrderComplete(order)
                    PaymentInstructions.NoInstructions(order)
                }
            }
        }
    }

    fun paymentConfirmed(orderId: OrderId, paymentId: PaymentId) {
        transactor.perform { (orderStorage, _, startBaking) ->
            val order = orderStorage.get(orderId)
            order?.let {
                orderStorage.upsert(it.paid(paymentId))
                startBaking(order)
            } ?: error("")
        }
    }

}

typealias NotifyOrderComplete = (Order) -> Unit

fun toTicket(order: Order): Ticket = Ticket(order.id, order.items.map { it.item })