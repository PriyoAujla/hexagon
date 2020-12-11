package componenttests.com.priyoaujla

import componenttests.com.priyoaujla.TestData.minimalMenu
import org.junit.jupiter.api.Test

class MenuTests {

    private val scenario = Scenario()
    private val customer = scenario.newCustomer()

    @Test
    fun `customer can view the menu`() {
        customer.canSeeMenuWith(minimalMenu.items)
    }
}
