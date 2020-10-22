package com.priyoaujla.domain.delivery

import com.priyoaujla.domain.order.*

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