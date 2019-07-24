package com.priyoaujla

import org.junit.Test

class PizzaDeliveryTest {

    private val scenario = Scenario()

    @Test
    fun `a completed ticket will notify the delivery driver`() = HasFinishedCookingAnOrder(scenario).run {

    }

    @Test
    fun `the delivery driver can notify when the order has been delivered`() = HasFinishedCookingAnOrder(scenario).run {

    }

}

class HasFinishedCookingAnOrder(scenario: Scenario, items: List<Menu.MenuItem> = TestData.minimalMenu.items.toList() + TestData.minimalMenu.items) {

    private val orderFixture = HasPaidForAnOrder(scenario, items)

    val customer get() = orderFixture.customer
    val theBill get() = orderFixture.theBill
    val paymentId get() = orderFixture.paymentId

    val chef = scenario.newChef()

    init {
        chef.canFinishCooking(Ticket.from(theBill.order))
    }
}