package com.priyoaujla.order

import com.priyoaujla.kitchen.Ticket
import com.priyoaujla.menu.Menu
import java.util.*

class Ordering(
        private val customer: UserDetails,
        private val theMenu: TheMenu,
        private val orderStorage: OrderStorage,
        private val startBaking: StartBaking
) {

    fun menu(): Menu {
        return theMenu()
    }

    fun order(items: List<Menu.MenuItem>): Order {
        val order = items.fold(Order(total = Money(0.0), status = Order.Status.New)) { order, menuItem ->
            order.copy(
                total = order.total + menuItem.price,
                items = order.items + menuItem
            )
        }
        orderStorage.upsert(order)
        return order
    }

    fun retrieve(orderId: OrderId): Order? {
        return orderStorage.get(orderId)
    }

    fun payment(paymentType: PaymentType, order: Order): PaymentInstructions {
        return when(paymentType) {
            PaymentType.Paypal -> PaymentInstructions.RedirectToPaypal(order)
        }
    }

    fun paymentConfirmed(orderId: OrderId, paymentId: PaymentId) {
        val order = orderStorage.get(orderId)
        order?.let {
            orderStorage.upsert(it.copy(status = Order.Status.Paid))
            startBaking(order)
        } ?: error("")
    }

}

class OrderProgress(private val orderStorage: OrderStorage) {

    fun update(orderId: OrderId, newStatus: Order.Status) {
        val order = orderStorage.get(orderId)
        order?.let {
            orderStorage.upsert(it.copy(status = newStatus))
        } ?: error("")
    }
}

interface Paypal {
    fun pay(orderId: OrderId, total: Money): PaymentId
}

typealias TheMenu = () -> Menu
typealias StartBaking = (Order) -> Unit

sealed class PaymentInstructions {
    abstract val order: Order
    data class RedirectToPaypal(override val order: Order) : PaymentInstructions()
}

data class PaymentId(val value: String)

enum class PaymentType {
    Paypal
}

data class UserDetails(
        val userId: UserId,
        val givenName: GivenName,
        val familyName: FamilyName,
        val address: Address
)

data class UserId(val value: UUID) {
    companion object {
        fun mint(): UserId {
            return UserId(UUID.randomUUID())
        }
    }
}
data class GivenName(val value: String)
data class FamilyName(val value: String)
data class Address(val value: String)

data class Order(
        val id: OrderId = OrderId.mint(),
        val items: List<Menu.MenuItem> = emptyList(),
        val total: Money,
        val status: Status
) {
    enum class Status {
        New, Paid, Cooked, Delivered
    }
}
data class OrderId(val uuid: UUID) {
    companion object {
        fun mint(): OrderId {
            return OrderId(UUID.randomUUID())
        }
    }
}

interface OrderStorage {
    fun upsert(order: Order)
    fun get(orderId: OrderId): Order?
}

class Orders(private val orderStorage: OrderStorage) {
    fun get(orderId: OrderId): Order? = orderStorage.get(orderId)
}

data class Money(val value: Double) {
    operator fun plus(increment: Money): Money {
        return Money(value + increment.value)
    }

    operator fun minus(decrement: Money): Money {
        return Money(value - decrement.value)
    }
}

fun toTicket(order: Order): Ticket = Ticket(order.id, order.items.map { it.item })