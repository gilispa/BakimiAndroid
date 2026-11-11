package com.bakimi.domain.recipes

import com.bakimi.domain.ingredients.Ingredient
import com.bakimi.domain.inventory.MeasureUnit
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeTest {

    /** Evaluation with [missingCount] missing ingredients out of [total]. */
    private fun evaluation(id: String, missingCount: Int, total: Int = 4, minutes: Int = 30): RecipeEvaluation {
        val ingredients = (1..total).map { i ->
            val required = needs(Ingredient("$id$i", "$id$i"), 1.0, MeasureUnit.PIECE)
            val state = if (i <= missingCount) IngredientStatus.MISSING else IngredientStatus.COVERED
            EvaluatedIngredient(required, available = 0.0, shortage = 0.0, state = state)
        }
        return RecipeEvaluation(recipe(id, minutes = minutes), servings = 2, ingredients = ingredients)
    }

    private fun List<RecipeEvaluation>.ids() = map { it.recipe.id }

    @Test
    fun `sorts by fewer missing, then more ingredients used, then less time`() {
        val home = buildHome(
            listOf(
                evaluation("tres_faltan", missingCount = 3),
                evaluation("lista_lenta", missingCount = 0, minutes = 40),
                evaluation("una_falta_de_cuatro", missingCount = 1, total = 4),
                evaluation("lista_rapida", missingCount = 0, minutes = 10),
                evaluation("una_falta_de_dos", missingCount = 1, total = 2),
            ),
        )

        assertEquals(
            listOf("lista_rapida", "lista_lenta", "una_falta_de_cuatro", "una_falta_de_dos", "tres_faltan"),
            home.featured.ids(),
        )
    }

    @Test
    fun `limits the carousel`() {
        val home = buildHome((1..10).map { evaluation("r$it", missingCount = 0) }, maxFeatured = 6)

        assertEquals(6, home.featured.size)
    }

    @Test
    fun `splits sections by missing ingredients and time`() {
        val home = buildHome(
            listOf(
                evaluation("lista", missingCount = 0, minutes = 15),
                evaluation("falta_una", missingCount = 1, minutes = 45),
                evaluation("faltan_dos", missingCount = 2, minutes = 20),
                evaluation("faltan_tres", missingCount = 3, minutes = 10),
            ),
        )

        assertEquals(listOf("lista"), home.readyToCook.ids())
        assertEquals(listOf("falta_una", "faltan_dos"), home.almostReady.ids())
        assertEquals(listOf("lista", "faltan_dos", "faltan_tres"), home.quick.ids())
        assertEquals(listOf("faltan_tres"), home.moreIdeas.ids())
    }
}
