package com.bakimi.domain.cooking

import com.bakimi.domain.ingredients.Ingredient
import com.bakimi.domain.inventory.InventoryItem
import com.bakimi.domain.inventory.StorageLocation
import com.bakimi.domain.inventory.MeasureUnit
import com.bakimi.domain.recipes.needs
import com.bakimi.domain.recipes.recipe
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ConsumptionTest {

    private val egg = Ingredient("huevo", "Huevo")
    private val chicken = Ingredient("pechuga_pollo", "Pechuga de pollo")
    private val tortilla = Ingredient("tortilla_maiz", "Tortilla de maíz", gramsPerPiece = 30.0)
    private val garlic = Ingredient("ajo", "Ajo")

    private fun item(
        id: Long,
        ingredient: Ingredient,
        quantity: Double,
        unit: MeasureUnit,
        expiry: LocalDate? = null,
    ) = InventoryItem(id, ingredient.name, quantity, unit, StorageLocation.FRIDGE, expiry, ingredient.id)

    private fun uses(ingredient: Ingredient, quantity: Double, unit: MeasureUnit) =
        UsedIngredient(ingredient, quantity, unit)

    @Test
    fun `deducts what was used from the record`() {
        val consumption = calculateConsumption(listOf(uses(egg, 4.0, MeasureUnit.PIECE)), listOf(item(1, egg, 12.0, MeasureUnit.PIECE)))

        assertEquals(8.0, consumption.changes.single().quantity, 0.0)
        assertTrue(consumption.discrepancies.isEmpty())
    }

    @Test
    fun `cooking fewer servings deducts the corresponding quantities`() {
        val omelette = recipe("omelette", baseServings = 2, ingredients = arrayOf(needs(egg, 4.0, MeasureUnit.PIECE)))

        assertEquals(listOf(uses(egg, 2.0, MeasureUnit.PIECE)), defaultUsage(omelette, servings = 1))
    }

    @Test
    fun `converts between compatible units`() {
        val consumption = calculateConsumption(listOf(uses(chicken, 300.0, MeasureUnit.GRAM)), listOf(item(1, chicken, 1.0, MeasureUnit.KILOGRAM)))

        assertEquals(0.7, consumption.changes.single().quantity, 0.0)
        assertFalse(consumption.approximate)
    }

    @Test
    fun `uses what expires soonest first`() {
        val consumption = calculateConsumption(
            listOf(uses(chicken, 200.0, MeasureUnit.GRAM)),
            listOf(
                item(1, chicken, 500.0, MeasureUnit.GRAM, LocalDate.of(2026, 11, 20)),
                item(2, chicken, 500.0, MeasureUnit.GRAM, LocalDate.of(2026, 11, 15)),
            ),
        )

        assertEquals(2L, consumption.changes.single().id)
        assertEquals(300.0, consumption.changes.single().quantity, 0.0)
    }

    @Test
    fun `splits across several records when one is not enough`() {
        val consumption = calculateConsumption(
            listOf(uses(chicken, 500.0, MeasureUnit.GRAM)),
            listOf(item(1, chicken, 300.0, MeasureUnit.GRAM), item(2, chicken, 400.0, MeasureUnit.GRAM)),
        )

        assertEquals(listOf(0.0, 200.0), consumption.changes.sortedBy { it.id }.map { it.quantity })
    }

    @Test
    fun `using more than recorded leaves the record at zero and reports the discrepancy`() {
        val consumption = calculateConsumption(listOf(uses(chicken, 300.0, MeasureUnit.GRAM)), listOf(item(1, chicken, 200.0, MeasureUnit.GRAM)))

        assertEquals(0.0, consumption.changes.single().quantity, 0.0)
        assertEquals(listOf(Discrepancy(chicken, 100.0, MeasureUnit.GRAM)), consumption.discrepancies)
    }

    @Test
    fun `what was not recorded is reported without touching the inventory`() {
        val consumption = calculateConsumption(listOf(uses(egg, 2.0, MeasureUnit.PIECE)), emptyList())

        assertTrue(consumption.changes.isEmpty())
        assertEquals(listOf(Discrepancy(egg, 2.0, MeasureUnit.PIECE)), consumption.discrepancies)
    }

    @Test
    fun `uses the weight per piece and marks the deduction as approximate`() {
        val consumption = calculateConsumption(
            listOf(uses(tortilla, 6.0, MeasureUnit.PIECE)),
            listOf(item(1, tortilla, 1.0, MeasureUnit.KILOGRAM)),
        )

        assertEquals(0.82, consumption.changes.single().quantity, 0.0)
        assertTrue(consumption.approximate)
    }

    @Test
    fun `without an equivalence it does not deduct from a record in another unit`() {
        val consumption = calculateConsumption(listOf(uses(garlic, 2.0, MeasureUnit.PIECE)), listOf(item(1, garlic, 100.0, MeasureUnit.GRAM)))

        assertTrue(consumption.changes.isEmpty())
        assertEquals(listOf(Discrepancy(garlic, 2.0, MeasureUnit.PIECE)), consumption.discrepancies)
    }

    @Test
    fun `a zero used quantity deducts nothing`() {
        val consumption = calculateConsumption(listOf(uses(egg, 0.0, MeasureUnit.PIECE)), listOf(item(1, egg, 12.0, MeasureUnit.PIECE)))

        assertTrue(consumption.changes.isEmpty())
        assertTrue(consumption.discrepancies.isEmpty())
    }
}
