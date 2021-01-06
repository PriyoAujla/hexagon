package systemtests.com.priyoaujla.domain.components

import com.priyoaujla.domain.components.order.Money
import com.priyoaujla.domain.components.order.PaymentStatus
import com.priyoaujla.domain.components.order.payment.PaymentType
import org.junit.jupiter.api.Test
import systemtests.com.priyoaujla.CustomerRole
import systemtests.com.priyoaujla.TestData.minimalMenu
import systemtests.com.priyoaujla.TheSystem

class OrderTests {

    private val theSystem = TheSystem()
    private val customer = theSystem.newCustomer()

    @Test
    fun `customer can order and pay with paypal`() {
        val items = minimalMenu.items.toList() + minimalMenu.items
        val order = customer.canOrder(items, Money(17.96))
        val paymentId = customer.canPayForOrder(order, PaymentType.Paypal)
        customer.canSeeOrderDetails(order.id, CustomerRole.OrderDetails(
            items = order.items,
            total = Money(17.96),
            paymentStatus = PaymentStatus.Paid(paymentId = paymentId!!)
        )
        )
    }

    @Test
    fun `customer can order and choose to pay on delivery`() {
        val items = minimalMenu.items.toList() + minimalMenu.items
        val order = customer.canOrder(items, Money(17.96))
        customer.canPayForOrder(order, PaymentType.Cash)
        customer.canSeeOrderDetails(order.id, CustomerRole.OrderDetails(
            items = order.items,
            total = Money(17.96),
            paymentStatus = PaymentStatus.PaymentRequired
        )
        )
    }
}