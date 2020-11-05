package componenttests.com.priyoaujla

import com.priyoaujla.domain.order.orderstatus.OrderStatus
import org.junit.jupiter.api.Test

class PizzaDeliveryTest {

    private val scenario = Scenario()

    @Test
    fun `the courier is able to see the order once it has been cooked`() =
        HasOrderWaitingDelivery(scenario).runTest {
            courier.theNextDeliveryIs(order)
        }

    @Test
    fun `the courier can notify when the order has been delivered`() =
        HasOrderWaitingDelivery(scenario).runTest {
            val delivery = courier.theNextDeliveryIs(order)
            courier.hasDelivered(delivery)
            customer.canSeeOrderStatus(delivery.orderId, OrderStatus.Status.Delivered)
        }

    @Test
    fun `the courier must confirm payment is received for a cash on delivery order`() =
        HasOrderWaitingDelivery(scenario, withOrder = CashOnDeliveryOrder(scenario)).runTest {
            val delivery = courier.theNextDeliveryIs(order)
            courier.canMarkDeliveryAsPaid(delivery.id)
            courier.hasDelivered(delivery)
        }

}