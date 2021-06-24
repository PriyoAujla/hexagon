package systemtests.com.priyoaujla.domain.components

import com.priyoaujla.domain.components.ordering.Money
import com.priyoaujla.domain.components.ordering.PaymentStatus
import com.priyoaujla.domain.components.ordering.payment.PaymentType
import org.junit.jupiter.api.Test
import systemtests.com.priyoaujla.CustomerRole.OrderDetails
import systemtests.com.priyoaujla.TestData.minimalMenu
import systemtests.com.priyoaujla.TheSystem

class OrderingTests {

    private val theSystem = TheSystem()
    private val customer = theSystem.newCustomer()

    @Test
    fun `customer can order and pay with paypal`() {
        val items = minimalMenu.items.toList() + minimalMenu.items
        val order = customer.canOrder(items, Money(17.96))
        val paymentId = customer.canPayForOrder(order, PaymentType.Paypal)
        customer.canSeeOrderWithDetails(order.id, OrderDetails(
            items = order.items,
            total = Money(17.96),
            paymentStatus = PaymentStatus.Paid(paymentId = paymentId!!)
        ))
    }

    @Test
    fun `customer can order and choose to pay on delivery`() {
        val items = minimalMenu.items.toList() + minimalMenu.items
        val order = customer.canOrder(items, Money(17.96))
        customer.canPayForOrder(order, PaymentType.Cash)
        customer.canSeeOrderWithDetails(order.id, OrderDetails(
            items = order.items,
            total = Money(17.96),
            paymentStatus = PaymentStatus.PaymentRequired
        ))
    }
}