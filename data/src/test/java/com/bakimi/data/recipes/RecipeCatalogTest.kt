package com.bakimi.data.recipes

import com.bakimi.domain.ingredients.matches
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class RecipeCatalogTest {

    private val catalog = LocalRecipeCatalog()
    private val recipes = catalog.all()

    @Test
    fun `the catalog has recipes`() {
        assertTrue(recipes.size >= 10)
    }

    @Test
    fun `recipe ids are unique and valid as resource names`() {
        assertEquals(recipes.size, recipes.map { it.id }.toSet().size)
        recipes.forEach { assertTrue(it.id, it.id.matches(Regex("[a-z0-9_]+"))) }
    }

    @Test
    fun `every recipe has complete data and positive quantities`() {
        recipes.forEach { recipe ->
            assertTrue(recipe.id, recipe.minutes > 0)
            assertTrue(recipe.id, recipe.baseServings > 0)
            assertTrue(recipe.id, recipe.steps.isNotEmpty())
            assertTrue(recipe.id, recipe.ingredients.isNotEmpty())
            assertTrue(recipe.id, recipe.photoCredit.author.isNotBlank())
            recipe.ingredients.forEach { assertTrue("${recipe.id}/${it.ingredient.id}", it.quantity > 0) }
        }
    }

    @Test
    fun `a recipe does not repeat ingredients`() {
        recipes.forEach { recipe ->
            val ids = recipe.ingredients.map { it.ingredient.id }
            assertEquals(recipe.id, ids.size, ids.toSet().size)
        }
    }

    @Test
    fun `no name or synonym matches two different ingredients`() {
        val ingredients = recipes.flatMap { r -> r.ingredients.map { it.ingredient } }.distinctBy { it.id }
        ingredients.forEach { ingredient ->
            (listOf(ingredient.name) + ingredient.synonyms).forEach { name ->
                val matchingIds = ingredients.filter { it.matches(name) }.map { it.id }
                assertEquals(name, listOf(ingredient.id), matchingIds)
            }
        }
    }

    @Test
    fun `step images exist in the app assets`() {
        // :data tests run from the module folder.
        val assets = File("../app/src/main/assets")
        recipes.flatMap { it.steps }.mapNotNull { it.image }.forEach { path ->
            assertTrue("falta $path", File(assets, path).isFile)
        }
    }

    @Test
    fun `huevos a la mexicana has one image per step`() {
        val recipe = catalog.get("huevos_mexicana")!!
        assertTrue(recipe.steps.all { it.image != null })
    }

    @Test
    fun `get finds by id`() {
        assertNotNull(catalog.get(recipes.first().id))
    }
}
