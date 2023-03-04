package systemtests.com.priyoaujla

import com.priyoaujla.domain.components.menu.Menu
import com.priyoaujla.domain.components.ordering.Money
import systemtests.com.priyoaujla.TestData.Ingredients.basil
import systemtests.com.priyoaujla.TestData.Ingredients.mozzarella
import systemtests.com.priyoaujla.TestData.Ingredients.pizzaDough
import systemtests.com.priyoaujla.TestData.Ingredients.tomatoSauce
import kotlin.random.Random
import kotlin.random.nextInt

object TestData {

    object Ingredients {
        val pizzaDough = Menu.Ingredient("Pizza Dough")
        val tomatoSauce = Menu.Ingredient("Tomato Sauce")
        val mozzarella = Menu.Ingredient("Mozzarella Cheese")
        val basil = Menu.Ingredient("Basil")
    }

    object Pizzas {
        val plainPizza =
            Menu.Item(
                Menu.Name("Plain Pizza"),
                listOf(pizzaDough, tomatoSauce)
            )
        val margarita = plainPizza
            .withName("Margarita Pizza")
            .addIngredient(mozzarella)
            .addIngredient(basil)
    }

    private fun Menu.Item.withName(name: String): Menu.Item {
        return copy(name = Menu.Name(name))
    }

    private fun Menu.Item.addIngredient(ingredient: Menu.Ingredient): Menu.Item {
        return copy(ingredients = ingredients + ingredient)
    }

    val minimalMenu = Menu(
        items = setOf(
            Menu.MenuItem(
                Pizzas.plainPizza,
                Money(3.99)
            ),
            Menu.MenuItem(
                Pizzas.margarita,
                Money(4.99)
            )
        )
    )

    fun randomItemsToOrder(): Pair<List<Menu.MenuItem>, Money> {
        val numberOfItems = Random.nextInt(1..10)
        val menuItemList = minimalMenu.items.toList()
        val menuItems = (1..numberOfItems).map {
            menuItemList[Random.nextInt(menuItemList.indices)]
        }
        val cost = menuItems.fold(Money.ZERO) { acc, it ->
            acc.plus(it.price)
        }
        return Pair(menuItems, cost)
    }
}