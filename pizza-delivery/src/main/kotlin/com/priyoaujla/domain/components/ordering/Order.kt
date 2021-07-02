package com.priyoaujla.domain.components.ordering

import com.priyoaujla.domain.components.checkout.TransactionId
import com.priyoaujla.domain.components.menu.Menu
import com.priyoaujla.domain.components.ordering.payment.PaymentId
import com.priyoaujla.domain.components.ordering.payment.PaymentType
import java.util.*

data class Order(
        val id: OrderId = OrderId.mint(),
        val transactionId: TransactionId,
        val items: List<Menu.MenuItem> = emptyList(),
        val total: Money,
        val paymentType: PaymentType,
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