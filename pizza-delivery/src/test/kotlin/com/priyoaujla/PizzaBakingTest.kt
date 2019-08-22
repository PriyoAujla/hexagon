package com.priyoaujla

import org.junit.Test

class PizzaBakingTest {

    private val scenario = Scenario()
    private val chef = scenario.newChef()

    @Test
    fun `once an order is paid for the kitchen will receive the order as a ticket`() = HasPaidForAnOrder(scenario).run {
        chef.hasTickets(Ticket.from(order))
    }

    @Test
    fun `the kitchen can pick up the first ticket received`() = thereAreTwoPaidOrders(scenario).run {
        val (firstOrder, _) = this
        chef.canPickupNextTicket(Ticket.from(firstOrder.order))
    }

    @Test
    fun `the kitchen can update the ticket once cooking is finished`() = HasPaidForAnOrder(scenario).run {
        chef.canFinishCooking(Ticket.from(order))
    }
}

private fun thereAreTwoPaidOrders(scenario: Scenario) =
    HasPaidForAnOrder(scenario) to HasPaidForAnOrder(scenario, TestData.minimalMenu.items.take(1))

class HasPaidForAnOrder(scenario: Scenario, items: List<Menu.MenuItem> = TestData.minimalMenu.items.toList() + TestData.minimalMenu.items) {
    val customer = scenario.newCustomer()

    val order = customer.canOrder(items, items.fold(Money(0.0)){ total, item -> total + item.price})
    val paymentId = customer.canPay(order, PaymentType.Paypal)
}

