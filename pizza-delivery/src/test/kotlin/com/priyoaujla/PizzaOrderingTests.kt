package com.priyoaujla

import com.priyoaujla.TestData.minimalMenu
import org.junit.Test

class PizzaOrderingTests {

    private val scenario = Scenario()
    private val customer = scenario.newCustomer()

    @Test
    fun `customer can order and pay`() {
        val items = minimalMenu.items.toList() + minimalMenu.items
        val theBill = customer.canOrder(items, Money(17.96))
        customer.canPay(theBill, PaymentType.Paypal)
    }
}