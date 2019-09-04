package com.priyoaujla

import com.priyoaujla.TestData.Ingredients.basil
import com.priyoaujla.TestData.Ingredients.mozzarella
import com.priyoaujla.TestData.Ingredients.pizzaDough
import com.priyoaujla.TestData.Ingredients.tomatoSauce
import org.junit.Assert.assertEquals
import java.time.Instant
import java.util.*

class Scenario {

    private val menuHub = MenuHub(InMemoryMenuStorage())
        .apply {
            TestData.minimalMenu.items.forEach {
                this.createItem(it.item, it.price)
            }
        }

    private val orders = Orders(InMemoryOrderStorage())
    private val orderHub = OrderingHub(
        customer = connorTheCustomer,
        theMenu = { menuHub.fetch() },
        orders = orders,
        startBaking = {
            kitchenHub.createTicket(it)
        }
    )

    private val kitchenHub = KitchenHub(
        chef = cathyTheChef,
        ticketStorage = InMemoryTicketStorage(),
        orders = orders,
        startDelivery = {
            deliveryHub.createDeliveryNote(it)
        }
    )

    private val deliveryHub = DeliveryHub(
        courier = carmenTheCourier,
        orders = orders,
        deliveries = Deliveries(InMemoryDeliveryStorage()),
        pickup = { error("") }
    )

    private val paypal = FakePaypal()

    fun newCustomer(): CustomerRole = CustomerRole(orderHub, paypal)

    fun newChef(): ChefRole = ChefRole(kitchenHub)

    fun newCourier(): CourierRole = CourierRole(deliveryHub)
}

class CourierRole(
    private val deliveryHub: DeliveryHub
){
    fun theNextDeliveryIs(order: Order): Delivery {
        val nextOrderToDeliver = deliveryHub.nextDelivery()
        val delivery = Delivery.from(order)
        assertEquals(nextOrderToDeliver, delivery)
        return delivery
    }

    fun hasDelivered(delivery: Delivery) {
        deliveryHub.delivered(delivery)
    }
}

class ChefRole(
    private val kitchenHub: KitchenHub
) {

    fun hasTickets(vararg tickets: Ticket) {
        assertEquals(tickets.asList(), kitchenHub.tickets().toList())
    }

    fun canPickupNextTicket(ticket: Ticket) {
        assertEquals(ticket, kitchenHub.nextTicket())
    }

    fun canFinishCooking(ticket: Ticket) {
        kitchenHub.ticketComplete(ticket)
        assertEquals(Instant.EPOCH, kitchenHub.ticketFor(ticket.orderId)?.completedAt)
    }
}

class CustomerRole(
    private val orderingHub: OrderingHub,
    private val paypal: Paypal
) {

    fun canSeeMenuWith(menuItems: Set<Menu.MenuItem>) {
        val menu = orderingHub.menu()
        assertEquals(menu.items, menuItems)
    }

    fun canOrder(items: List<Menu.MenuItem>, expectedTotal: Money): Order {
        val order = orderingHub.order(items)
        assertEquals(order.total, expectedTotal)
        return order
    }

    fun canPay(order: Order, paymentType: PaymentType) {
        val paymentInstructions = orderingHub.payment(paymentType, order)
        val paymentId = paypal.pay(paymentInstructions.order.id, paymentInstructions.order.total)
        orderingHub.paymentConfirmation(paymentInstructions.order.id, paymentId)
    }

    fun canSeeOrderStatus(orderId: OrderId, status: Order.Status){
        assertEquals(status, orderingHub.retrieve(orderId)?.status)
    }
}

object TestData {

    object Ingredients {
        val pizzaDough = Menu.Ingredient("Pizza Dough")
        val tomatoSauce = Menu.Ingredient("Tomato Sauce")
        val mozzarella = Menu.Ingredient("Mozzarella Cheese")
        val basil = Menu.Ingredient("Basil")
    }

    object Pizzas {
        val plainPizza =
            Menu.Item(Menu.Name("Plain Pizza"), listOf(pizzaDough, tomatoSauce))
        val margarita = plainPizza
            .withName("Margarita Pizza")
            .addIngredient(mozzarella)
            .addIngredient(basil)
    }

    private fun Menu.Item.withName(name: String): Menu.Item {
        return copy(name = Menu.Name(name))
    }

    private fun Menu.Item.addIngredient(ingredient: Menu.Ingredient): Menu.Item {
        return copy(ingredients = ingredients + ingredient)
    }

    val minimalMenu = Menu(
        items = setOf(
            Menu.MenuItem(Pizzas.plainPizza, Money(3.99)),
            Menu.MenuItem(Pizzas.margarita, Money(4.99))
        )
    )
}

class FakePaypal: Paypal {

    override fun pay(orderId: OrderId, total: Money): PaymentId {
        return PaymentId(UUID.randomUUID().toString())
    }
}

class InMemoryTicketStorage: TicketStorage {
    private val storage = mutableSetOf<Ticket>()

    override fun add(ticket: Ticket) {
        storage.add(ticket)
    }

    override fun list(): Set<Ticket> {
        return storage
    }

    override fun take(): Ticket = storage.first()

    override fun update(ticket: Ticket) {
        storage.removeIf {it.orderId == ticket.orderId}
        storage.add(ticket)
    }

    override fun findBy(orderId: OrderId): Ticket? = storage.find { it.orderId == orderId }
}

class InMemoryOrderStorage: OrderStorage {
    private val storage = mutableSetOf<Order>()

    override fun upsert(order: Order) {
        storage.removeIf { it.id == order.id}
        storage.add(order)
    }

    override fun get(orderId: OrderId): Order? = storage.find { it.id == orderId }
}

class InMemoryMenuStorage : MenuStorage {
    private val storage = mutableSetOf<Menu.MenuItem>()

    override fun get(): Menu {
        return Menu(storage)
    }

    override fun add(item: Menu.Item, price: Money) {
        storage.add(Menu.MenuItem(item, price))
    }
}

class InMemoryDeliveryStorage: DeliveryStorage {
    private val storage = mutableSetOf<Delivery>()

    override fun upsert(delivery: Delivery) {
        storage.add(delivery)
    }

    override fun take(): Delivery = storage.first()
}

val cathyTheChef = UserDetails(
    UserId.mint(),
    GivenName("Cathy"),
    FamilyName("UserDetails"),
    Address("Some address")
)
val connorTheCustomer = UserDetails(
    UserId.mint(),
    GivenName("Connor"),
    FamilyName("UserDetails"),
    Address("Some address")
)
val carmenTheCourier = UserDetails(
    UserId.mint(),
    GivenName("Carmen"),
    FamilyName("UserDetails"),
    Address("Some address")
)

inline fun <T, R> T.runTest(block: T.() -> R) {
    block()
}