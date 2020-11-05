package componenttests.com.priyoaujla

import com.priyoaujla.domain.order.Money
import com.priyoaujla.domain.order.PaymentStatus
import com.priyoaujla.domain.order.payment.PaymentType
import componenttests.com.priyoaujla.TestData.minimalMenu
import org.junit.jupiter.api.Test

class PizzaOrderingTests {

    private val scenario = Scenario()
    private val customer = scenario.newCustomer()

    @Test
    fun `customer can order and pay with paypal`() {
        val items = minimalMenu.items.toList() + minimalMenu.items
        val order = customer.canOrder(items, Money(17.96))
        val paymentId = customer.canPayForOrder(order, PaymentType.Paypal)
        customer.canSeeOrderDetails(order.id, CustomerRole.OrderDetails(
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
        customer.canSeeOrderDetails(order.id, CustomerRole.OrderDetails(
            items = order.items,
            total = Money(17.96),
            paymentStatus = PaymentStatus.PaymentRequired
        ))
    }
}