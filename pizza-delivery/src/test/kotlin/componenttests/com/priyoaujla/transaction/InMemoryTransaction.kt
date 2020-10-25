package componenttests.com.priyoaujla.transaction

import com.priyoaujla.transaction.Transactor
import java.util.concurrent.atomic.AtomicReference

class InMemoryTransactor<R, S: R> private constructor(
        private val cloneableStorage: AtomicReference<S>,
        private val cloneOf: (S) -> S
): Transactor<R> {

    override fun <T> perform(runInTransaction: (R) -> T): T {
        try {
            val original = cloneableStorage.get()
            val snapshot = cloneOf(original)
            val result = runInTransaction(snapshot)
            if(! cloneableStorage.compareAndSet(original, snapshot)) {
                throw error("Concurrency violation: storage has changed since taking a snapshot")
            }
            return result
        } catch(e: Exception) {
            throw e
        }
    }

    companion object {
        fun <R, S: R> of(storage: S, clone: (S) -> S): InMemoryTransactor<R, S> {
            return InMemoryTransactor(AtomicReference(storage), clone)
        }
    }
}