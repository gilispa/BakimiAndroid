package com.bakimi.domain.inventario

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CapturaTest {

    private fun item(nombre: String = "Huevo", cantidad: Double = 12.0) =
        ItemInventario(nombre = nombre, cantidad = cantidad, unidad = Unidad.PIEZA, ubicacion = Ubicacion.REFRIGERADOR)

    @Test
    fun `acepta punto decimal`() {
        assertEquals(1.5, parsearCantidad("1.5")!!, 0.0)
    }

    @Test
    fun `acepta coma decimal`() {
        assertEquals(0.5, parsearCantidad("0,5")!!, 0.0)
    }

    @Test
    fun `ignora espacios alrededor`() {
        assertEquals(12.0, parsearCantidad("  12 ")!!, 0.0)
    }

    @Test
    fun `rechaza texto que no es numero`() {
        assertNull(parsearCantidad("medio kilo"))
        assertNull(parsearCantidad(""))
    }

    @Test
    fun `rechaza cantidades negativas`() {
        assertNull(parsearCantidad("-2"))
    }

    @Test
    fun `rechaza valores no finitos`() {
        assertNull(parsearCantidad("NaN"))
        assertNull(parsearCantidad("Infinity"))
    }

    @Test
    fun `un alimento con nombre y cantidad es valido`() {
        assertTrue(validar(item()).isEmpty())
    }

    @Test
    fun `cantidad cero es valida porque representa un agotado`() {
        assertTrue(validar(item(cantidad = 0.0)).isEmpty())
    }

    @Test
    fun `nombre en blanco es invalido`() {
        assertEquals(setOf(ErrorItem.NOMBRE_VACIO), validar(item(nombre = "   ")))
    }

    @Test
    fun `cantidad negativa es invalida`() {
        assertEquals(setOf(ErrorItem.CANTIDAD_INVALIDA), validar(item(cantidad = -1.0)))
    }

    @Test
    fun `reporta todos los errores juntos`() {
        assertEquals(
            setOf(ErrorItem.NOMBRE_VACIO, ErrorItem.CANTIDAD_INVALIDA),
            validar(item(nombre = "", cantidad = Double.NaN)),
        )
    }
}
