package componenttests.com.priyoaujla.transaction

import com.priyoaujla.transaction.Transactor

class InMemoryTransactor<R> constructor(
        private val factory: () -> R
): Transactor<R> {

    override fun <T> perform(runInTransaction: (R) -> T): T {
        try {
            return runInTransaction(factory())
        } catch(e: Exception) {
            throw e
        }
    }
}