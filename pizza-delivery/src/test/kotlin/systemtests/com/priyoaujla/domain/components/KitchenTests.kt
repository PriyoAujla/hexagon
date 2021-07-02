package systemtests.com.priyoaujla.domain.components

import com.priyoaujla.domain.components.ordering.orderstatus.OrderStatus
import com.priyoaujla.domain.components.ordering.payment.PaymentType
import org.junit.jupiter.api.Test
import systemtests.com.priyoaujla.ChefRole.TicketDetails
import systemtests.com.priyoaujla.Scenarios
import systemtests.com.priyoaujla.TestData
import systemtests.com.priyoaujla.TheSystem

class KitchenTests {

    private val theSystem = TheSystem()
    private val chef = theSystem.newChef()

    @Test
    fun `once an order is paid for the kitchen will pick up the first ticket received`() {
        val firstCustomer = theSystem.newCustomer()
        val firstCustomerBasket = TestData.minimalMenu.items.toList() + TestData.minimalMenu.items
        Scenarios.checkoutBasket(customer = firstCustomer, basketItems = firstCustomerBasket, withPaymentType = PaymentType.Cash)

        val secondCustomer = theSystem.newCustomer()
        val secondCustomerBasket = TestData.minimalMenu.items.toList()
        Scenarios.checkoutBasket(customer = secondCustomer, basketItems = secondCustomerBasket, withPaymentType = PaymentType.Cash)

        chef.canPickupNextTicket(TicketDetails(firstCustomerBasket))
    }

    @Test
    fun `the kitchen can update the ticket and order status once cooking is finished`() {
        val firstCustomer = theSystem.newCustomer()
        val firstCustomerBasket = TestData.minimalMenu.items.toList() + TestData.minimalMenu.items
        val firstCustomerOrderId = Scenarios.checkoutBasket(customer = firstCustomer, basketItems = firstCustomerBasket, withPaymentType = PaymentType.Cash)

        val secondCustomer = theSystem.newCustomer()
        val secondCustomerBasket = TestData.minimalMenu.items.toList()
        val secondCustomerOrderId = Scenarios.checkoutBasket(customer = secondCustomer, basketItems = secondCustomerBasket, withPaymentType = PaymentType.Cash)

        chef.canPickupNextTicket(TicketDetails(firstCustomerBasket)).also {
            chef.canFinishCooking(it)
            firstCustomer.canSeeOrderStatus(firstCustomerOrderId, OrderStatus.Status.Cooked)
            secondCustomer.canSeeOrderStatus(secondCustomerOrderId, OrderStatus.Status.New)
        }

        chef.canPickupNextTicket(TicketDetails(secondCustomerBasket)).also {
            chef.canFinishCooking(it)
            firstCustomer.canSeeOrderStatus(secondCustomerOrderId, OrderStatus.Status.Cooked)
            secondCustomer.canSeeOrderStatus(secondCustomerOrderId, OrderStatus.Status.Cooked)
        }
    }
}

