package com.priyoaujla.domain.components.order

import java.util.*

data class UserDetails(
        val userId: UserId,
        val givenName: GivenName,
        val familyName: FamilyName,
        val address: Address
)

data class GivenName(val value: String)
data class FamilyName(val value: String)
data class Address(val value: String)

data class UserId(val value: UUID) {
    companion object {
        fun mint(): UserId {
            return UserId(UUID.randomUUID())
        }
    }
}