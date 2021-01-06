package systemtests.com.priyoaujla.domain.components

import com.priyoaujla.domain.components.order.orderstatus.OrderStatus
import org.junit.jupiter.api.Test
import systemtests.com.priyoaujla.CashOnDeliveryScenario
import systemtests.com.priyoaujla.OrderFinishedCookingScenario
import systemtests.com.priyoaujla.TheSystem
import systemtests.com.priyoaujla.runTest

class DeliveryTest {

    private val theSystem = TheSystem()

    @Test
    fun `the courier is able to see the order once it has been cooked`() =
        OrderFinishedCookingScenario(theSystem).runTest {
            courier.theNextDeliveryIs(order)
        }

    @Test
    fun `the courier can notify when the order has been delivered`() =
        OrderFinishedCookingScenario(theSystem).runTest {
            val delivery = courier.theNextDeliveryIs(order)
            courier.hasDelivered(delivery)
            customer.canSeeOrderStatus(delivery.orderId, OrderStatus.Status.Delivered)
        }

    @Test
    fun `the courier must confirm payment is received for a cash on delivery order`() =
        OrderFinishedCookingScenario(theSystem, using = CashOnDeliveryScenario(theSystem)).runTest {
            val delivery = courier.theNextDeliveryIs(order)
            courier.canMarkDeliveryAsPaid(delivery.id)
            courier.hasDelivered(delivery)
        }

}