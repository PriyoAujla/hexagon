package com.priyoaujla

import com.priyoaujla.TestData.minimalMenu
import org.junit.Test

class PizzaOrderingTests {

    private val scenario = Scenario()
    private val customer = scenario.newCustomer()

    @Test
    fun `customer can order and pay`() {
        val items = minimalMenu.items.toList() + minimalMenu.items
        val order = customer.canOrder(items, Money(17.96))
        customer.canPay(order, PaymentType.Paypal)
        customer.canSeeOrderStatus(order.id, Order.Status.Paid)
    }
}