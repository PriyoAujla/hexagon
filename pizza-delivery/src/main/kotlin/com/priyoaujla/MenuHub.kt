package com.priyoaujla

class MenuHub(
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