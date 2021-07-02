package com.priyoaujla.domain.components.ordering.payment

import com.priyoaujla.domain.components.checkout.TransactionId
import com.priyoaujla.domain.components.ordering.Money

interface Paypal {
    fun pay(transactionId: TransactionId, total: Money): PaymentId
}