package com.bakimi.domain.ingredients

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MatchingTest {

    private val tomato = Ingredient("jitomate", "Jitomate", synonyms = listOf("Tomate", "Tomate rojo"))
    private val breast = Ingredient("pechuga_pollo", "Pechuga de pollo", synonyms = listOf("Pollo"))

    @Test
    fun `normalizes case, accents and whitespace`() {
        assertEquals("platano macho", normalizeName("  Plátano   MACHO "))
    }

    @Test
    fun `normalizes common Spanish plurals`() {
        assertEquals("huevo", normalizeName("Huevos"))
        assertEquals("limon", normalizeName("Limones"))
        assertEquals("jitomate", normalizeName("Jitomates"))
        assertEquals("pechuga de pollo", normalizeName("Pechugas de pollo"))
    }

    @Test
    fun `does not trim words that are not plurals`() {
        assertEquals("arroz", normalizeName("Arroz"))
        assertEquals("pan", normalizeName("Pan"))
    }

    @Test
    fun `matches the ingredient name in plural`() {
        assertTrue(tomato.matches("jitomates"))
    }

    @Test
    fun `matches a synonym`() {
        assertTrue(tomato.matches("Tomates"))
        assertTrue(breast.matches("pollo"))
    }

    @Test
    fun `does not confuse products that only share a word`() {
        assertFalse(tomato.matches("Tomate verde"))
        assertFalse(breast.matches("Caldo de pollo"))
    }
}
