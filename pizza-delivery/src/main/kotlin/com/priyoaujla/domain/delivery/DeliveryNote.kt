package com.priyoaujla.domain.delivery

import com.priyoaujla.domain.menu.Menu
import com.priyoaujla.domain.order.Money
import com.priyoaujla.domain.order.OrderId
import java.util.*

data class DeliveryNote(
        val id: DeliveryId = DeliveryId.mint(),
        val orderId: OrderId,
        val menuItem: List<Menu.MenuItem>,
        val total: Money,
        val state: DeliveryState = DeliveryState.AwaitingDelivery,
        val paymentStatus: PaymentStatus
) {
    enum class PaymentStatus {
        Paid, PaymentRequired
    }

    enum class DeliveryState {
        AwaitingDelivery, Delivered
    }

    fun delivered() = copy(state = DeliveryState.Delivered)
    fun paid(): DeliveryNote {
        if(paymentStatus == PaymentStatus.PaymentRequired) {
            return copy(paymentStatus = PaymentStatus.Paid)
        } else {
            error("")
        }
    }
}

data class DeliveryId(val uuid: UUID){
    companion object {
        fun mint(): DeliveryId {
            return DeliveryId(UUID.randomUUID())
        }
    }
}