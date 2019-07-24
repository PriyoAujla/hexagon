package com.priyoaujla

import com.priyoaujla.Menu.Item
import java.util.*

class PizzaHub(
    private val customer: UserDetails,
    private val theMenu: TheMenu,
    private val orders: Orders,
    private val startBaking: StartBaking
) {

    fun menu(): Menu {
        return theMenu()
    }

    fun order(items: List<Menu.MenuItem>): Bill {
        val theBill = items.fold(Bill(Money(0.0), Order())) { bill, menuItem ->
            bill.copy(
                amount = bill.amount + menuItem.price,
                order = bill.order.copy(items = bill.order.items + menuItem)
            )
        }
        orders.add(theBill.order)
        return theBill
    }

    fun payment(paymentType: PaymentType, bill: Bill): PaymentInstructions {
        return  when(paymentType) {
            PaymentType.Paypal -> PaymentInstructions.GotoPaypal(bill)
            PaymentType.DirectDebit -> TODO()
            PaymentType.CreditCard -> TODO()
        }
    }

    fun paymentConfirmation(orderId: OrderId, paymentId: PaymentId) {
        val order = orders.get(orderId)
        order?.let {
            startBaking(order)
        } ?: TODO()
    }

}

interface Paypal {

    fun pay(orderId: OrderId, total: Money): PaymentId
}

interface MenuStorage {
    fun get(): Menu

    fun add(item: Item, price: Money)
}

class MenuHub(
    private val menuStorage: MenuStorage
) {

    fun fetch(): Menu = menuStorage.get()

    fun createItem(item: Item, price: Money) {
        menuStorage.add(item, price)
    }
}

typealias TheMenu = () -> Menu
typealias StartBaking = (Order) -> Unit

sealed class PaymentInstructions {
    abstract val bill: Bill
    data class GotoPaypal(override val bill: Bill) : PaymentInstructions()
}

data class PaymentId(val value: String)

enum class PaymentType {
    DirectDebit, CreditCard, Paypal
}

data class Bill(val amount: Money, val order: Order)

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

data class Order(val id: OrderId = OrderId.mint(), val items: List<Menu.MenuItem> = emptyList())
data class OrderId(val uuid: UUID) {
    companion object {
        fun mint(): OrderId {
            return OrderId(UUID.randomUUID())
        }
    }
}


interface OrderStorage {

    fun add(order: Order)
    fun get(orderId: OrderId): Order?
}

class Orders(private val orderStorage: OrderStorage) {

    fun add(order: Order) {
        orderStorage.add(order)
    }

    fun get(orderId: OrderId): Order? = orderStorage.get(orderId)

}

data class Money(val value: Double) {
    operator fun plus(increment: Money): Money {
        return Money(value + increment.value)
    }

    operator fun minus(increment: Money): Money {
        return Money(value - increment.value)
    }
}
data class Menu(val items: Set<MenuItem>) {
    data class MenuItem(val item: Item, val price: Money)
    data class Item(val name: Name, val ingredients: List<Ingredient>)
    data class Name(val value: String)
    data class Ingredient(val value: String)
}