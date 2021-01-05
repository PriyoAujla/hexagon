package componenttests.com.priyoaujla

import com.priyoaujla.domain.components.menu.Menu
import com.priyoaujla.domain.components.order.Money
import com.priyoaujla.domain.components.order.Order
import com.priyoaujla.domain.components.order.payment.PaymentType

class TwoSeparatedPaidOrdersScenario(system: System) {
    private val pair = PaypalPaidScenario(system) to PaypalPaidScenario(
        system,
        TestData.minimalMenu.items.take(1)
    )

    val firstCustomer = pair.first.customer
    val secondCustomer = pair.second.customer

    val firstOrder = pair.first.order
    val secondOrder = pair.second.order
}

abstract class OrderScenario {
    abstract val customer: CustomerRole
    abstract val order: Order

    protected fun createOrder(withItems: List<Menu.MenuItem>, withPaymentMethod: PaymentType) = customer.canOrder(withItems, withItems.fold(Money(0.0)) { total, item -> total + item.price })
        .also {
            customer.canPayForOrder(it, withPaymentMethod)
        }
}

class PaypalPaidScenario(
    system: System,
    items: List<Menu.MenuItem> = TestData.minimalMenu.items.toList() + TestData.minimalMenu.items
) : OrderScenario() {
    override val customer = system.newCustomer()

    override val order = createOrder(items, PaymentType.Paypal)
}

class CashOnDeliveryScenario(
    system: System,
    items: List<Menu.MenuItem> = TestData.minimalMenu.items.toList() + TestData.minimalMenu.items
) : OrderScenario() {
    override val customer = system.newCustomer()

    override val order = createOrder(items, PaymentType.Cash)
}

class OrderWaitingDeliveryScenario(
    system: System,
    private val using: OrderScenario = PaypalPaidScenario(
        system,
        TestData.minimalMenu.items.toList() + TestData.minimalMenu.items
    )
) {

    val customer get() = using.customer
    val order get() = using.order

    val chef = system.newChef()
    val courier = system.newCourier()

    init {
        chef.canPickupNextTicket(order).also {
            chef.canFinishCooking(it)
        }
    }
}