package com.bakimi.domain.shopping

import com.bakimi.domain.ingredients.Ingredient
import com.bakimi.domain.inventory.MeasureUnit
import com.bakimi.domain.recipes.inStock
import com.bakimi.domain.recipes.needs
import com.bakimi.domain.recipes.recipe
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ComplementaryRecipesTest {

    private val lettuce = Ingredient("lechuga", "Lechuga")
    private val chicken = Ingredient("pechuga_pollo", "Pechuga de pollo")
    private val bread = Ingredient("pan_caja", "Pan de caja")
    private val salt = Ingredient("sal", "Sal", staple = true)
    private val oats = Ingredient("avena", "Avena")

    private val salad = recipe(
        "ensalada",
        ingredients = arrayOf(
            needs(lettuce, 0.5, MeasureUnit.PIECE),
            needs(chicken, 300.0, MeasureUnit.GRAM),
            needs(salt, 2.0, MeasureUnit.GRAM),
        ),
    )
    private val sandwich = recipe(
        "sandwich",
        minutes = 20,
        ingredients = arrayOf(
            needs(lettuce, 0.25, MeasureUnit.PIECE),
            needs(chicken, 200.0, MeasureUnit.GRAM),
            needs(bread, 4.0, MeasureUnit.PIECE),
        ),
    )
    private val tacos = recipe(
        "tacos",
        minutes = 10,
        ingredients = arrayOf(needs(lettuce, 0.25, MeasureUnit.PIECE), needs(bread, 2.0, MeasureUnit.PIECE)),
    )
    private val onlySalt = recipe("solo_sal", ingredients = arrayOf(needs(salt, 1.0, MeasureUnit.GRAM), needs(oats, 50.0, MeasureUnit.GRAM)))

    private val catalog = listOf(salad, sandwich, tacos, onlySalt)

    private fun recommend(vararg inventory: com.bakimi.domain.inventory.InventoryItem) =
        recommendComplementary(listOf(PlannedRecipe(salad, 2)), inventory.toList(), catalog)

    @Test
    fun `recommends using the rest of the lettuce that has to be bought`() {
        val recommendations = recommend()

        val sandwich = recommendations.single { it.recipe.id == "sandwich" }
        assertEquals(listOf("lechuga"), sandwich.shared.map { it.id })
    }

    @Test
    fun `also counts what is already at home and left over`() {
        val recommendations = recommend(inStock("Pechuga de pollo", 1.0, MeasureUnit.KILOGRAM))

        val sandwich = recommendations.first()
        assertEquals("sandwich", sandwich.recipe.id)
        assertEquals(setOf("lechuga", "pechuga_pollo"), sandwich.shared.map { it.id }.toSet())
    }

    @Test
    fun `sorts by more shared ingredients`() {
        val recommendations = recommend(inStock("Pechuga de pollo", 1.0, MeasureUnit.KILOGRAM))

        assertEquals(listOf("sandwich", "tacos"), recommendations.map { it.recipe.id })
    }

    @Test
    fun `does not recommend for sharing only staples like salt`() {
        assertTrue(recommend(inStock("Sal", 1.0, MeasureUnit.KILOGRAM)).none { it.recipe.id == "solo_sal" })
    }

    @Test
    fun `if the plan uses up the whole ingredient it does not count as shared`() {
        // The salad uses exactly the half lettuce available; nothing is left over.
        val recommendations = recommend(inStock("Lechuga", 0.5, MeasureUnit.PIECE))

        assertTrue(recommendations.none { r -> r.shared.any { it.id == "lechuga" } })
    }

    @Test
    fun `does not recommend recipes already in the plan`() {
        assertTrue(recommend().none { it.recipe.id == "ensalada" })
    }

    @Test
    fun `without a plan there are no recommendations`() {
        assertTrue(recommendComplementary(emptyList(), emptyList(), catalog).isEmpty())
    }
}
