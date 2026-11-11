package com.bakimi.domain.shopping

import com.bakimi.domain.ingredients.Category
import com.bakimi.domain.ingredients.PurchaseInfo
import com.bakimi.domain.ingredients.Ingredient
import com.bakimi.domain.ingredients.IngredientRepository
import com.bakimi.domain.inventory.InventoryRepository
import com.bakimi.domain.inventory.InventoryItem
import com.bakimi.domain.inventory.MeasureUnit
import com.bakimi.domain.recipes.Recipe
import com.bakimi.domain.recipes.RecipeRepository
import com.bakimi.domain.recipes.needs
import com.bakimi.domain.recipes.recipe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ShoppingManagerTest {

    private val rice = Ingredient(
        "arroz",
        "Arroz",
        category = Category.PANTRY,
        suggestedUnit = MeasureUnit.GRAM,
        purchase = PurchaseInfo(MeasureUnit.GRAM, 1000.0, "bolsa de 1 kg", 34.0),
    )
    private val onion = Ingredient("cebolla", "Cebolla", category = Category.PRODUCE)
    private val redRice = recipe(
        "arroz_rojo",
        baseServings = 2,
        ingredients = arrayOf(needs(rice, 200.0, MeasureUnit.GRAM), needs(onion, 0.5, MeasureUnit.PIECE)),
    )

    private val inventory = InMemoryInventory()
    private val planRepo = InMemoryPlan()
    private val shoppingRepo = InMemoryShopping(inventory)
    private val manager = ShoppingManager(
        planRepo,
        shoppingRepo,
        object : RecipeRepository {
            override fun all() = listOf(redRice)
            override fun get(id: String): Recipe? = all().find { it.id == id }
        },
        object : IngredientRepository {
            override fun all() = listOf(rice, onion)
            override fun get(id: String) = all().find { it.id == id }
        },
        inventory,
    )

    private fun week() = runBlocking { manager.observeWeek().first() }

    @Test
    fun `adding to the plan uses the number of people as servings`() = runBlocking {
        manager.saveSettings(PlanSettings(people = 3))
        manager.addToPlan("arroz_rojo")

        assertEquals(3, week().recipes.single().servings)
    }

    @Test
    fun `the list is generated from the plan`() = runBlocking {
        manager.addToPlan("arroz_rojo", servings = 2)

        assertEquals(listOf("plan:arroz", "plan:cebolla"), week().shoppingList.lines.map { it.key })
    }

    @Test
    fun `marking a purchase does not change the inventory`() = runBlocking {
        manager.addToPlan("arroz_rojo", servings = 2)
        manager.markPurchased("plan:arroz", true)

        assertTrue(inventory.items.value.isEmpty())
        assertTrue(week().shoppingList.lines.single { it.key == "plan:arroz" }.purchased)
    }

    @Test
    fun `a partial purchase adds only what was checked and leaves the rest pending`() = runBlocking {
        manager.addToPlan("arroz_rojo", servings = 2)
        manager.markPurchased("plan:arroz", true)

        assertEquals(1, manager.finishShopping())

        val riceInFridge = inventory.items.value.single()
        assertEquals("arroz", riceInFridge.ingredientId)
        assertEquals(1000.0, riceInFridge.quantity, 0.0)
        assertEquals(listOf("plan:cebolla"), week().shoppingList.lines.map { it.key })
    }

    @Test
    fun `repeating the confirmation does not duplicate the purchase`() = runBlocking {
        manager.addToPlan("arroz_rojo", servings = 2)
        manager.markPurchased("plan:arroz", true)
        manager.finishShopping()

        assertEquals(0, manager.finishShopping())
        assertEquals(1000.0, inventory.items.value.single().quantity, 0.0)
    }

    @Test
    fun `the corrected actual quantity is added`() = runBlocking {
        manager.addToPlan("arroz_rojo", servings = 2)
        manager.correct("plan:arroz", quantity = 2000.0, price = 60.0)
        manager.markPurchased("plan:arroz", true)
        manager.finishShopping()

        assertEquals(2000.0, inventory.items.value.single().quantity, 0.0)
    }

    @Test
    fun `a manual product is added and leaves the list`() = runBlocking {
        manager.addManual(ManualItem(ingredientId = null, name = "Café", quantity = 1.0, unit = MeasureUnit.PIECE, price = 80.0))
        val key = week().shoppingList.lines.single().key
        manager.markPurchased(key, true)

        assertEquals(1, manager.finishShopping())
        assertEquals("Café", inventory.items.value.single().name)
        assertTrue(week().shoppingList.lines.isEmpty())
    }

    @Test
    fun `servings stay between 1 and 12`() = runBlocking {
        manager.addToPlan("arroz_rojo", servings = 2)
        manager.changeServings("arroz_rojo", 40)

        assertEquals(12, week().recipes.single().servings)
    }

    @Test
    fun `the budget is compared with the list total`() = runBlocking {
        manager.saveSettings(PlanSettings(people = 2, budget = 20.0))
        manager.addToPlan("arroz_rojo", servings = 2)

        assertEquals(BudgetStatus.EXCEEDED, week().budgetStatus)
    }

    private class InMemoryInventory : InventoryRepository {
        val items = MutableStateFlow<List<InventoryItem>>(emptyList())
        private var next = 1L

        override fun observeAll(): Flow<List<InventoryItem>> = items
        override suspend fun get(id: Long) = items.value.find { it.id == id }
        override suspend fun save(item: InventoryItem): Long {
            val id = if (item.id == 0L) next++ else item.id
            items.value = items.value.filter { it.id != id } + item.copy(id = id)
            return id
        }
        override suspend fun delete(id: Long) {
            items.value = items.value.filter { it.id != id }
        }
    }

    private class InMemoryPlan : PlanRepository {
        val config = MutableStateFlow(PlanSettings())
        val recipes = MutableStateFlow<List<PlanEntry>>(emptyList())

        override fun observeSettings(): Flow<PlanSettings> = config
        override fun observeRecipes(): Flow<List<PlanEntry>> = recipes
        override suspend fun saveSettings(config: PlanSettings) {
            this.config.value = config
        }
        override suspend fun saveRecipe(entry: PlanEntry) {
            val current = recipes.value
            recipes.value = if (current.any { it.recipeId == entry.recipeId }) {
                current.map { if (it.recipeId == entry.recipeId) entry else it }
            } else {
                current + entry
            }
        }
        override suspend fun removeRecipe(recipeId: String) {
            recipes.value = recipes.value.filter { it.recipeId != recipeId }
        }
    }

    private class InMemoryShopping(private val inventory: InMemoryInventory) : ShoppingRepository {
        val marks = MutableStateFlow<Map<String, PurchaseMark>>(emptyMap())
        val manualItems = MutableStateFlow<Map<Long, ManualItem>>(emptyMap())
        private var next = 1L

        override fun observeMarks(): Flow<List<PurchaseMark>> = marks.map { it.values.toList() }
        override fun observeManuals(): Flow<List<ManualItem>> = manualItems.map { it.values.toList() }
        override suspend fun getMark(ingredientId: String) = marks.value[ingredientId]
        override suspend fun getManual(id: Long) = manualItems.value[id]
        override suspend fun saveMark(mark: PurchaseMark) {
            marks.value = marks.value + (mark.ingredientId to mark)
        }
        override suspend fun saveManual(purchaseItem: ManualItem): Long {
            val id = if (purchaseItem.id == 0L) next++ else purchaseItem.id
            manualItems.value = manualItems.value + (id to purchaseItem.copy(id = id))
            return id
        }
        override suspend fun deleteManual(id: Long) {
            manualItems.value = manualItems.value - id
        }
        override suspend fun registerPurchase(
            inventory: List<InventoryItem>,
            processedMarks: List<String>,
            processedManuals: List<Long>,
        ) {
            inventory.forEach { this.inventory.save(it) }
            marks.value = marks.value - processedMarks.toSet()
            manualItems.value = manualItems.value - processedManuals.toSet()
        }
    }
}
