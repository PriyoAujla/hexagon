package systemtests.com.priyoaujla.domain.components

import com.priyoaujla.domain.components.ordering.Money
import com.priyoaujla.domain.components.ordering.payment.PaymentType
import org.junit.jupiter.api.Test
import systemtests.com.priyoaujla.CustomerRole.OrderDetails
import systemtests.com.priyoaujla.CustomerRole.OrderDetails.PaymentStatus.Paid
import systemtests.com.priyoaujla.CustomerRole.OrderDetails.PaymentStatus.PaymentRequired
import systemtests.com.priyoaujla.TestData
import systemtests.com.priyoaujla.TheSystem

class CheckoutTests {

    private val theSystem = TheSystem()
    private val customer = theSystem.newCustomer()

    private val itemsToCheckout = TestData.minimalMenu.items.toList() + TestData.minimalMenu.items

    @Test
    fun `the customer can add menu items to their basket`() {
        val items = itemsToCheckout
        customer.canAddToBasket(items)
        customer.canSeeBasketHas(items)
    }

    @Test
    fun `the customer can checkout the basket using paypal`() {
        val items = itemsToCheckout.apply {
            customer.canAddToBasket(this)
        }

        customer.canPayForBasket(PaymentType.Paypal)

        customer.canSeeOrderWithDetails(OrderDetails(
            items = items,
            total = Money(17.96),
            paymentStatus = Paid
        ))
    }

    @Test
    fun `the customer can checkout the basket and choose cash on delivery`() {
        val items = itemsToCheckout.apply {
            customer.canAddToBasket(this)
        }

        customer.canPayForBasket(PaymentType.Cash)

        customer.canSeeOrderWithDetails(OrderDetails(
            items = items,
            total = Money(17.96),
            paymentStatus = PaymentRequired
        ))
    }
}