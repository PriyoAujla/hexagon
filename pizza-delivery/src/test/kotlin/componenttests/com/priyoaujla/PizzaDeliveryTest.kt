package componenttests.com.priyoaujla

import com.priyoaujla.order.Order
import com.priyoaujla.order.OrderStatus
import org.junit.jupiter.api.Test

class PizzaDeliveryTest {

    private val scenario = Scenario()

    @Test
    fun `the courier is able to see the order once it has been cooked`() = HasFinishedCookingOrder(scenario).runTest {
        courier.theNextDeliveryIs(order)
    }

    @Test
    fun `the courier can notify when the order has been delivered`() = HasFinishedCookingOrder(scenario).runTest {
        val delivery = courier.theNextDeliveryIs(order)
        courier.hasDelivered(delivery)
        customer.canSeeOrderStatus(delivery.orderId, OrderStatus.Status.Delivered)
    }

    @Test
    fun `the courier must confirm money was exchanged`() = HasFinishedCookingOrder(scenario, withOrder = CashOnDeliveryOrder(scenario)).runTest {

    }

}