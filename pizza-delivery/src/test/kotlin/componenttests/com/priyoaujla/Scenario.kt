package componenttests.com.priyoaujla

import com.priyoaujla.delivery.*
import com.priyoaujla.kitchen.*
import com.priyoaujla.menu.Menu
import com.priyoaujla.menu.TheMenu
import com.priyoaujla.menu.MenuStorage
import com.priyoaujla.order.*
import com.priyoaujla.order.payment.PaymentId
import com.priyoaujla.order.payment.PaymentType
import com.priyoaujla.order.payment.Paypal
import componenttests.com.priyoaujla.TestData.Ingredients.basil
import componenttests.com.priyoaujla.TestData.Ingredients.mozzarella
import componenttests.com.priyoaujla.TestData.Ingredients.pizzaDough
import componenttests.com.priyoaujla.TestData.Ingredients.tomatoSauce
import org.junit.Assert.*
import java.util.*

class Scenario {

    private val menuHub = TheMenu(InMemoryMenuStorage())
        .apply {
            TestData.minimalMenu.items.forEach {
                this.createItem(it.item, it.price)
            }
        }

    private val orderStorage = InMemoryOrderStorage()

    private val orders = Orders(orderStorage)

    private val ordering = Ordering(
            customer = connorTheCustomer,
            orderStorage = orderStorage,
            startBaking = {
                kitchen.createTicket(toTicket(it))
            }
    )

    private val orderProgress = OrderProgress(orderStorage)

    private val kitchen = Kitchen(
            ticketStorage = InMemoryTicketStorage(),
            notifyTicketComplete = NotifyTicketComplete(
                    updateOrderStatus = { orderId, status -> orderProgress.update(orderId, status) },
                    createDelivery = CreateDelivery(orders) {
                        delivery.newDelivery(it)
                    }
            )
    )

    private val delivery = Delivery(
            deliveryStorage = InMemoryDeliveryStorage(),
            notifyDelivered = NotifyDelivered { orderId, status ->
                orderProgress.update(orderId, status)
            }
    )

    private val paypal = FakePaypal()

    fun newCustomer(): CustomerRole =
        CustomerRole(menuHub, ordering, paypal)

    fun newChef(): ChefRole =
        ChefRole(kitchen)

    fun newCourier(): CourierRole =
        CourierRole(delivery)
}

class CourierRole(
    private val delivery: Delivery
){
    fun theNextDeliveryIs(order: Order): DeliveryNote {
        val nextOrderToDeliver = delivery.nextDelivery()
        assertEquals(nextOrderToDeliver.orderId, order.id)
        assertEquals(nextOrderToDeliver.menuItem, order.items)
        assertEquals(nextOrderToDeliver.total, order.total)
        return nextOrderToDeliver
    }

    fun hasDelivered(delivery: DeliveryNote) {
        this.delivery.delivered(delivery.id)
    }
}

class ChefRole(
    private val kitchen: Kitchen
) {

    fun hasTickets(vararg tickets: Ticket) {
        tickets.forEach {
            assertEquals(it, kitchen.nextTicket())
        }
    }

    fun canPickupNextTicket(ticket: Ticket) {
        assertEquals(ticket, kitchen.nextTicket())
    }

    fun canFinishCooking(ticket: Ticket) {
        kitchen.ticketComplete(ticket)
        assertFalse(kitchen.tickets().toList().contains(ticket))
    }
}

class CustomerRole(
        private val theMenu: TheMenu,
        private val ordering: Ordering,
        private val paypal: Paypal
) {

    fun canSeeMenuWith(menuItems: Set<Menu.MenuItem>) {
        val menu = theMenu.fetch()
        assertEquals(menu.items, menuItems)
    }

    fun canOrder(items: List<Menu.MenuItem>, expectedTotal: Money): Order {
        val order = ordering.order(items)
        assertEquals(order.total, expectedTotal)
        return order
    }

    fun canPay(order: Order, paymentType: PaymentType) {
        val paymentInstructions = ordering.payment(paymentType, order)
        val paymentId = paypal.pay(paymentInstructions.order.id, paymentInstructions.order.total)
        ordering.paymentConfirmed(paymentInstructions.order.id, paymentId)
    }

    fun canSeeOrderStatus(orderId: OrderId, status: Order.Status){
        assertEquals(status, ordering.retrieve(orderId)?.status)
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
            Menu.Item(
                Menu.Name("Plain Pizza"),
                listOf(pizzaDough, tomatoSauce)
            )
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
                    Menu.MenuItem(
                            Pizzas.plainPizza,
                            Money(3.99)
                    ),
                    Menu.MenuItem(
                            Pizzas.margarita,
                            Money(4.99)
                    )
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
    private val storage = mutableMapOf<DeliveryId, DeliveryNote>()

    override fun get(id: DeliveryId): DeliveryNote? {
        return storage[id]
    }

    override fun upsert(deliveryNote: DeliveryNote) {
        storage += deliveryNote.id to deliveryNote
    }

    override fun take(): DeliveryNote = storage.toList().first().second
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