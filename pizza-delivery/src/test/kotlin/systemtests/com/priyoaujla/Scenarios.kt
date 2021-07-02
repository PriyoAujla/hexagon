package systemtests.com.priyoaujla

import com.priyoaujla.domain.components.menu.Menu
import com.priyoaujla.domain.components.ordering.Money
import com.priyoaujla.domain.components.ordering.OrderId
import com.priyoaujla.domain.components.ordering.payment.PaymentType
import systemtests.com.priyoaujla.CustomerRole.OrderDetails
import systemtests.com.priyoaujla.CustomerRole.OrderDetails.PaymentStatus.Paid
import systemtests.com.priyoaujla.CustomerRole.OrderDetails.PaymentStatus.PaymentRequired

class TwoSeparatePaidOrdersScenario(theSystem: TheSystem) {
    private val pair = PaypalPaidScenario(theSystem) to PaypalPaidScenario(
        theSystem,
        TestData.minimalMenu.items.take(1)
    )

    val firstCustomer = pair.first.customer
    val firstCustomerItemsOrdered = pair.first.basketItems
    val firstCustomerOrderId = pair.first.customer.canSeeOrderWithDetails(OrderDetails(firstCustomerItemsOrdered, Money(17.96), Paid))

    val secondCustomer = pair.second.customer
    val secondCustomerItemsOrdered = pair.second.basketItems
    val secondCustomerOrderId = pair.second.customer.canSeeOrderWithDetails(OrderDetails(secondCustomerItemsOrdered, Money(3.99), Paid))
}

abstract class OrderScenario {
    abstract val customer: CustomerRole

    protected fun createOrder(withItems: List<Menu.MenuItem>, withPaymentMethod: PaymentType) {
        customer.canAddToBasket(withItems)
        customer.canPayForBasket(withPaymentMethod)
    }
}

class PaypalPaidScenario(
    theSystem: TheSystem,
    val basketItems: List<Menu.MenuItem> = TestData.minimalMenu.items.toList() + TestData.minimalMenu.items
) : OrderScenario() {

    override val customer = theSystem.newCustomer()

    init {
        createOrder(basketItems, PaymentType.Paypal)
    }

}

object Scenarios {

    fun cashOnDeliveryCookedOrder(
        theSystem: TheSystem,
        customer: CustomerRole? = null,
        chef: ChefRole? = null,
        basketItems: List<Menu.MenuItem> = TestData.minimalMenu.items.toList() + TestData.minimalMenu.items
    ) {
        cookedOrder(customer, theSystem, chef, basketItems, PaymentType.Cash)
    }

    fun paypalCookedOrder(
        theSystem: TheSystem,
        customer: CustomerRole? = null,
        chef: ChefRole? = null,
        basketItems: List<Menu.MenuItem> = TestData.minimalMenu.items.toList() + TestData.minimalMenu.items
    ) {
        cookedOrder(customer, theSystem, chef, basketItems, PaymentType.Paypal)
    }

    private fun cookedOrder(
        customer: CustomerRole?,
        theSystem: TheSystem,
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