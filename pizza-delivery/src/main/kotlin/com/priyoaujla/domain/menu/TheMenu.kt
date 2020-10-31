package com.priyoaujla.domain.menu

import com.priyoaujla.domain.order.Money
import com.priyoaujla.transaction.Transactor

class TheMenu(
    private val transactor: Transactor<MenuStorage>
) {

    fun fetch(): Menu = transactor.perform { menuStorage -> menuStorage.get() }

    fun createItem(item: Menu.Item, price: Money) {
        transactor.perform { menuStorage ->
            menuStorage.add(item, price)
        }
    }
}

interface MenuStorage {
    fun get(): Menu

    fun add(item: Menu.Item, price: Money)
}


data class Menu(val items: Set<MenuItem>) {
    data class MenuItem(val item: Item, val price: Money)
    data class Item(val name: Name, val ingredients: List<Ingredient>)
    data class Name(val value: String)
    data class Ingredient(val value: String)
}