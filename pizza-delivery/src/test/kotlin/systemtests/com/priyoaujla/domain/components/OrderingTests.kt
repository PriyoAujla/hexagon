package systemtests.com.priyoaujla.domain.components

import com.priyoaujla.domain.components.ordering.orderstatus.OrderStatus.Status.New
import com.priyoaujla.domain.components.ordering.payment.PaymentType
import org.junit.jupiter.api.Test
import systemtests.com.priyoaujla.CustomerRole
import systemtests.com.priyoaujla.CustomerRole.OrderDetails.PaymentStatus.Paid
import systemtests.com.priyoaujla.TestData
import systemtests.com.priyoaujla.TheSystem

class OrderingTests {

    private val theSystem = TheSystem()
    private val customer = theSystem.newCustomer()

    @Test
    fun `the customer can see a list of their orders`() {
        val (firstOrderItems, firstOrderTotal) = TestData.randomItemsToOrder().apply {
            customer.canAddToBasket(this.first)
        }

        customer.canPayForBasket(PaymentType.Paypal)

        val firstOrderId = customer.canSeeOrderWithDetails(
            CustomerRole.OrderDetails(
                items = firstOrderItems,
                total = firstOrderTotal,
                paymentStatus = Paid
            )
        )

        val (secondOrderItems, secondOrderTotal) = TestData.randomItemsToOrder().apply {
            customer.canAddToBasket(this.first)
        }

        customer.canPayForBasket(PaymentType.Paypal)

        val secondOrderId = customer.canSeeOrderWithDetails(
            CustomerRole.OrderDetails(
                items = secondOrderItems,
                total = secondOrderTotal,
                paymentStatus = Paid
            )
        )

        customer.canSeeOrderStatus(secondOrderId, New)

        customer.canSeeOrders(firstOrderId, secondOrderId)
    }
}