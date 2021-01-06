package systemtests.com.priyoaujla.domain.components

import org.junit.jupiter.api.Test
import systemtests.com.priyoaujla.TestData.minimalMenu
import systemtests.com.priyoaujla.TheSystem

class MenuTests {

    private val theSystem = TheSystem()
    private val customer = theSystem.newCustomer()

    @Test
    fun `customer can view the menu`() {
        customer.canSeeMenuWith(minimalMenu.items)
    }
}

