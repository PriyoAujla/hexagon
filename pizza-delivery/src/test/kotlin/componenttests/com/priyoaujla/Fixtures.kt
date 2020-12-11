package componenttests.com.priyoaujla

import com.priyoaujla.domain.menu.Menu
import com.priyoaujla.domain.order.Money
import com.priyoaujla.domain.order.Order
import com.priyoaujla.domain.order.payment.PaymentType

fun thereAreTwoPaidOrders(scenario: Scenario) =
        PaypalPaidOrder(scenario) to PaypalPaidOrder(
                scenario,
                TestData.minimalMenu.items.take(1)
        )

abstract class OrderSteps {
    abstract val customer: CustomerRole
    abstract val order: Order
}

class PaypalPaidOrder(
        scenario: Scenario,
        items: List<Menu.MenuItem> = TestData.minimalMenu.items.toList() + TestData.minimalMenu.items
): OrderSteps() {
    override val customer = scenario.newCustomer()

    override val order = customer.canOrder(items, items.fold(Money(0.0)){ total, item -> total + item.price})
        .also {
            customer.canPayForOrder(it, PaymentType.Paypal)
        }
}

class CashOnDeliveryOrder(
        scenario: Scenario,
        items: List<Menu.MenuItem> = TestData.minimalMenu.items.toList() + TestData.minimalMenu.items
): OrderSteps() {
    override val customer = scenario.newCustomer()

    override val order = customer.canOrder(items, items.fold(Money(0.0)){ total, item -> total + item.price})
        .also {
            customer.canPayForOrder(it, PaymentType.Cash)
        }
}

class HasOrderWaitingDelivery(
        scenario: Scenario,
        private val withOrder: OrderSteps = PaypalPaidOrder(scenario, TestData.minimalMenu.items.toList() + TestData.minimalMenu.items)
) {

    val customer get() = withOrder.customer
    val order get() = withOrder.order

    val chef = scenario.newChef()
    val courier = scenario.newCourier()

    init {
        chef.canPickupNextTicket(order).also {
            chef.canFinishCooking(it)
        }
    }
}