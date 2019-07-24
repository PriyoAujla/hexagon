package com.priyoaujla

import com.priyoaujla.TestData.Ingredients.basil
import com.priyoaujla.TestData.Ingredients.mozzarella
import com.priyoaujla.TestData.Ingredients.pizzaDough
import com.priyoaujla.TestData.Ingredients.tomatoSauce
import org.junit.Assert
import java.time.Instant
import java.util.*

class Scenario {

    private val menuHub = MenuHub(InMemoryMenuStorage())
        .apply {
            TestData.minimalMenu.items.forEach {
                this.createItem(it.item, it.price)
            }
        }

    private val pizzaHub = PizzaHub(
        customer = connorTheCustomer,
        theMenu = { menuHub.fetch() },
        orders = Orders(InMemoryOrderStorage()),
        startBaking = {
            kitchenHub.queue(it)
        }
    )


    private val kitchenHub = KitchenHub(
        chef = cathyTheChef,
        ticketStorage = InMemoryTicketStorage()
    )


    private val paypal = FakePaypal()

    fun newCustomer(): CustomerRole = CustomerRole(pizzaHub, paypal)

    fun newChef(): ChefRole = ChefRole(kitchenHub)
}

class ChefRole(
    private val kitchenHub: KitchenHub
) {

    fun hasTickets(vararg tickets: Ticket) {
        Assert.assertEquals(tickets.asList(), kitchenHub.tickets().toList())
    }

    fun canPickupNextTicket(ticket: Ticket) {
        Assert.assertEquals(ticket, kitchenHub.nextTicket())
    }

    fun canFinishCooking(ticket: Ticket) {
        kitchenHub.ticketComplete(ticket)
        Assert.assertEquals(Instant.EPOCH, kitchenHub.ticketFor(ticket.orderId)?.completedAt)
    }

}

class CustomerRole(
    private val pizzaHub: PizzaHub,
    private val paypal: Paypal
) {

    fun canSeeMenuWith(menuItems: Set<Menu.MenuItem>) {
        val menu = pizzaHub.menu()
        Assert.assertEquals(menu.items, menuItems)
    }

    fun canOrder(items: List<Menu.MenuItem>, expectedTotal: Money): Bill {
        val bill = pizzaHub.order(items)
        Assert.assertEquals(bill.amount, expectedTotal)
        return bill
    }

    fun canPay(bill: Bill, paymentType: PaymentType) {
        val paymentInstructions = pizzaHub.payment(paymentType, bill)
        val paymentId = paypal.pay(paymentInstructions.bill.order.id, paymentInstructions.bill.amount)
        pizzaHub.paymentConfirmation(paymentInstructions.bill.order.id, paymentId)
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

    override fun add(order: Order) {
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