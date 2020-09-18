package componenttests.com.priyoaujla

import com.priyoaujla.menu.Menu
import com.priyoaujla.order.Money
import com.priyoaujla.order.payment.PaymentType
import com.priyoaujla.order.toTicket

fun thereAreTwoPaidOrders(scenario: Scenario) =
        HasPaidOrder(scenario) to HasPaidOrder(
                scenario,
                TestData.minimalMenu.items.take(1)
        )

class HasPaidOrder(scenario: Scenario, items: List<Menu.MenuItem> = TestData.minimalMenu.items.toList() + TestData.minimalMenu.items) {
    val customer = scenario.newCustomer()

    val order = customer.canOrder(items, items.fold(Money(0.0)){ total, item -> total + item.price})
    val paymentId = customer.canPay(order, PaymentType.Paypal)
}

class HasFinishedCookingOrder(scenario: Scenario, items: List<Menu.MenuItem> = TestData.minimalMenu.items.toList() + TestData.minimalMenu.items) {

    private val hasPaidForAnOrder = HasPaidOrder(scenario, items)

    val customer get() = hasPaidForAnOrder.customer
    val order get() = hasPaidForAnOrder.order

    val chef = scenario.newChef()
    val courier = scenario.newCourier()

    init {
        chef.canFinishCooking(toTicket(order))
    }
}