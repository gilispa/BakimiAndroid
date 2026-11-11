package com.bakimi.domain.ingredients

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchTest {

    private val catalog = listOf(
        Ingredient("jitomate", "Jitomate", listOf("Tomate", "Tomate rojo")),
        Ingredient("tomate_verde", "Tomate verde", listOf("Tomatillo")),
        Ingredient("pechuga_pollo", "Pechuga de pollo", listOf("Pechuga", "Pollo")),
        Ingredient("caldo_pollo", "Caldo de pollo"),
        Ingredient("platano", "Plátano", listOf("Banana")),
        Ingredient("papa", "Papa", listOf("Patata")),
        Ingredient("papaya", "Papaya"),
        Ingredient("huevo", "Huevo"),
        Ingredient("limon", "Limón"),
    )

    private fun search(text: String, limit: Int = 8) = searchIngredients(text, catalog, limit).map { it.id }

    @Test
    fun `empty text returns no results`() {
        assertEquals(emptyList<String>(), search("   "))
    }

    @Test
    fun `finds by name prefix`() {
        assertEquals("jitomate", search("jito").first())
    }

    @Test
    fun `ignores case and accents`() {
        assertEquals("platano", search("PLATANO").first())
        assertEquals("limon", search("limón").first())
    }

    @Test
    fun `finds the singular when typed in plural`() {
        assertEquals("huevo", search("huevos").first())
        assertEquals("limon", search("limones").first())
    }

    @Test
    fun `finds by synonym`() {
        assertEquals("platano", search("banana").first())
    }

    @Test
    fun `an exact match comes before a prefix match`() {
        assertEquals(listOf("jitomate", "tomate_verde"), search("tomate").take(2))
        assertEquals(listOf("papa", "papaya"), search("papa"))
    }

    @Test
    fun `a word prefix comes before a word in the middle of the name`() {
        assertEquals(listOf("pechuga_pollo", "caldo_pollo"), search("pollo"))
    }

    @Test
    fun `tolerates typos`() {
        assertEquals("jitomate", search("jitomte").first())
        assertEquals("pechuga_pollo", search("pechga").first())
        assertEquals("platano", search("platnao").first())
    }

    @Test
    fun `does not invent matches for different short texts`() {
        assertTrue(search("xyz").isEmpty())
        assertTrue(search("arroz").isEmpty())
    }

    @Test
    fun `each ingredient appears only once`() {
        val ids = search("tomate")
        assertEquals(ids.toSet().size, ids.size)
    }

    @Test
    fun `respects the limit`() {
        assertEquals(1, search("p", limit = 1).size)
    }

    @Test
    fun `search by name only accepts exact matches`() {
        assertEquals("jitomate", catalog.buscarPorNombre("Tomates")?.id)
        assertNull(catalog.buscarPorNombre("jito"))
    }
}
