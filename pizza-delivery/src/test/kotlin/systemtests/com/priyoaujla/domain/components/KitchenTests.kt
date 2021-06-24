package systemtests.com.priyoaujla.domain.components

import com.priyoaujla.domain.components.ordering.orderstatus.OrderStatus
import org.junit.jupiter.api.Test
import systemtests.com.priyoaujla.TheSystem
import systemtests.com.priyoaujla.TwoSeparatePaidOrdersScenario
import systemtests.com.priyoaujla.runTest

class KitchenTests {

    private val theSystem = TheSystem()
    private val chef = theSystem.newChef()

    @Test
    fun `once an order is paid for the kitchen will pick up the first ticket received`() =
        TwoSeparatePaidOrdersScenario(theSystem).runTest {
            chef.canPickupNextTicket(forOrder = firstOrder)
        }

    @Test
    fun `the kitchen can update the ticket once cooking is finished and proceed to the next ticket`() =
        TwoSeparatePaidOrdersScenario(theSystem).runTest {

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

