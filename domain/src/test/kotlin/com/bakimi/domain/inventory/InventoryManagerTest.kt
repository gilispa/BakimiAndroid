package com.bakimi.domain.inventory

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InventoryManagerTest {

    /** In-memory repository that yields the thread between reading and writing, like a database would. */
    private class InMemoryRepository : InventoryRepository {
        private val items = MutableStateFlow<Map<Long, InventoryItem>>(emptyMap())
        private var nextId = 1L

        override fun observeAll(): Flow<List<InventoryItem>> = TODO("No se usa en estas pruebas")

        override suspend fun get(id: Long): InventoryItem? {
            yield()
            return items.value[id]
        }

        override suspend fun save(item: InventoryItem): Long {
            yield()
            val id = if (item.id == 0L) nextId++ else item.id
            items.value = items.value + (id to item.copy(id = id))
            return id
        }

        override suspend fun delete(id: Long) {
            items.value = items.value - id
        }

        fun current(id: Long): InventoryItem? = items.value[id]
    }

    private val repository = InMemoryRepository()
    private val manager = InventoryManager(repository)

    private fun created(
        name: String = "Huevo",
        quantity: Double = 12.0,
        unit: MeasureUnit = MeasureUnit.PIECE,
    ) = InventoryItem(name = name, quantity = quantity, unit = unit, location = StorageLocation.FRIDGE)

    private fun saveValid(item: InventoryItem): Long = runBlocking {
        (manager.save(item) as SaveResult.Saved).id
    }

    @Test
    fun `saving a valid food persists it with a trimmed name`() = runBlocking {
        val result = manager.save(created(name = "  Pechuga de pollo  "))

        val id = (result as SaveResult.Saved).id
        assertEquals("Pechuga de pollo", repository.current(id)!!.name)
    }

    @Test
    fun `saving an invalid food persists nothing`() = runBlocking {
        val result = manager.save(created(name = ""))

        assertEquals(SaveResult.Invalid(setOf(ItemError.EMPTY_NAME)), result)
        assertNull(repository.current(1))
    }

    @Test
    fun `increasing adds the unit quick step`() = runBlocking {
        val id = saveValid(created(quantity = 1.0, unit = MeasureUnit.KILOGRAM))

        manager.adjust(id, steps = 1)

        assertEquals(1.5, repository.current(id)!!.quantity, 0.0)
    }

    @Test
    fun `subtracting too much leaves the food depleted without a negative quantity`() = runBlocking {
        val id = saveValid(created(quantity = 50.0, unit = MeasureUnit.GRAM))

        manager.adjust(id, steps = -1)

        val item = repository.current(id)!!
        assertEquals(0.0, item.quantity, 0.0)
        assertTrue(item.depleted)
    }

    @Test
    fun `repeated adjustments do not accumulate floating point errors`() = runBlocking {
        val id = saveValid(created(quantity = 0.1, unit = MeasureUnit.LITER))

        repeat(3) { manager.adjust(id, steps = 1) }

        assertEquals(1.6, repository.current(id)!!.quantity, 0.0)
    }

    @Test
    fun `simultaneous quick taps do not lose adjustments`() = runBlocking {
        val id = saveValid(created(quantity = 0.0))

        (1..10).map { async { manager.adjust(id, steps = 1) } }.awaitAll()

        assertEquals(10.0, repository.current(id)!!.quantity, 0.0)
    }

    @Test
    fun `adjusting a food that does not exist returns null`() = runBlocking {
        assertNull(manager.adjust(99, steps = 1))
    }

    @Test
    fun `marking depleted keeps the record with zero quantity`() = runBlocking {
        val id = saveValid(created(quantity = 6.0))

        manager.markDepleted(id)

        val item = repository.current(id)!!
        assertEquals("Huevo", item.name)
        assertTrue(item.depleted)
    }

    @Test
    fun `deleting removes the record`() = runBlocking {
        val id = saveValid(created())

        manager.delete(id)

        assertNull(repository.current(id))
    }
}
