package com.priyoaujla.domain.components.checkout

import com.priyoaujla.domain.components.menu.Menu
import com.priyoaujla.domain.components.ordering.Money
import com.priyoaujla.domain.components.ordering.Ordering
import com.priyoaujla.domain.components.ordering.PaymentConfirmationType.Cash
import com.priyoaujla.domain.components.ordering.payment.PaymentInstructions
import com.priyoaujla.domain.components.ordering.payment.PaymentType
import com.priyoaujla.transaction.Transactor
import java.util.*

class Basket(private val customerBasketStorageTransactor: Transactor<Pair<CustomerBasketStorage, NotifyOnCheckout>>) {

    fun add(customerId: CustomerId, items: List<Menu.MenuItem>) {
        customerBasketStorageTransactor.perform { (customerBasketStorage) ->
            val customerBasket = customerBasketStorage.retrieve(customerId) ?: CustomerBasket.empty(customerId)
            val updatedCustomerBasket = customerBasket.add(items)
            customerBasketStorage.save(updatedCustomerBasket)
        }
    }

    fun fetch(customerId: CustomerId): CustomerBasket? =
        customerBasketStorageTransactor.perform { (customerBasketStorage) ->
            customerBasketStorage.retrieve(customerId)
        }

    fun payment(customerId: CustomerId, paymentType: PaymentType): PaymentInstructions {
        return customerBasketStorageTransactor.perform { (customerBasketStorage, notifyOnCheckout) ->
            val basket = customerBasketStorage.retrieve(customerId) ?: error("")
            val checkoutCustomerBasket = CheckoutCustomerBasket(TransactionId.mint(), paymentType, basket)
            val instructions = when (paymentType) {
                PaymentType.Paypal -> {
                    PaymentInstructions.RedirectToPaypal(checkoutCustomerBasket = checkoutCustomerBasket)
                }
                PaymentType.Cash -> {
                    PaymentInstructions.NoInstructions(checkoutCustomerBasket = checkoutCustomerBasket)
                }
            }
            notifyOnCheckout(checkoutCustomerBasket)
            instructions
        }
    }

}

interface CustomerBasketStorage {
    fun retrieve(customerId: CustomerId): CustomerBasket?
    fun save(customerBasket: CustomerBasket)

    fun clear(customerId: CustomerId)
}

interface NotifyOnCheckout: (CheckoutCustomerBasket) -> Unit {
    companion object {
        fun instance(ordering: Ordering, customerBasketStorage: CustomerBasketStorage) = object : NotifyOnCheckout {
            override fun invoke(checkoutCustomerBasket: CheckoutCustomerBasket) {
                ordering.create(
                    checkoutCustomerBasket.transactionId,
                    checkoutCustomerBasket.basket.items,
                    checkoutCustomerBasket.basket.total(),
                    checkoutCustomerBasket.paymentType
                )
                if (checkoutCustomerBasket.paymentType == PaymentType.Cash) {
                    ordering.paymentConfirmed(checkoutCustomerBasket.transactionId, Cash)
                }
                customerBasketStorage.clear(checkoutCustomerBasket.basket.customerId)
            }
        }
    }
}

data class CustomerBasket(
    val customerId: CustomerId,
    val items: List<Menu.MenuItem>
) {
    fun add(items: List<Menu.MenuItem>): CustomerBasket {
        return copy(items = this.items + items)
    }

    fun total(): Money = items.fold(Money.ZERO) { acc, it -> acc.plus(it.price) }

    companion object {
        fun empty(customerId: CustomerId) = CustomerBasket(customerId, emptyList())
    }
}

data class CheckoutCustomerBasket(
    val transactionId: TransactionId,
    val paymentType: PaymentType,
    val basket: CustomerBasket
)

data class CustomerId(val value: String) {
    companion object {
        fun mint() = CustomerId(UUID.randomUUID().toString())
    }
}

data class TransactionId(val value: String) {
    companion object {
        fun mint() = TransactionId(UUID.randomUUID().toString())
    }
}