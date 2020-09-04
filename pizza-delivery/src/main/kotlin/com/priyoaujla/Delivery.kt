package com.priyoaujla

import java.util.*

class DeliveryHub(
    courier: UserDetails,
    private val orders: Orders,
    private val deliveryStorage: DeliveryStorage,
    private val deliveryComplete: (OrderId) -> Unit
) {
    fun nextDelivery(): Delivery = deliveryStorage.take()

    fun newDelivery(ticket: Ticket) {
        val orderId = ticket.orderId
        val order = orders.get(orderId) ?: error("Implement me!")
        deliveryStorage.upsert(Delivery(orderId = orderId, menuItem = order.items, total = order.total))
    }

    fun delivered(deliveryId: DeliveryId) {
        val delivery = deliveryStorage.get(deliveryId) ?: error("Implement me!")
        deliveryStorage.upsert(delivery.delivered())
        deliveryComplete(delivery.orderId)
    }

}

data class DeliveryId(val uuid: UUID){
    companion object {
        fun mint(): DeliveryId {
            return DeliveryId(UUID.randomUUID())
        }
    }
}

data class Delivery(
    val id: DeliveryId = DeliveryId.mint(),
    val orderId: OrderId,
    val menuItem: List<Menu.MenuItem>,
    val total: Money,
    val state: DeliveryState = DeliveryState.AwaitingDelivery
) {

    enum class DeliveryState {
        AwaitingDelivery, Delivered
    }

    fun delivered() = copy(state = DeliveryState.Delivered)
}

interface DeliveryStorage {
    fun get(id: DeliveryId): Delivery?
    fun upsert(delivery: Delivery)
    fun take(): Delivery
}