package componenttests.com.priyoaujla

import com.priyoaujla.domain.order.orderstatus.OrderStatus
import com.priyoaujla.domain.order.toTicket
import org.junit.jupiter.api.Test

class PizzaBakingTest {

    private val scenario = Scenario()
    private val chef = scenario.newChef()

    @Test
    fun `once an order is paid for the kitchen will pick up the first ticket received`() = thereAreTwoPaidOrders(scenario).runTest {
        val (first, _) = this
        chef.canPickupNextTicket(toTicket(first.order))
    }

    @Test
    fun `the kitchen can update the ticket once cooking is finished and proceed to the next ticket`() = thereAreTwoPaidOrders(scenario).runTest {
        val (first, second) = this

        val firstCustomer = first.customer
        val firstTicket = toTicket(first.order)

        val secondCustomer = second.customer
        val secondTicket = toTicket(second.order)

        chef.canFinishCooking(firstTicket)
        firstCustomer.canSeeOrderStatus(firstTicket.orderId, OrderStatus.Status.Cooked)

        chef.canPickupNextTicket(secondTicket)
        chef.canFinishCooking(secondTicket)
        secondCustomer.canSeeOrderStatus(firstTicket.orderId, OrderStatus.Status.Cooked)
    }
}

