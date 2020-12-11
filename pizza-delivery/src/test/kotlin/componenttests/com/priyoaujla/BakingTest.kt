package componenttests.com.priyoaujla

import com.priyoaujla.domain.order.orderstatus.OrderStatus
import org.junit.jupiter.api.Test

class BakingTest {

    private val scenario = Scenario()
    private val chef = scenario.newChef()

    @Test
    fun `once an order is paid for the kitchen will pick up the first ticket received`() = thereAreTwoPaidOrders(scenario).runTest {
        val (paypalOrder) = this
        chef.canPickupNextTicket(forOrder = paypalOrder.order)
    }

    @Test
    fun `the kitchen can update the ticket once cooking is finished and proceed to the next ticket`() = thereAreTwoPaidOrders(scenario).runTest {
        val (firstPaypalOrder, secondPaypalOrder) = this

        val firstCustomer = firstPaypalOrder.customer
        val secondCustomer = secondPaypalOrder.customer

        chef.canPickupNextTicket(forOrder = firstPaypalOrder.order).also {
            chef.canFinishCooking(it)
            firstCustomer.canSeeOrderStatus(firstPaypalOrder.order.id, OrderStatus.Status.Cooked)
            secondCustomer.canSeeOrderStatus(secondPaypalOrder.order.id, OrderStatus.Status.New)
        }

        chef.canPickupNextTicket(forOrder = secondPaypalOrder.order).also {
            chef.canFinishCooking(it)
            firstCustomer.canSeeOrderStatus(firstPaypalOrder.order.id, OrderStatus.Status.Cooked)
            secondCustomer.canSeeOrderStatus(secondPaypalOrder.order.id, OrderStatus.Status.Cooked)
        }
    }
}

