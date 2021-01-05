package componenttests.com.priyoaujla

import com.priyoaujla.domain.components.delivery.*
import com.priyoaujla.domain.components.kitchen.*
import com.priyoaujla.domain.components.menu.Menu
import com.priyoaujla.domain.components.menu.MenuStorage
import com.priyoaujla.domain.components.menu.TheMenu
import com.priyoaujla.domain.components.order.*
import com.priyoaujla.domain.components.order.orderstatus.OrderProgress
import com.priyoaujla.domain.components.order.orderstatus.OrderStatus
import com.priyoaujla.domain.components.order.orderstatus.OrderStatusStorage
import com.priyoaujla.domain.components.order.payment.PaymentId
import com.priyoaujla.domain.components.order.payment.PaymentInstructions
import com.priyoaujla.domain.components.order.payment.PaymentType
import com.priyoaujla.domain.components.order.payment.Paypal
import componenttests.com.priyoaujla.TestData.Ingredients.basil
import componenttests.com.priyoaujla.TestData.Ingredients.mozzarella
import componenttests.com.priyoaujla.TestData.Ingredients.pizzaDough
import componenttests.com.priyoaujla.TestData.Ingredients.tomatoSauce
import componenttests.com.priyoaujla.transaction.InMemoryTransactor
import org.junit.jupiter.api.Assertions.*
import java.util.*

class TheSystem {

    private val menuStorage = InMemoryMenuStorage()
    private val menuHub = TheMenu(InMemoryTransactor { menuStorage })
        .apply {
            TestData.minimalMenu.items.forEach {
                this.createItem(it.item, it.price)
            }
        }

    private val orderStorage: InMemoryOrderStorage = InMemoryOrderStorage()
    private val orderStatusStorage = InMemoryOrderStatusStorage()

    private val NotifyOrderComplete: NotifyOrderComplete = {
        kitchen.createTicket(toTicket(it))
    }

    private val ordering = Ordering(transactor = InMemoryTransactor {
        Triple(
            orderStorage,
            orderStatusStorage,
            NotifyOrderComplete
        )
    })

    private val ticketStorage = InMemoryTicketStorage()

    private val kitchen = Kitchen(
        transactor = InMemoryTransactor {
            ticketStorage to NotifyTicketComplete(
                updateOrderStatus = { orderId, status -> OrderProgress(
                    orderStatusStorage
                ).update(orderId, status) },
                createDelivery = CreateDelivery(orderStorage) { delivery.newDelivery(it) }
            )
        })


    private val deliveryStorage = InMemoryDeliveryStorage()

    private val delivery = Delivery(
        transactor = InMemoryTransactor {
            deliveryStorage to NotifyDelivered { orderId, status ->
                OrderProgress(orderStatusStorage)
                    .update(orderId, status)
            }
        }
    )

    private val paypal = FakePaypal()

    fun newCustomer(): CustomerRole =
        CustomerRole(menuHub, ordering, paypal, orderStatusStorage)

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

    fun canMarkDeliveryAsPaid(deliveryId: DeliveryId) {
        delivery.acceptedPaymentFor(deliveryId)
    }
}

class ChefRole(
    private val kitchen: Kitchen
) {

    fun canPickupNextTicket(forOrder: Order): Ticket {
        val nextTicket = kitchen.nextTicket()
        assertNotNull(nextTicket)
        assertEquals(nextTicket.orderId, forOrder.id)
        assertEquals(nextTicket.items, forOrder.items.map { it.item })
        return nextTicket
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
    private val orderStatusStorage: OrderStatusStorage
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

    fun canPayForOrder(order: Order, paymentType: PaymentType): PaymentId? {
        return when(val paymentInstructions = ordering.payment(paymentType, order)) {
            is PaymentInstructions.RedirectToPaypal -> {
                val paymentId = paypal.pay(paymentInstructions.order.id, paymentInstructions.order.total)
                ordering.paymentConfirmed(paymentInstructions.order.id, paymentId)
                paymentId
            }
            is PaymentInstructions.NoInstructions -> null
        }
    }

    fun canSeeOrderDetails(orderId: OrderId, expectedOrderDetails: OrderDetails) {
        val order = ordering.retrieve(orderId)
        assertEquals(expectedOrderDetails.items, order?.items)
        assertEquals(expectedOrderDetails.total, order?.total)
        assertEquals(expectedOrderDetails.paymentStatus, order?.paymentStatus)
    }

    fun canSeeOrderStatus(orderId: OrderId, status: OrderStatus.Status) {
        assertEquals(status, orderStatusStorage.get(orderId)?.status)
    }

    data class OrderDetails(
        val items: List<Menu.MenuItem> = emptyList(),
        val total: Money,
        val paymentStatus: PaymentStatus = PaymentStatus.PaymentRequired
    )
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
) : TicketStorage {

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

}

class InMemoryOrderStatusStorage(
    private val storage: MutableSet<OrderStatus> = mutableSetOf()
) : OrderStatusStorage {

    override fun upsert(orderStatus: OrderStatus) {
        storage.removeIf { it.id == orderStatus.id }
        storage.add(orderStatus)
    }

    override fun get(orderId: OrderId): OrderStatus? = storage.find { it.id == orderId }
}

data class InMemoryOrderStorage(
    private val storage: MutableSet<Order> = mutableSetOf()
) : OrderStorage {

    override fun upsert(order: Order) {
        storage.removeIf { it.id == order.id }
        storage.add(order)
    }

    override fun get(orderId: OrderId): Order? = storage.find { it.id == orderId }

}

class InMemoryMenuStorage(
    private val storage: MutableSet<Menu.MenuItem> = mutableSetOf()
) : MenuStorage {


    override fun get(): Menu {
        return Menu(storage)
    }

    override fun add(item: Menu.Item, price: Money) {
        storage.add(Menu.MenuItem(item, price))
    }

}

class InMemoryDeliveryStorage(
    private val storage: MutableMap<DeliveryId, DeliveryNote> = mutableMapOf<DeliveryId, DeliveryNote>()
) : DeliveryStorage {

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