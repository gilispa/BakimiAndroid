package com.bakimi.domain.shopping

import com.bakimi.domain.ingredients.Ingredient
import com.bakimi.domain.ingredients.PurchaseInfo
import com.bakimi.domain.inventory.MeasureUnit
import com.bakimi.domain.recipes.inStock
import com.bakimi.domain.recipes.needs
import com.bakimi.domain.recipes.recipe
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PurchaseTest {

    private val chicken = Ingredient(
        "pechuga_pollo",
        "Pechuga de pollo",
        purchase = PurchaseInfo(MeasureUnit.GRAM, packageSize = 100.0, price = 17.0),
    )
    private val rice = Ingredient(
        "arroz",
        "Arroz",
        purchase = PurchaseInfo(MeasureUnit.GRAM, packageSize = 1000.0, packageName = "bolsa de 1 kg", price = 34.0),
    )
    private val onion = Ingredient("cebolla", "Cebolla")
    private val tortilla = Ingredient(
        "tortilla_maiz",
        "Tortilla de maíz",
        gramsPerPiece = 30.0,
        purchase = PurchaseInfo(MeasureUnit.GRAM, packageSize = 500.0, price = 14.0),
    )

    private val chickenWithRice = recipe(
        "pollo_arroz",
        baseServings = 2,
        ingredients = arrayOf(needs(chicken, 600.0, MeasureUnit.GRAM), needs(rice, 200.0, MeasureUnit.GRAM)),
    )
    private val tacos = recipe(
        "tacos",
        baseServings = 2,
        ingredients = arrayOf(needs(chicken, 600.0, MeasureUnit.GRAM), needs(onion, 0.75, MeasureUnit.PIECE)),
    )

    private fun requirement(plan: List<PlannedRecipe>, ingredient: Ingredient, vararg inventory: com.bakimi.domain.inventory.InventoryItem) =
        consolidatePlan(plan, inventory.toList()).single { it.ingredient == ingredient }

    @Test
    fun `two recipes with 1,2 kg of chicken and 1 kg at home produce a 200 g shortage`() {
        val plan = listOf(PlannedRecipe(chickenWithRice, 2), PlannedRecipe(tacos, 2))

        val chicken = requirement(plan, chicken, inStock("Pechuga de pollo", 1.0, MeasureUnit.KILOGRAM))

        assertEquals(1200.0, chicken.required, 0.0)
        assertEquals(200.0, chicken.shortage, 0.0)
    }

    @Test
    fun `a shared ingredient appears once with the recipes that use it`() {
        val plan = listOf(PlannedRecipe(chickenWithRice, 2), PlannedRecipe(tacos, 2))

        val requirements = consolidatePlan(plan, emptyList())

        assertEquals(1, requirements.count { it.ingredient == chicken })
        assertEquals(listOf("pollo_arroz", "tacos"), requirements.single { it.ingredient == chicken }.recipes)
    }

    @Test
    fun `changing servings recalculates what is needed`() {
        val plan = listOf(PlannedRecipe(chickenWithRice, 1), PlannedRecipe(tacos, 4))

        assertEquals(1500.0, requirement(plan, chicken).required, 0.0)
    }

    @Test
    fun `if 200 g of rice are missing it buys the whole 1 kg bag at full price`() {
        val plan = listOf(PlannedRecipe(chickenWithRice, 2))

        val purchase = calculatePurchase(requirement(plan, rice))!!

        assertEquals(1, purchase.packages)
        assertEquals(1000.0, purchase.quantity, 0.0)
        assertEquals("bolsa de 1 kg", purchase.packageName)
        assertEquals(34.0, purchase.price!!, 0.0)
    }

    @Test
    fun `if one bag is not enough it buys as many as needed`() {
        val plan = listOf(PlannedRecipe(chickenWithRice, 12))

        val purchase = calculatePurchase(requirement(plan, rice))!!

        assertEquals(2, purchase.packages)
        assertEquals(68.0, purchase.price!!, 0.0)
    }

    @Test
    fun `loose items are rounded up to the purchase step`() {
        val plan = listOf(PlannedRecipe(chickenWithRice, 2))

        val purchase = calculatePurchase(requirement(plan, chicken, inStock("Pechuga de pollo", 350.0, MeasureUnit.GRAM)))!!

        assertEquals(300.0, purchase.quantity, 0.0)
        assertEquals(51.0, purchase.price!!, 0.0)
    }

    @Test
    fun `pieces are bought whole and without a known price the price is empty`() {
        val plan = listOf(PlannedRecipe(tacos, 2))

        val purchase = calculatePurchase(requirement(plan, onion))!!

        assertEquals(MeasureUnit.PIECE, purchase.unit)
        assertEquals(1.0, purchase.quantity, 0.0)
        assertNull(purchase.price)
    }

    @Test
    fun `pieces are converted to the purchase unit approximately`() {
        val quesadillas = recipe("quesadillas", ingredients = arrayOf(needs(tortilla, 6.0, MeasureUnit.PIECE)))

        val purchase = calculatePurchase(requirement(listOf(PlannedRecipe(quesadillas, 2)), tortilla))!!

        assertEquals(MeasureUnit.GRAM, purchase.unit)
        assertEquals(500.0, purchase.quantity, 0.0)
        assertTrue(purchase.approximate)
    }

    @Test
    fun `what is already enough does not go into the list`() {
        val plan = listOf(PlannedRecipe(chickenWithRice, 2))

        assertNull(calculatePurchase(requirement(plan, rice, inStock("Arroz", 1.0, MeasureUnit.KILOGRAM))))
    }

    @Test
    fun `an unknown price does not count as zero and marks the total as partial`() {
        val total = totalOf(listOf(34.0, null, 51.0))

        assertEquals(85.0, total.total, 0.0)
        assertTrue(total.partial)
        assertFalse(totalOf(listOf(34.0, 51.0)).partial)
    }

    @Test
    fun `the budget is only considered met if all prices are known`() {
        assertEquals(BudgetStatus.WITHIN, budgetStatus(PurchaseTotal(320.0, partial = false), 500.0))
        assertEquals(BudgetStatus.UNCERTAIN, budgetStatus(PurchaseTotal(320.0, partial = true), 500.0))
        assertEquals(BudgetStatus.EXCEEDED, budgetStatus(PurchaseTotal(520.0, partial = true), 500.0))
        assertEquals(BudgetStatus.NO_BUDGET, budgetStatus(PurchaseTotal(520.0, partial = false), null))
    }
}
