package com.priyoaujla.delivery

import com.priyoaujla.menu.Menu
import com.priyoaujla.order.*
import java.util.*

class Delivery(
        private val deliveryStorage: DeliveryStorage,
        private val notifyDelivered: NotifyDelivered
) {
    fun nextDelivery(): DeliveryNote = deliveryStorage.take()

    fun newDelivery(deliveryNote: DeliveryNote) {
        deliveryStorage.upsert(deliveryNote)
    }

    fun delivered(deliveryId: DeliveryId) {
        val delivery = deliveryStorage.get(deliveryId) ?: error("Implement me!")
        deliveryStorage.upsert(delivery.delivered())
        notifyDelivered(delivery)
    }
}

class NotifyDelivered(val orderProgressUpdater: (OrderId, OrderStatus.Status) -> Unit): (DeliveryNote) -> Unit {
    override fun invoke(deliveryNote: DeliveryNote) {
        orderProgressUpdater(deliveryNote.orderId, OrderStatus.Status.Delivered)
    }
}


data class DeliveryId(val uuid: UUID){
    companion object {
        fun mint(): DeliveryId {
            return DeliveryId(UUID.randomUUID())
        }
    }
}

data class DeliveryNote(
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
    fun get(id: DeliveryId): DeliveryNote?
    fun upsert(deliveryNote: DeliveryNote)
    fun take(): DeliveryNote
}