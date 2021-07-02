package com.priyoaujla.domain.components.ordering.payment

import com.priyoaujla.domain.components.checkout.CheckoutCustomerBasket
import com.priyoaujla.domain.components.ordering.Order

sealed class PaymentInstructions {
    abstract val order: Order?
    abstract val checkoutCustomerBasket: CheckoutCustomerBasket?
    data class RedirectToPaypal(override val order: Order? = null, override val checkoutCustomerBasket: CheckoutCustomerBasket? = null) : PaymentInstructions()
    data class NoInstructions(override val order: Order? = null, override val checkoutCustomerBasket: CheckoutCustomerBasket? = null): PaymentInstructions()
}