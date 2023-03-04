package systemtests.com.priyoaujla

import com.priyoaujla.domain.components.checkout.*
import com.priyoaujla.domain.components.delivery.*
import com.priyoaujla.domain.components.kitchen.*
import com.priyoaujla.domain.components.menu.Menu
import com.priyoaujla.domain.components.menu.MenuStorage
import com.priyoaujla.domain.components.menu.TheMenu
import com.priyoaujla.domain.components.ordering.*
import com.priyoaujla.domain.components.ordering.PaymentConfirmationType.Paid
import com.priyoaujla.domain.components.ordering.orderstatus.OrderProgress
import com.priyoaujla.domain.components.ordering.orderstatus.OrderStatus
import com.priyoaujla.domain.components.ordering.orderstatus.OrderStatusStorage
import com.priyoaujla.domain.components.ordering.payment.PaymentId
import com.priyoaujla.domain.components.ordering.payment.PaymentInstructions
import com.priyoaujla.domain.components.ordering.payment.PaymentType
import com.priyoaujla.domain.components.ordering.payment.Paypal
import org.junit.jupiter.api.Assertions.*
import systemtests.com.priyoaujla.transaction.InMemoryTransactor
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

    private val notifyOrderComplete: NotifyOrderComplete = {
        kitchen.createTicket(toTicket(it))
    }

    private val ordering = Ordering(transactor = InMemoryTransactor {
        Triple(
            orderStorage,
            orderStatusStorage,
            notifyOrderComplete
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
                OrderProgress(orderStatusStorage).update(orderId, status)
            }
        }
    )

    private val paypal = FakePaypal()

    private val customerBasketStorage = InMemoryCustomerBasketStorage()

    fun newCustomer(): CustomerRole =
        CustomerRole(
            theMenu = menuHub,
            ordering = ordering,
            paypal = paypal,
            orderStatusStorage = orderStatusStorage,
            basket = Basket(InMemoryTransactor { Pair(customerBasketStorage, NotifyOnCheckout.instance(
                ordering = ordering,
                customerBasketStorage = customerBasketStorage
            )) })
        )

    fun newChef(): ChefRole =
        ChefRole(kitchen)

    fun newCourier(): CourierRole =
        CourierRole(delivery)
}

class CourierRole(
    private val delivery: Delivery
) {

    fun theNextDeliveryIs(deliveryDetails: DeliveryDetails): DeliveryNote {
        val nextOrderToDeliver = delivery.nextDelivery()
        assertEquals(nextOrderToDeliver.menuItem, deliveryDetails.items)
        assertEquals(nextOrderToDeliver.total, deliveryDetails.total)
        return nextOrderToDeliver
    }

    fun hasDelivered(delivery: DeliveryNote) {
        this.delivery.delivered(delivery.id)
    }

    fun canMarkDeliveryAsPaid(deliveryId: DeliveryId) {
        delivery.acceptedPaymentFor(deliveryId)
    }

    data class DeliveryDetails(val items: List<Menu.MenuItem>, val total: Money)
}

class ChefRole(
    private val kitchen: Kitchen
) {

    fun canPickupNextTicket(expectedTicketDetails: TicketDetails): Ticket {
        val nextTicket = kitchen.nextTicket()
        assertNotNull(nextTicket)
        assertEquals(nextTicket.items, expectedTicketDetails.items.map { it.item })
        return nextTicket
    }

    fun canFinishCooking(ticket: Ticket) {
        kitchen.ticketComplete(ticket)
        assertFalse(kitchen.tickets().toList().contains(ticket))
    }

    data class TicketDetails(val items: List<Menu.MenuItem>)
}

