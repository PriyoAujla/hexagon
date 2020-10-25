package com.priyoaujla.transaction

interface Transactor<R> {
    fun <T> perform(runInTransaction: (R) -> T): T
}