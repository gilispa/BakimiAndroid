package com.bakimi.domain.shopping

import com.bakimi.domain.ingredients.Ingredient
import com.bakimi.domain.inventory.InventoryItem
import com.bakimi.domain.inventory.StorageLocation
import com.bakimi.domain.inventory.MeasureUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RestockTest {

    private val rice = Ingredient(
        "arroz",
        "Arroz",
        suggestedUnit = MeasureUnit.GRAM,
        suggestedLocation = StorageLocation.PANTRY,
    )
    private val chicken = Ingredient(
        "pechuga_pollo",
        "Pechuga de pollo",
        suggestedUnit = MeasureUnit.GRAM,
        suggestedLocation = StorageLocation.FRIDGE,
    )

    private fun item(
        id: Long,
        name: String,
        quantity: Double,
        unit: MeasureUnit,
        location: StorageLocation = StorageLocation.PANTRY,
        ingredientId: String? = null,
    ) = InventoryItem(id, name, quantity, unit, location, ingredientId = ingredientId)

    @Test
    fun `a 1 kg bag adds the whole kilo to what was there`() {
        val changes = restock(
            listOf(item(1, "Arroz", 200.0, MeasureUnit.GRAM, ingredientId = "arroz")),
            listOf(Purchased(rice, "Arroz", 1000.0, MeasureUnit.GRAM)),
        )

        assertEquals(listOf(item(1, "Arroz", 1200.0, MeasureUnit.GRAM, ingredientId = "arroz")), changes)
    }

    @Test
    fun `converts between compatible units when adding`() {
        val changes = restock(
            listOf(item(1, "Pechuga de pollo", 1.0, MeasureUnit.KILOGRAM, StorageLocation.FRIDGE)),
            listOf(Purchased(chicken, "Pechuga de pollo", 300.0, MeasureUnit.GRAM)),
        )

        assertEquals(1.3, changes.single().quantity, 0.0)
        assertEquals(MeasureUnit.KILOGRAM, changes.single().unit)
    }

    @Test
    fun `a record created before the catalog gets linked when adding`() {
        val changes = restock(
            listOf(item(1, "Pechuga de pollo", 1.0, MeasureUnit.KILOGRAM, StorageLocation.FRIDGE)),
            listOf(Purchased(chicken, "Pechuga de pollo", 300.0, MeasureUnit.GRAM)),
        )

        assertEquals("pechuga_pollo", changes.single().ingredientId)
    }

    @Test
    fun `without a previous record it creates one in the usual location`() {
        val changes = restock(emptyList(), listOf(Purchased(chicken, "Pechuga de pollo", 500.0, MeasureUnit.GRAM)))

        val created = changes.single()
        assertEquals(0L, created.id)
        assertEquals("Pechuga de pollo", created.name)
        assertEquals(StorageLocation.FRIDGE, created.location)
        assertEquals("pechuga_pollo", created.ingredientId)
    }

    @Test
    fun `reuses a depleted record`() {
        val changes = restock(
            listOf(item(4, "Arroz", 0.0, MeasureUnit.GRAM, ingredientId = "arroz")),
            listOf(Purchased(rice, "Arroz", 1000.0, MeasureUnit.GRAM)),
        )

        assertEquals(4L, changes.single().id)
        assertEquals(1000.0, changes.single().quantity, 0.0)
    }

    @Test
    fun `prefers the record in the usual location`() {
        val changes = restock(
            listOf(
                item(1, "Pechuga de pollo", 200.0, MeasureUnit.GRAM, StorageLocation.FREEZER, "pechuga_pollo"),
                item(2, "Pechuga de pollo", 100.0, MeasureUnit.GRAM, StorageLocation.FRIDGE, "pechuga_pollo"),
            ),
            listOf(Purchased(chicken, "Pechuga de pollo", 300.0, MeasureUnit.GRAM)),
        )

        assertEquals(2L, changes.single().id)
        assertEquals(400.0, changes.single().quantity, 0.0)
    }

    @Test
    fun `with incompatible units it creates a separate record instead of guessing`() {
        val changes = restock(
            listOf(item(1, "Pechuga de pollo", 2.0, MeasureUnit.PIECE, StorageLocation.FRIDGE, "pechuga_pollo")),
            listOf(Purchased(chicken, "Pechuga de pollo", 500.0, MeasureUnit.GRAM)),
        )

        assertEquals(0L, changes.single().id)
        assertEquals(500.0, changes.single().quantity, 0.0)
    }

    @Test
    fun `a product outside the catalog is added to the record with the same name`() {
        val changes = restock(
            listOf(item(9, "Salsa Valentina", 1.0, MeasureUnit.PIECE)),
            listOf(Purchased(null, "salsa valentina", 2.0, MeasureUnit.PIECE)),
        )

        assertEquals(9L, changes.single().id)
        assertEquals(3.0, changes.single().quantity, 0.0)
    }

    @Test
    fun `two purchases of the same food end up in a single record`() {
        val changes = restock(
            emptyList(),
            listOf(
                Purchased(rice, "Arroz", 1000.0, MeasureUnit.GRAM),
                Purchased(rice, "Arroz", 500.0, MeasureUnit.GRAM),
            ),
        )

        assertEquals(1500.0, changes.single().quantity, 0.0)
    }

    @Test
    fun `a zero quantity is not added`() {
        assertTrue(restock(emptyList(), listOf(Purchased(rice, "Arroz", 0.0, MeasureUnit.GRAM))).isEmpty())
    }
}
