package com.bakimi.domain.cooking

import com.bakimi.domain.ingredients.Ingredient
import com.bakimi.domain.inventory.InventoryRepository
import com.bakimi.domain.inventory.InventoryItem
import com.bakimi.domain.inventory.StorageLocation
import com.bakimi.domain.inventory.MeasureUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CookingManagerTest {

    private val egg = Ingredient("huevo", "Huevo")
    private val inventory = InMemoryInventory(
        InventoryItem(1, "Huevo", 12.0, MeasureUnit.PIECE, StorageLocation.FRIDGE, ingredientId = "huevo"),
    )
    private val preparations = InMemoryPreparations(inventory)
    private val manager = CookingManager(inventory, preparations, now = { 1_000L })
    private val usedItems = listOf(UsedIngredient(egg, 4.0, MeasureUnit.PIECE))

    @Test
    fun `calculating the deduction does not change the inventory`() = runBlocking {
        val consumption = manager.calculate(usedItems)

        assertEquals(8.0, consumption.changes.single().quantity, 0.0)
        assertEquals(12.0, inventory.items.value.single().quantity, 0.0)
    }

    @Test
    fun `confirming deducts and registers the preparation`() = runBlocking {
        val result = manager.confirm("sesion-1", "omelette", servings = 2, usedItems = usedItems)

        assertTrue(result is PreparationResult.Registered)
        assertEquals(8.0, inventory.items.value.single().quantity, 0.0)
        assertEquals(listOf(Preparation("sesion-1", "omelette", 2, 1_000L)), preparations.registeredPreparations)
    }

    @Test
    fun `a repeated confirmation does not duplicate the deduction`() = runBlocking {
        manager.confirm("sesion-1", "omelette", servings = 2, usedItems = usedItems)

        val second = manager.confirm("sesion-1", "omelette", servings = 2, usedItems = usedItems)

        assertEquals(PreparationResult.AlreadyRegistered, second)
        assertEquals(8.0, inventory.items.value.single().quantity, 0.0)
    }

    @Test
    fun `another session of the same recipe does deduct`() = runBlocking {
        manager.confirm("sesion-1", "omelette", servings = 2, usedItems = usedItems)
        manager.confirm("sesion-2", "omelette", servings = 2, usedItems = usedItems)

        assertEquals(4.0, inventory.items.value.single().quantity, 0.0)
    }

    private class InMemoryInventory(vararg iniciales: InventoryItem) : InventoryRepository {
        val items = MutableStateFlow(iniciales.toList())

        override fun observeAll(): Flow<List<InventoryItem>> = items
        override suspend fun get(id: Long) = items.value.find { it.id == id }
        override suspend fun save(item: InventoryItem): Long {
            items.value = items.value.filter { it.id != item.id } + item
            return item.id
        }
        override suspend fun delete(id: Long) {
            items.value = items.value.filter { it.id != id }
        }
    }

    private class InMemoryPreparations(private val inventory: InMemoryInventory) : PreparationRepository {
        val registeredPreparations = mutableListOf<Preparation>()

        override suspend fun register(preparation: Preparation, changes: List<InventoryItem>): Boolean {
            if (registeredPreparations.any { it.id == preparation.id }) return false
            registeredPreparations += preparation
            changes.forEach { inventory.save(it) }
            return true
        }
    }
}
