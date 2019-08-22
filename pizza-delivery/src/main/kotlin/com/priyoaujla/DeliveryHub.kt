package com.priyoaujla

class DeliveryHub(
    courier: UserDetails,
    private val orders: Orders,
    private val deliveries: Deliveries,
    private val pickup: () -> Order
) {
    fun nextDelivery(): Delivery = deliveries.next()

    fun createDeliveryNote(orderId: OrderId) {
        orders.get(orderId)?.let {
            deliveries.create(Delivery.from(it))
        } ?: error("")

    }

}

data class Delivery(val orderId: OrderId, val menuItem: List<Menu.MenuItem>, val total: Money) {
    companion object {
        fun from(order: Order): Delivery = Delivery(order.id, order.items, order.total)
    }
}

class Deliveries(private val deliveryStorage: DeliveryStorage) {
    fun next(): Delivery = deliveryStorage.take()

    fun create(delivery: Delivery) {
        deliveryStorage.upsert(delivery)
    }
}

interface DeliveryStorage {

    fun upsert(delivery: Delivery)
    fun take(): Delivery
}