package componenttests.com.priyoaujla.domain.components

import com.priyoaujla.domain.components.order.orderstatus.OrderStatus
import componenttests.com.priyoaujla.CashOnDeliveryScenario
import componenttests.com.priyoaujla.OrderWaitingDeliveryScenario
import componenttests.com.priyoaujla.System
import componenttests.com.priyoaujla.runTest
import org.junit.jupiter.api.Test

class DeliveryTest {

    private val system = System()

    @Test
    fun `the courier is able to see the order once it has been cooked`() =
        OrderWaitingDeliveryScenario(system).runTest {
            courier.theNextDeliveryIs(order)
        }

    @Test
    fun `the courier can notify when the order has been delivered`() =
        OrderWaitingDeliveryScenario(system).runTest {
            val delivery = courier.theNextDeliveryIs(order)
            courier.hasDelivered(delivery)
            customer.canSeeOrderStatus(delivery.orderId, OrderStatus.Status.Delivered)
        }

    @Test
    fun `the courier must confirm payment is received for a cash on delivery order`() =
        OrderWaitingDeliveryScenario(system, using = CashOnDeliveryScenario(system)).runTest {
            val delivery = courier.theNextDeliveryIs(order)
            courier.canMarkDeliveryAsPaid(delivery.id)
            courier.hasDelivered(delivery)
        }

}