package com.priyoaujla.domain.delivery

interface DeliveryStorage {
    fun get(id: DeliveryId): DeliveryNote?
    fun upsert(deliveryNote: DeliveryNote)
    fun take(): DeliveryNote
}