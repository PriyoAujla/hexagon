package com.priyoaujla.domain.components.checkout

import com.priyoaujla.domain.components.menu.Menu
import com.priyoaujla.transaction.Transactor
import java.util.*

class Basket(private val customerBasketStorageTransactor: Transactor<CustomerBasketStorage>) {

    fun add(customerId: CustomerId, items: List<Menu.MenuItem>) {
        customerBasketStorageTransactor.perform { customerBasketStorage ->
            val customerBasket = customerBasketStorage.retrieve(customerId) ?: CustomerBasket.empty(customerId)
            val updatedCustomerBasket = customerBasket.add(items)
            customerBasketStorage.save(updatedCustomerBasket)
        }
    }

    fun fetch(customerId: CustomerId): CustomerBasket? =
        customerBasketStorageTransactor.perform { customerBasketStorage ->
            customerBasketStorage.retrieve(customerId)
        }

}

interface CustomerBasketStorage {
    fun retrieve(customerId: CustomerId): CustomerBasket?
    fun save(customerBasket: CustomerBasket)
}

data class CustomerBasket(val customerId: CustomerId, val items: List<Menu.MenuItem>) {
    fun add(items: List<Menu.MenuItem>): CustomerBasket {
        return copy(items = this.items + items)
    }

    companion object {
        fun empty(customerId: CustomerId) = CustomerBasket(customerId, emptyList())
    }
}

data class CustomerId(val value: String) {
    companion object {
        fun mint() = CustomerId(UUID.randomUUID().toString())
    }
}