package componenttests.com.priyoaujla.domain.components

import com.priyoaujla.domain.components.order.orderstatus.OrderStatus
import componenttests.com.priyoaujla.System
import componenttests.com.priyoaujla.TwoSeparatedPaidOrdersScenario
import componenttests.com.priyoaujla.runTest
import org.junit.jupiter.api.Test

class KitchenTest {

    private val system = System()
    private val chef = system.newChef()

    @Test
    fun `once an order is paid for the kitchen will pick up the first ticket received`() =
        TwoSeparatedPaidOrdersScenario(system).runTest {
            chef.canPickupNextTicket(forOrder = firstOrder)
        }

    @Test
    fun `the kitchen can update the ticket once cooking is finished and proceed to the next ticket`() =
        TwoSeparatedPaidOrdersScenario(system).runTest {

            chef.canPickupNextTicket(forOrder = firstOrder).also {
                chef.canFinishCooking(it)
                firstCustomer.canSeeOrderStatus(firstOrder.id, OrderStatus.Status.Cooked)
                secondCustomer.canSeeOrderStatus(secondOrder.id, OrderStatus.Status.New)
            }

            chef.canPickupNextTicket(forOrder = secondOrder).also {
                chef.canFinishCooking(it)
                firstCustomer.canSeeOrderStatus(firstOrder.id, OrderStatus.Status.Cooked)
                secondCustomer.canSeeOrderStatus(secondOrder.id, OrderStatus.Status.Cooked)
            }
        }
}

