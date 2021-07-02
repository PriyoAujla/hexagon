package systemtests.com.priyoaujla.domain.components

import com.priyoaujla.domain.components.ordering.Money
import com.priyoaujla.domain.components.ordering.orderstatus.OrderStatus
import org.junit.jupiter.api.Test
import systemtests.com.priyoaujla.CourierRole.DeliveryDetails
import systemtests.com.priyoaujla.Scenarios
import systemtests.com.priyoaujla.TestData
import systemtests.com.priyoaujla.TheSystem

class DeliveryTests {

    private val theSystem = TheSystem()
    private val courier = theSystem.newCourier()

    @Test
    fun `the courier is able to see the order once it has been cooked`() {
        val basketItems = TestData.minimalMenu.items.toList() + TestData.minimalMenu.items
        Scenarios.paypalCookedOrder(theSystem, basketItems = basketItems)
        courier.theNextDeliveryIs(DeliveryDetails(basketItems, Money(17.96)))
    }

    @Test
    fun `the customer can see the order has been delivered`() {
        val basketItems = TestData.minimalMenu.items.toList() + TestData.minimalMenu.items
        val customer = theSystem.newCustomer()
        Scenarios.paypalCookedOrder(theSystem, customer = customer,  basketItems = basketItems)

        val delivery = courier.theNextDeliveryIs(DeliveryDetails(basketItems, Money(17.96)))
        courier.hasDelivered(delivery)
        customer.canSeeOrderStatus(delivery.orderId, OrderStatus.Status.Delivered)
    }

    @Test
    fun `the courier must confirm payment is received for a cash on delivery order`() {
        val basketItems = TestData.minimalMenu.items.toList() + TestData.minimalMenu.items
        Scenarios.cashOnDeliveryCookedOrder(theSystem, basketItems = basketItems)

        val delivery = courier.theNextDeliveryIs(DeliveryDetails(basketItems, Money(17.96)))
        courier.canMarkDeliveryAsPaid(delivery.id)
        courier.hasDelivered(delivery)
    }

}