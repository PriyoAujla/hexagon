package com.priyoaujla

import org.junit.Test

class PizzaBakingTest {

    private val scenario = Scenario()
    private val chef = scenario.newChef()

    @Test
    fun `once an order is paid for the kitchen will receive the order as a ticket`() = thereAreTwoPaidOrders(scenario).runTest {
        val (first, second) = this
        chef.hasTickets(Ticket.from(first.order), Ticket.from(second.order))
    }

    @Test
    fun `the kitchen will pick up the first ticket received`() = thereAreTwoPaidOrders(scenario).runTest {
        val (first, _) = this
        chef.canPickupNextTicket(Ticket.from(first.order))
    }

    @Test
    fun `the kitchen can update the ticket once cooking is finished`() = HasPaidForAnOrder(scenario).runTest {
        val ticket = Ticket.from(order)
        chef.canFinishCooking(ticket)
        customer.canSeeOrderStatus(ticket.orderId, Order.Status.Cooked)
    }
}

private fun thereAreTwoPaidOrders(scenario: Scenario) =
    HasPaidForAnOrder(scenario) to HasPaidForAnOrder(scenario, TestData.minimalMenu.items.take(1))

class HasPaidForAnOrder(scenario: Scenario, items: List<Menu.MenuItem> = TestData.minimalMenu.items.toList() + TestData.minimalMenu.items) {
    val customer = scenario.newCustomer()

    val order = customer.canOrder(items, items.fold(Money(0.0)){ total, item -> total + item.price})
    val paymentId = customer.canPay(order, PaymentType.Paypal)
}

