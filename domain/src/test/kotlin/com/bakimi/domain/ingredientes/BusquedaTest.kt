package com.bakimi.domain.ingredientes

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BusquedaTest {

    private val catalogo = listOf(
        Ingrediente("jitomate", "Jitomate", listOf("Tomate", "Tomate rojo")),
        Ingrediente("tomate_verde", "Tomate verde", listOf("Tomatillo")),
        Ingrediente("pechuga_pollo", "Pechuga de pollo", listOf("Pechuga", "Pollo")),
        Ingrediente("caldo_pollo", "Caldo de pollo"),
        Ingrediente("platano", "Plátano", listOf("Banana")),
        Ingrediente("papa", "Papa", listOf("Patata")),
        Ingrediente("papaya", "Papaya"),
        Ingrediente("huevo", "Huevo"),
        Ingrediente("limon", "Limón"),
    )

    private fun buscar(texto: String, limite: Int = 8) = buscarIngredientes(texto, catalogo, limite).map { it.id }

    @Test
    fun `texto vacio no devuelve resultados`() {
        assertEquals(emptyList<String>(), buscar("   "))
    }

    @Test
    fun `encuentra por el inicio del nombre`() {
        assertEquals("jitomate", buscar("jito").first())
    }

    @Test
    fun `ignora mayusculas y acentos`() {
        assertEquals("platano", buscar("PLATANO").first())
        assertEquals("limon", buscar("limón").first())
    }

    @Test
    fun `encuentra el singular cuando se escribe en plural`() {
        assertEquals("huevo", buscar("huevos").first())
        assertEquals("limon", buscar("limones").first())
    }

    @Test
    fun `encuentra por sinonimo`() {
        assertEquals("platano", buscar("banana").first())
    }

    @Test
    fun `la coincidencia exacta va antes que la que solo empieza igual`() {
        assertEquals(listOf("jitomate", "tomate_verde"), buscar("tomate").take(2))
        assertEquals(listOf("papa", "papaya"), buscar("papa"))
    }

    @Test
    fun `el inicio de una palabra va antes que la palabra en medio del nombre`() {
        assertEquals(listOf("pechuga_pollo", "caldo_pollo"), buscar("pollo"))
    }

    @Test
    fun `tolera errores de escritura`() {
        assertEquals("jitomate", buscar("jitomte").first())
        assertEquals("pechuga_pollo", buscar("pechga").first())
        assertEquals("platano", buscar("platnao").first())
    }

    @Test
    fun `no inventa coincidencias con textos cortos distintos`() {
        assertTrue(buscar("xyz").isEmpty())
        assertTrue(buscar("arroz").isEmpty())
    }

    @Test
    fun `cada ingrediente aparece una sola vez`() {
        val ids = buscar("tomate")
        assertEquals(ids.toSet().size, ids.size)
    }

    @Test
    fun `respeta el limite`() {
        assertEquals(1, buscar("p", limite = 1).size)
    }

    @Test
    fun `buscar por nombre solo acepta coincidencias exactas`() {
        assertEquals("jitomate", catalogo.buscarPorNombre("Tomates")?.id)
        assertNull(catalogo.buscarPorNombre("jito"))
    }
}
