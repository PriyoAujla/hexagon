package com.priyoaujla

import org.junit.Test

class PizzaDeliveryTest {

    private val scenario = Scenario()

    @Test
    fun `the courier is able to see the order once it has been cooked`() = HasFinishedCookingAnOrder(scenario).runTest {
        courier.theNextDeliveryIs(order)
    }

    @Test
    fun `the courier can notify when the order has been delivered`() = HasFinishedCookingAnOrder(scenario).runTest {
        val delivery = courier.theNextDeliveryIs(order)
        courier.hasDelivered(delivery)
        customer.canSeeOrderStatus(delivery.orderId, Order.Status.Delivered)
    }

}

class HasFinishedCookingAnOrder(scenario: Scenario, items: List<Menu.MenuItem> = TestData.minimalMenu.items.toList() + TestData.minimalMenu.items) {

    private val orderFixture = HasPaidForAnOrder(scenario, items)

    val customer get() = orderFixture.customer
    val order get() = orderFixture.order

    val chef = scenario.newChef()
    val courier = scenario.newCourier()

    init {
        chef.canFinishCooking(Ticket.from(order))
    }
}