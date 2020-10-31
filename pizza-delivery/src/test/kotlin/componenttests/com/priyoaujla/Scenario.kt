package componenttests.com.priyoaujla

import com.priyoaujla.domain.delivery.*
import com.priyoaujla.domain.kitchen.*
import com.priyoaujla.domain.menu.Menu
import com.priyoaujla.domain.menu.TheMenu
import com.priyoaujla.domain.menu.MenuStorage
import com.priyoaujla.domain.order.*
import com.priyoaujla.domain.order.payment.PaymentId
import com.priyoaujla.domain.order.payment.PaymentType
import com.priyoaujla.domain.order.payment.Paypal
import com.priyoaujla.transaction.Transactor
import componenttests.com.priyoaujla.TestData.Ingredients.basil
import componenttests.com.priyoaujla.TestData.Ingredients.mozzarella
import componenttests.com.priyoaujla.TestData.Ingredients.pizzaDough
import componenttests.com.priyoaujla.TestData.Ingredients.tomatoSauce
import componenttests.com.priyoaujla.transaction.InMemoryTransactor
import componenttests.com.priyoaujla.transaction.IsCloneable
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import java.util.*

class Scenario {

    private val menuHub = TheMenu(InMemoryMenuStorage())
        .apply {
            TestData.minimalMenu.items.forEach {
                this.createItem(it.item, it.price)
            }
        }

    private val orderStorage: InMemoryOrderStorage = InMemoryOrderStorage()

    private val orderStatusStorage = InMemoryOrderStatusStorage()

    private val orderStorageTransactor: Transactor<OrderStorage> = InMemoryTransactor { orderStorage }

    private val orders = Orders(orderStorageTransactor)

    private val startBaking: StartBaking = {
        kitchen.createTicket(toTicket(it))
    }

    private val ordering = Ordering(transactor = InMemoryTransactor {
        Triple(
            orderStorage,
            orderStatusStorage,
            startBaking
        )
    })

    private val orderProgress = OrderProgress(orderStatusStorage)

    private val ticketStorage = InMemoryTicketStorage()

    private val kitchen = Kitchen(
        transactor = InMemoryTransactor {
            ticketStorage to NotifyTicketComplete(
                updateOrderStatus = { orderId, status -> orderProgress.update(orderId, status) },
                createDelivery = CreateDelivery(orders) { delivery.newDelivery(it) }
            )
        })


    private val deliveryStorage = InMemoryDeliveryStorage()
    private val notifyDelivered = NotifyDelivered { orderId, status ->
        orderProgress.update(orderId, status)
    }

    private val delivery = Delivery(
        transactor = InMemoryTransactor {
            deliveryStorage to notifyDelivered
        }
    )

    private val trackingOrder = TrackOrder(orderStatusStorage)

    private val paypal = FakePaypal()

    fun newCustomer(): CustomerRole =
        CustomerRole(menuHub, ordering, paypal, trackingOrder)

    fun newChef(): ChefRole =
        ChefRole(kitchen)

    fun newCourier(): CourierRole =
        CourierRole(delivery)
}

class CourierRole(
    private val delivery: Delivery
) {
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
    private val paypal: Paypal,
    private val trackOrder: TrackOrder
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

    fun canSeeOrderStatus(orderId: OrderId, status: OrderStatus.Status) {
        assertEquals(status, trackOrder.statusOf(orderId)?.status)
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

class FakePaypal : Paypal {

    override fun pay(orderId: OrderId, total: Money): PaymentId {
        return PaymentId(UUID.randomUUID().toString())
    }
}

class InMemoryTicketStorage(
    private val storage: MutableSet<Ticket> = mutableSetOf()
) : TicketStorage, IsCloneable<InMemoryTicketStorage> {

    override fun add(ticket: Ticket) {
        storage.add(ticket)
    }

    override fun list(): Set<Ticket> {
        return storage
    }

    override fun take(): Ticket = storage.first()

    override fun update(ticket: Ticket) {
        storage.removeIf { it.orderId == ticket.orderId }
        storage.add(ticket)
    }

    override fun findBy(orderId: OrderId): Ticket? = storage.find { it.orderId == orderId }

    override fun clone(): InMemoryTicketStorage = InMemoryTicketStorage(mutableSetOf(*storage.toTypedArray()))
}

class InMemoryOrderStatusStorage(
    private val storage: MutableSet<OrderStatus> = mutableSetOf()
) : OrderStatusStorage, IsCloneable<InMemoryOrderStatusStorage> {

    override fun upsert(orderStatus: OrderStatus) {
        storage.removeIf { it.id == orderStatus.id }
        storage.add(orderStatus)
    }

    override fun get(orderId: OrderId): OrderStatus? = storage.find { it.id == orderId }

    override fun clone(): InMemoryOrderStatusStorage = InMemoryOrderStatusStorage(mutableSetOf(*storage.toTypedArray()))
}

data class InMemoryOrderStorage(
    private val storage: MutableSet<Order> = mutableSetOf()
) : OrderStorage, IsCloneable<InMemoryOrderStorage> {

    override fun upsert(order: Order) {
        storage.removeIf { it.id == order.id }
        storage.add(order)
    }

    override fun get(orderId: OrderId): Order? = storage.find { it.id == orderId }

    override fun clone(): InMemoryOrderStorage = InMemoryOrderStorage(mutableSetOf(*storage.toTypedArray()))
}

class InMemoryMenuStorage(
    private val storage: MutableSet<Menu.MenuItem> = mutableSetOf()
) : MenuStorage, IsCloneable<InMemoryMenuStorage> {


    override fun get(): Menu {
        return Menu(storage)
    }

    override fun add(item: Menu.Item, price: Money) {
        storage.add(Menu.MenuItem(item, price))
    }

    override fun clone(): InMemoryMenuStorage = InMemoryMenuStorage(mutableSetOf(*storage.toTypedArray()))
}

class InMemoryDeliveryStorage(
    private val storage: MutableMap<DeliveryId, DeliveryNote> = mutableMapOf<DeliveryId, DeliveryNote>()
) : DeliveryStorage, IsCloneable<InMemoryDeliveryStorage> {

    override fun get(id: DeliveryId): DeliveryNote? {
        return storage[id]
    }

    override fun upsert(deliveryNote: DeliveryNote) {
        storage += deliveryNote.id to deliveryNote
    }

    override fun take(): DeliveryNote = storage.toList().first().second

    override fun clone(): InMemoryDeliveryStorage =
        InMemoryDeliveryStorage(mutableMapOf(*storage.toList().toTypedArray()))
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