class CustomerRole(
    private val customerId: CustomerId = CustomerId.mint(),
    private val theMenu: TheMenu,
    private val ordering: Ordering,
    private val paypal: Paypal,
    private val orderStatusStorage: OrderStatusStorage,
    private val basket: Basket
) {

    fun canSeeMenuWith(menuItems: Set<Menu.MenuItem>) {
        val menu = theMenu.fetch()
        assertEquals(menu.items, menuItems)
    }

    fun canSeeOrderWithDetails(expectedOrderDetails: OrderDetails): OrderId {
        val orders = ordering.list()
        val result = orders.find {
            it.items == expectedOrderDetails.items &&
            it.total == expectedOrderDetails.total &&
            when(it.paymentStatus) {
                is PaymentStatus.PaymentRequired -> OrderDetails.PaymentStatus.PaymentRequired
                is PaymentStatus.Paid -> OrderDetails.PaymentStatus.Paid
            } == expectedOrderDetails.paymentStatus
        }
        assertNotNull(result)
        return result!!.id
    }

    fun canSeeOrderStatus(orderId: OrderId, status: OrderStatus.Status) {
        assertEquals(status, orderStatusStorage.get(orderId)?.status)
    }

    fun canAddToBasket(items: List<Menu.MenuItem>) {
        basket.add(customerId, items)
    }

    fun canSeeBasketHas(items: List<Menu.MenuItem>) {
        val customerBasket = basket.fetch(customerId)
        assertNotNull(customerBasket)
        assertEquals(items, customerBasket?.items)
    }

    fun canPayForBasket(paymentType: PaymentType): PaymentId? {
        val customerBasket = basket.fetch(customerId)!!
        val instructions = basket.payment(customerId, paymentType)
        assertEquals(instructions.checkoutCustomerBasket?.basket?.customerId, customerId)
        assertEquals(instructions.checkoutCustomerBasket?.basket?.items, customerBasket.items)
        assertEquals(instructions.checkoutCustomerBasket?.basket?.total(), customerBasket.total())
        return when(instructions) {
            is PaymentInstructions.RedirectToPaypal -> {
                val transactionId = instructions.checkoutCustomerBasket!!.transactionId
                val paymentId = paypal.pay(transactionId, instructions.checkoutCustomerBasket!!.basket.total())
                ordering.paymentConfirmed(transactionId, Paid(paymentId))
                paymentId
            }
            is PaymentInstructions.NoInstructions -> null
        }
    }

    fun canSeeOrders(vararg orderIds: OrderId) {
        val actualOrderIds = ordering.list()
            .map { it.id }

        assertEquals(orderIds.toList(), actualOrderIds)
    }

    data class OrderDetails(
        val items: List<Menu.MenuItem> = emptyList(),
        val total: Money,
        val paymentStatus: PaymentStatus = PaymentStatus.PaymentRequired
    ) {
        enum class PaymentStatus {
            PaymentRequired, Paid
        }
    }

}

class FakePaypal : Paypal {

    override fun pay(transactionId: TransactionId, total: Money): PaymentId {
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

    override fun findBy(transactionId: TransactionId): Order? = storage.find { it.transactionId == transactionId }
    override fun all(): Set<Order> = storage

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
    private val storage: MutableMap<DeliveryId, DeliveryNote> = mutableMapOf()
) : DeliveryStorage {

    override fun get(id: DeliveryId): DeliveryNote? {
        return storage[id]
    }

    override fun upsert(deliveryNote: DeliveryNote) {
        storage += deliveryNote.id to deliveryNote
    }

    override fun take(): DeliveryNote = storage.toList().first().second
}

class InMemoryCustomerBasketStorage(
    private val storage: MutableMap<CustomerId, CustomerBasket> = mutableMapOf()
): CustomerBasketStorage {

    override fun retrieve(customerId: CustomerId): CustomerBasket? = storage[customerId]

    override fun save(customerBasket: CustomerBasket) {
        storage += customerBasket.customerId to customerBasket
    }

    override fun clear(customerId: CustomerId) {
        storage.remove(customerId)
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
val carmenTheCourier = UserDetails(
    UserId.mint(),
    GivenName("Carmen"),
    FamilyName("UserDetails"),
    Address("Some address")
)

inline fun <T, R> T.runTest(block: T.() -> R) {
    block()
}