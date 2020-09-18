package componenttests.com.priyoaujla

import com.priyoaujla.menu.Menu
import com.priyoaujla.order.Order
import com.priyoaujla.order.toTicket
import org.junit.Test

class PizzaDeliveryTest {

    private val scenario = Scenario()

    @Test
    fun `the courier is able to see the order once it has been cooked`() = HasFinishedCookingAnOrder(scenario).runTest {
        courier.theNextDeliveryIs(order)
    }

    @Test
    fun `the courier can notify when the order has been delivered`() = HasFinishedCookingAnOrder(scenario).runTest {
        val delivery = courier.theNextDeliveryIs(order)
        courier.hasDelivered(delivery)
        customer.canSeeOrderStatus(delivery.orderId, Order.Status.Delivered)
    }

}

class HasFinishedCookingAnOrder(scenario: Scenario, items: List<Menu.MenuItem> = TestData.minimalMenu.items.toList() + TestData.minimalMenu.items) {

    private val hasPaidForAnOrder = HasPaidForAnOrder(scenario, items)

    val customer get() = hasPaidForAnOrder.customer
    val order get() = hasPaidForAnOrder.order

    val chef = scenario.newChef()
    val courier = scenario.newCourier()

    init {
        chef.canFinishCooking(toTicket(order))
    }
}