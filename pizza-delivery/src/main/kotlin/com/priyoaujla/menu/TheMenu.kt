package com.priyoaujla.menu

import com.priyoaujla.order.Money

class TheMenu(
    private val menuStorage: MenuStorage
) {

    fun fetch(): Menu = menuStorage.get()

    fun createItem(item: Menu.Item, price: Money) {
        menuStorage.add(item, price)
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