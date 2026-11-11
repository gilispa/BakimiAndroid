package com.bakimi.data.ingredients

import com.bakimi.data.recipes.LocalRecipeCatalog
import com.bakimi.domain.ingredients.matches
import com.bakimi.domain.ingredients.convert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IngredientCatalogTest {

    private val catalog = LocalIngredientCatalog()
    private val ingredients = catalog.all()

    @Test
    fun `covers everyday ingredients`() {
        assertTrue("hay ${ingredients.size}", ingredients.size >= 100)
    }

    @Test
    fun `ids are unique and stable`() {
        assertEquals(ingredients.size, ingredients.map { it.id }.toSet().size)
        ingredients.forEach { assertTrue(it.id, it.id.matches(Regex("[a-z0-9_]+"))) }
    }

    @Test
    fun `no name or synonym corresponds to two ingredients`() {
        ingredients.forEach { ingredient ->
            (listOf(ingredient.name) + ingredient.synonyms).forEach { name ->
                val matchingIds = ingredients.filter { it.matches(name) }.map { it.id }
                assertEquals(name, listOf(ingredient.id), matchingIds)
            }
        }
    }

    @Test
    fun `every recipe ingredient is in the catalog`() {
        LocalRecipeCatalog().all().flatMap { it.ingredients }.forEach { used ->
            assertEquals(used.ingredient.id, used.ingredient, catalog.get(used.ingredient.id))
        }
    }

    @Test
    fun `foods bought by weight or by the piece have an equivalence`() {
        listOf("tortilla_maiz", "pechuga_pollo", "jitomate", "huevo", "cebolla", "pan_caja").forEach { id ->
            assertTrue(id, (catalog.get(id)?.gramsPerPiece ?: 0.0) > 0.0)
        }
    }

    @Test
    fun `garlic has no equivalence because a piece can be a head or a clove`() {
        assertEquals(null, catalog.get("ajo")?.gramsPerPiece)
    }

    @Test
    fun `equivalences are positive and all belong to a catalog ingredient`() {
        val ids = ingredients.map { it.id }.toSet()
        GRAMS_PER_PIECE.keys.forEach { id -> assertTrue("$id no está en el catálogo", id in ids) }
        ingredients.forEach { ingredient ->
            ingredient.gramsPerPiece?.let { assertTrue(ingredient.id, it > 0.0) }
        }
    }

    @Test
    fun `every recipe ingredient has a reference price and a valid package`() {
        LocalRecipeCatalog().all().flatMap { it.ingredients }.map { it.ingredient }.distinct().forEach { ing ->
            val purchase = ing.purchase
            assertTrue("${ing.id} sin datos de compra", purchase != null)
            assertTrue("${ing.id} sin precio", (purchase!!.price ?: 0.0) > 0.0)
            assertTrue("${ing.id} presentación", purchase.packageSize > 0.0)
        }
    }

    @Test
    fun `the purchase unit can be converted from the recipe unit`() {
        LocalRecipeCatalog().all().flatMap { it.ingredients }.forEach { used ->
            val purchase = used.ingredient.purchase ?: return@forEach
            assertTrue(
                "${used.ingredient.id}: ${used.unit} → ${purchase.unit}",
                used.ingredient.convert(1.0, used.unit, purchase.unit) != null,
            )
        }
    }

    @Test
    fun `purchase data and staples belong to catalog ingredients`() {
        val ids = ingredients.map { it.id }.toSet()
        (PURCHASE_INFO.keys + STAPLES).forEach { id -> assertTrue("$id no está en el catálogo", id in ids) }
    }

    @Test
    fun `the suggested unit is compatible with the one the recipes use`() {
        LocalRecipeCatalog().all().flatMap { it.ingredients }.forEach { used ->
            assertTrue(
                "${used.ingredient.id}: ${used.ingredient.suggestedUnit} vs ${used.unit}",
                used.ingredient.suggestedUnit.isCompatibleWith(used.unit),
            )
        }
    }
}
