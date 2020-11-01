package com.priyoaujla.domain.order

import com.priyoaujla.domain.menu.Menu
import com.priyoaujla.domain.order.payment.PaymentId
import java.util.*

data class Order(
        val id: OrderId = OrderId.mint(),
        val items: List<Menu.MenuItem> = emptyList(),
        val total: Money,
        val paymentStatus: PaymentStatus = PaymentStatus.PaymentRequired
) {
    fun paid(paymentId: PaymentId): Order = copy(paymentStatus = PaymentStatus.Paid(paymentId))

}

sealed class PaymentStatus {
    object PaymentRequired: PaymentStatus()
    data class Paid(val paymentId: PaymentId): PaymentStatus()
}

data class OrderId(val uuid: UUID) {
    companion object {
        fun mint(): OrderId {
            return OrderId(UUID.randomUUID())
        }
    }
}