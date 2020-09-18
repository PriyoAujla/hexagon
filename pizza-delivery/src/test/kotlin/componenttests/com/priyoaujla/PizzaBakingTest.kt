package componenttests.com.priyoaujla

import com.priyoaujla.menu.Menu
import com.priyoaujla.order.*
import com.priyoaujla.order.payment.PaymentType
import org.junit.Test

class PizzaBakingTest {

    private val scenario = Scenario()
    private val chef = scenario.newChef()

    @Test
    fun `once an order is paid for the kitchen will pick up the first ticket received`() = thereAreTwoPaidOrders(
        scenario
    ).runTest {
        val (first, _) = this
        chef.canPickupNextTicket(toTicket(first.order))
    }

    @Test
    fun `the kitchen can update the ticket once cooking is finished and proceed to the next ticket`() = thereAreTwoPaidOrders(
        scenario
    ).runTest {
        val (first, second) = this

        val firstCustomer = first.customer
        val firstTicket = toTicket(first.order)

        val secondCustomer = second.customer
        val secondTicket = toTicket(second.order)

        chef.canFinishCooking(firstTicket)
        firstCustomer.canSeeOrderStatus(firstTicket.orderId, Order.Status.Cooked)

        chef.canPickupNextTicket(secondTicket)
        chef.canFinishCooking(secondTicket)
        secondCustomer.canSeeOrderStatus(firstTicket.orderId, Order.Status.Cooked)
    }
}

private fun thereAreTwoPaidOrders(scenario: Scenario) =
    HasPaidForAnOrder(scenario) to HasPaidForAnOrder(
        scenario,
        TestData.minimalMenu.items.take(1)
    )

class HasPaidForAnOrder(scenario: Scenario, items: List<Menu.MenuItem> = TestData.minimalMenu.items.toList() + TestData.minimalMenu.items) {
    val customer = scenario.newCustomer()

    val order = customer.canOrder(items, items.fold(Money(0.0)){ total, item -> total + item.price})
    val paymentId = customer.canPay(order, PaymentType.Paypal)
}

