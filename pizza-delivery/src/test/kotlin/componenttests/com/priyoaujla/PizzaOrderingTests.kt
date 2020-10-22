package componenttests.com.priyoaujla

import com.priyoaujla.domain.order.Money
import com.priyoaujla.domain.order.OrderStatus
import com.priyoaujla.domain.order.payment.PaymentType
import componenttests.com.priyoaujla.TestData.minimalMenu
import org.junit.jupiter.api.Test

class PizzaOrderingTests {

    private val scenario = Scenario()
    private val customer = scenario.newCustomer()

    @Test
    fun `customer can order and pay`() {
        val items = minimalMenu.items.toList() + minimalMenu.items
        val order = customer.canOrder(items, Money(17.96))
        customer.canPay(order, PaymentType.Paypal)
        customer.canSeeOrderStatus(order.id, OrderStatus.Status.Paid)
    }
}