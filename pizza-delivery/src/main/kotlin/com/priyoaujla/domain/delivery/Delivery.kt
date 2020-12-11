package com.priyoaujla.domain.delivery

import com.priyoaujla.domain.order.OrderId
import com.priyoaujla.domain.order.orderstatus.OrderStatus
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
            if(delivery.isPaid()) {
                deliveryStorage.upsert(delivery.delivered())
                notifyDelivered(delivery)
            } else {
                error("Payment required!")
            }
        }
    }

    private fun DeliveryNote.isPaid() =
        paymentStatus == DeliveryNote.PaymentStatus.Paid

    fun acceptedPaymentFor(deliveryId: DeliveryId) {
        transactor.perform { (deliveryStorage) ->
            val deliveryNote = deliveryStorage.get(deliveryId)
            deliveryNote?.let {
                deliveryStorage.upsert(it.paid())
            } ?: error("")
        }
    }
}

class NotifyDelivered(val orderProgressUpdater: (OrderId, OrderStatus.Status) -> Unit): (DeliveryNote) -> Unit {
    override fun invoke(deliveryNote: DeliveryNote) {
        orderProgressUpdater(deliveryNote.orderId, OrderStatus.Status.Delivered)
    }
}