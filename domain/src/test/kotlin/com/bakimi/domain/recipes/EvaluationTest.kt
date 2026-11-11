package com.bakimi.domain.recipes

import com.bakimi.domain.inventory.StorageLocation
import com.bakimi.domain.ingredients.Ingredient
import com.bakimi.domain.inventory.MeasureUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EvaluationTest {

    private val chickenWithRice = recipe(
        "pollo_arroz",
        baseServings = 2,
        ingredients = arrayOf(
            needs(CHICKEN, 600.0, MeasureUnit.GRAM),
            needs(RICE, 200.0, MeasureUnit.GRAM),
        ),
    )

    private fun evaluated(evaluation: RecipeEvaluation, ingredient: Ingredient) =
        evaluation.ingredients.single { it.required.ingredient == ingredient }

    @Test
    fun `a recipe with enough quantities appears ready`() {
        val evaluation = evaluateRecipe(
            chickenWithRice,
            servings = 2,
            inventory = listOf(inStock("Pollo", 1.0, MeasureUnit.KILOGRAM), inStock("Arroz", 500.0, MeasureUnit.GRAM)),
        )

        assertTrue(evaluation.isReady)
        assertEquals(0, evaluation.missing)
    }

    @Test
    fun `needs 600 g of chicken with 400 g at home, 200 g are missing`() {
        val evaluation = evaluateRecipe(
            chickenWithRice,
            servings = 2,
            inventory = listOf(inStock("Pechuga de pollo", 400.0, MeasureUnit.GRAM), inStock("Arroz", 1.0, MeasureUnit.KILOGRAM)),
        )

        val chicken = evaluated(evaluation, CHICKEN)
        assertEquals(IngredientStatus.INSUFFICIENT, chicken.state)
        assertEquals(200.0, chicken.shortage, 0.0)
        assertFalse(evaluation.isReady)
        assertEquals(1, evaluation.missing)
    }

    @Test
    fun `an ingredient without a record is fully missing`() {
        val evaluation = evaluateRecipe(chickenWithRice, servings = 2, inventory = emptyList())

        val rice = evaluated(evaluation, RICE)
        assertEquals(IngredientStatus.MISSING, rice.state)
        assertEquals(200.0, rice.shortage, 0.0)
    }

    @Test
    fun `a depleted ingredient counts as missing`() {
        val evaluation = evaluateRecipe(
            chickenWithRice,
            servings = 2,
            inventory = listOf(inStock("Arroz", 0.0, MeasureUnit.GRAM)),
        )

        assertEquals(IngredientStatus.MISSING, evaluated(evaluation, RICE).state)
    }

    @Test
    fun `adds records of the same ingredient in different locations`() {
        val evaluation = evaluateRecipe(
            chickenWithRice,
            servings = 2,
            inventory = listOf(
                inStock("Pollo", 300.0, MeasureUnit.GRAM, StorageLocation.FRIDGE),
                inStock("Pollo", 0.5, MeasureUnit.KILOGRAM, StorageLocation.FREEZER),
            ),
        )

        val chicken = evaluated(evaluation, CHICKEN)
        assertEquals(IngredientStatus.COVERED, chicken.state)
        assertEquals(800.0, chicken.available, 0.0)
    }

    @Test
    fun `scales quantities to the chosen servings`() {
        val evaluation = evaluateRecipe(
            chickenWithRice,
            servings = 1,
            inventory = listOf(inStock("Pollo", 400.0, MeasureUnit.GRAM)),
        )

        val chicken = evaluated(evaluation, CHICKEN)
        assertEquals(300.0, chicken.required.quantity, 0.0)
        assertEquals(IngredientStatus.COVERED, chicken.state)
    }

    @Test
    fun `does not convert pieces to grams`() {
        val evaluation = evaluateRecipe(
            chickenWithRice,
            servings = 2,
            inventory = listOf(inStock("Pollo", 2.0, MeasureUnit.PIECE)),
        )

        val chicken = evaluated(evaluation, CHICKEN)
        assertEquals(IngredientStatus.OTHER_UNIT, chicken.state)
        assertEquals(600.0, chicken.shortage, 0.0)
    }

    private val tortilla = Ingredient("tortilla_maiz", "Tortilla de maíz", gramsPerPiece = 30.0)
    private val quesadillas = recipe(
        "quesadillas",
        baseServings = 2,
        ingredients = arrayOf(needs(tortilla, 6.0, MeasureUnit.PIECE)),
    )

    @Test
    fun `a kilo of tortillas is enough for 6 tortillas, approximately`() {
        val evaluation = evaluateRecipe(
            quesadillas,
            servings = 2,
            inventory = listOf(inStock("Tortilla de maíz", 1.0, MeasureUnit.KILOGRAM)),
        )

        val tortillas = evaluated(evaluation, tortilla)
        assertEquals(IngredientStatus.COVERED, tortillas.state)
        assertEquals(33.3, tortillas.available, 0.0)
        assertTrue(tortillas.approximate)
        assertTrue(evaluation.isReady)
    }

    @Test
    fun `100 g of tortillas is not enough and the shortage is approximate`() {
        val evaluation = evaluateRecipe(
            quesadillas,
            servings = 2,
            inventory = listOf(inStock("Tortilla de maíz", 100.0, MeasureUnit.GRAM)),
        )

        val tortillas = evaluated(evaluation, tortilla)
        assertEquals(IngredientStatus.INSUFFICIENT, tortillas.state)
        assertEquals(2.7, tortillas.shortage, 0.0)
        assertTrue(tortillas.approximate)
    }

    @Test
    fun `without an approximate conversion the result is not marked approximate`() {
        val evaluation = evaluateRecipe(
            quesadillas,
            servings = 2,
            inventory = listOf(inStock("Tortilla de maíz", 10.0, MeasureUnit.PIECE)),
        )

        assertFalse(evaluated(evaluation, tortilla).approximate)
    }

    @Test
    fun `with enough in a compatible unit it ignores the record in another unit`() {
        val evaluation = evaluateRecipe(
            chickenWithRice,
            servings = 2,
            inventory = listOf(inStock("Pollo", 2.0, MeasureUnit.PIECE), inStock("Pollo", 1.0, MeasureUnit.KILOGRAM)),
        )

        assertEquals(IngredientStatus.COVERED, evaluated(evaluation, CHICKEN).state)
    }

    @Test
    fun `a record linked to the catalog counts even if its name is different`() {
        val evaluation = evaluateRecipe(
            chickenWithRice,
            servings = 2,
            inventory = listOf(inStock("Pollo de rancho", 1.0, MeasureUnit.KILOGRAM, ingredientId = CHICKEN.id)),
        )

        assertEquals(IngredientStatus.COVERED, evaluated(evaluation, CHICKEN).state)
    }

    @Test
    fun `a record linked to another ingredient does not count even if its name matches`() {
        val evaluation = evaluateRecipe(
            chickenWithRice,
            servings = 2,
            inventory = listOf(inStock("Pollo", 1.0, MeasureUnit.KILOGRAM, ingredientId = "caldo_pollo")),
        )

        assertEquals(IngredientStatus.MISSING, evaluated(evaluation, CHICKEN).state)
    }

    @Test
    fun `does not confuse a different product with the ingredient`() {
        val evaluation = evaluateRecipe(
            chickenWithRice,
            servings = 2,
            inventory = listOf(inStock("Caldo de pollo", 1.0, MeasureUnit.LITER)),
        )

        assertEquals(IngredientStatus.MISSING, evaluated(evaluation, CHICKEN).state)
    }
}
