package com.priyoaujla.domain.delivery

import com.priyoaujla.domain.order.*
import com.priyoaujla.transaction.Transactor

class Delivery(
    private val transactor: Transactor<Pair<DeliveryStorage, NotifyDelivered>>
) {
    fun nextDelivery(): DeliveryNote {
        return transactor.perform { (deliveryStorage) ->
            deliveryStorage.take()
        }
    }

    fun newDelivery(deliveryNote: DeliveryNote) {
        return transactor.perform { (deliveryStorage) ->
            deliveryStorage.upsert(deliveryNote)
        }
    }

    fun delivered(deliveryId: DeliveryId) {
        transactor.perform { (deliveryStorage, notifyDelivered) ->
            val delivery = deliveryStorage.get(deliveryId) ?: error("Implement me!")
            deliveryStorage.upsert(delivery.delivered())
            notifyDelivered(delivery)
        }
    }
}

class NotifyDelivered(val orderProgressUpdater: (OrderId, OrderStatus.Status) -> Unit): (DeliveryNote) -> Unit {
    override fun invoke(deliveryNote: DeliveryNote) {
        orderProgressUpdater(deliveryNote.orderId, OrderStatus.Status.Delivered)
    }
}