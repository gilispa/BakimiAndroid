package com.bakimi.domain.ingredientes

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CoincidenciaTest {

    private val jitomate = Ingrediente("jitomate", "Jitomate", sinonimos = listOf("Tomate", "Tomate rojo"))
    private val pechuga = Ingrediente("pechuga_pollo", "Pechuga de pollo", sinonimos = listOf("Pollo"))

    @Test
    fun `normaliza mayusculas, acentos y espacios`() {
        assertEquals("platano macho", normalizarNombre("  Plátano   MACHO "))
    }

    @Test
    fun `normaliza plurales comunes en espanol`() {
        assertEquals("huevo", normalizarNombre("Huevos"))
        assertEquals("limon", normalizarNombre("Limones"))
        assertEquals("jitomate", normalizarNombre("Jitomates"))
        assertEquals("pechuga de pollo", normalizarNombre("Pechugas de pollo"))
    }

    @Test
    fun `no recorta palabras que no son plurales`() {
        assertEquals("arroz", normalizarNombre("Arroz"))
        assertEquals("pan", normalizarNombre("Pan"))
    }

    @Test
    fun `coincide con el nombre del ingrediente en plural`() {
        assertTrue(jitomate.coincideCon("jitomates"))
    }

    @Test
    fun `coincide con un sinonimo`() {
        assertTrue(jitomate.coincideCon("Tomates"))
        assertTrue(pechuga.coincideCon("pollo"))
    }

    @Test
    fun `no confunde productos que solo comparten una palabra`() {
        assertFalse(jitomate.coincideCon("Tomate verde"))
        assertFalse(pechuga.coincideCon("Caldo de pollo"))
    }
}
