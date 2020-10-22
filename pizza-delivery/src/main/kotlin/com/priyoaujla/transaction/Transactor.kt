package com.priyoaujla.transaction

interface Transactor<R, T> {
    fun perform(runInTransaction: (R) -> T): T
}