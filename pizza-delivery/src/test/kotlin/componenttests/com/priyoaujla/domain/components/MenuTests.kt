package componenttests.com.priyoaujla.domain.components

import componenttests.com.priyoaujla.TestData.minimalMenu
import componenttests.com.priyoaujla.TheSystem
import org.junit.jupiter.api.Test

class MenuTests {

    private val theSystem = TheSystem()
    private val customer = theSystem.newCustomer()

    @Test
    fun `customer can view the menu`() {
        customer.canSeeMenuWith(minimalMenu.items)
    }
}

