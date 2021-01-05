package componenttests.com.priyoaujla.domain.components

import componenttests.com.priyoaujla.System
import componenttests.com.priyoaujla.TestData.minimalMenu
import org.junit.jupiter.api.Test

class MenuTests {

    private val scenario = System()
    private val customer = scenario.newCustomer()

    @Test
    fun `customer can view the menu`() {
        customer.canSeeMenuWith(minimalMenu.items)
    }
}

