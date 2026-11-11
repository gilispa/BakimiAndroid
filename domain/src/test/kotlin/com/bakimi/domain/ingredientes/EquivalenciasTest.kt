package com.bakimi.domain.ingredientes

import com.bakimi.domain.inventario.Unidad
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EquivalenciasTest {

    private val tortilla = Ingrediente("tortilla_maiz", "Tortilla de maíz", gramosPorPieza = 30.0)
    private val ajo = Ingrediente("ajo", "Ajo")

    @Test
    fun `entre unidades compatibles la conversion es exacta`() {
        val conversion = tortilla.convertir(1.0, Unidad.KILOGRAMO, Unidad.GRAMO)!!

        assertEquals(1000.0, conversion.cantidad, 0.0)
        assertFalse(conversion.aproximada)
    }

    @Test
    fun `un kilo de tortillas son unas 33 piezas`() {
        val conversion = tortilla.convertir(1.0, Unidad.KILOGRAMO, Unidad.PIEZA)!!

        assertEquals(33.3, conversion.cantidad, 0.0)
        assertTrue(conversion.aproximada)
    }

    @Test
    fun `de piezas a gramos usa el mismo peso por pieza`() {
        val conversion = tortilla.convertir(6.0, Unidad.PIEZA, Unidad.GRAMO)!!

        assertEquals(180.0, conversion.cantidad, 0.0)
        assertTrue(conversion.aproximada)
    }

    @Test
    fun `sin peso por pieza no convierte piezas a peso`() {
        assertNull(ajo.convertir(1.0, Unidad.PIEZA, Unidad.GRAMO))
    }

    @Test
    fun `el peso por pieza no sirve para convertir volumen`() {
        assertNull(tortilla.convertir(1.0, Unidad.LITRO, Unidad.PIEZA))
    }
}
