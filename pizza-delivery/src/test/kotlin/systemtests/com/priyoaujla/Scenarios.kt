package systemtests.com.priyoaujla

import com.priyoaujla.domain.components.menu.Menu
import com.priyoaujla.domain.components.ordering.Money
import com.priyoaujla.domain.components.ordering.OrderId
import com.priyoaujla.domain.components.ordering.payment.PaymentType
import systemtests.com.priyoaujla.CustomerRole.OrderDetails
import systemtests.com.priyoaujla.CustomerRole.OrderDetails.PaymentStatus.Paid
import systemtests.com.priyoaujla.CustomerRole.OrderDetails.PaymentStatus.PaymentRequired

object Scenarios {

    fun cashOnDeliveryCookedOrder(
        theSystem: TheSystem,
        customer: CustomerRole? = null,
        chef: ChefRole? = null,
        basketItems: List<Menu.MenuItem> = TestData.minimalMenu.items.toList() + TestData.minimalMenu.items
    ) {
        cookedOrder(theSystem, customer, chef, basketItems, PaymentType.Cash)
    }

    fun paypalCookedOrder(
        theSystem: TheSystem,
        customer: CustomerRole? = null,
        chef: ChefRole? = null,
        basketItems: List<Menu.MenuItem> = TestData.minimalMenu.items.toList() + TestData.minimalMenu.items
    ) {
        cookedOrder(theSystem, customer, chef, basketItems, PaymentType.Paypal)
    }

    private fun cookedOrder(
        theSystem: TheSystem,
        customer: CustomerRole?,
        chef: ChefRole?,
        basketItems: List<Menu.MenuItem>,
        paymentType: PaymentType
    ) {
        val theCustomer = customer ?: theSystem.newCustomer()
        val theChef = chef ?: theSystem.newChef()
        checkoutBasket(theCustomer, basketItems, paymentType)
        cookTheNextTicket(theChef, basketItems)
    }

    private fun cookTheNextTicket(
        chef: ChefRole,
        basketItems: List<Menu.MenuItem>
    ) {
        chef.canPickupNextTicket(ChefRole.TicketDetails(basketItems)).also {
            chef.canFinishCooking(it)
        }
    }

    fun checkoutBasket(customer: CustomerRole, basketItems: List<Menu.MenuItem>, withPaymentType: PaymentType): OrderId {
        customer.canAddToBasket(basketItems)
        customer.canPayForBasket(withPaymentType)
        val expectedTotal = basketItems.fold(Money.ZERO) { acc, it -> acc.plus(it.price) }
        val expectedPaymentStatus = when(withPaymentType) {
            PaymentType.Paypal -> Paid
            PaymentType.Cash -> PaymentRequired
        }
        return customer.canSeeOrderWithDetails(OrderDetails(items = basketItems, total = expectedTotal, paymentStatus = expectedPaymentStatus))
    }
}