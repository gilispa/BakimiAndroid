package com.bakimi.domain.inventario

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UnidadTest {

    @Test
    fun `kilogramos a gramos multiplica por mil`() {
        assertEquals(1500.0, convertir(1.5, Unidad.KILOGRAMO, Unidad.GRAMO)!!, 0.0)
    }

    @Test
    fun `mililitros a litros divide entre mil`() {
        assertEquals(0.25, convertir(250.0, Unidad.MILILITRO, Unidad.LITRO)!!, 0.0)
    }

    @Test
    fun `convertir a la misma unidad conserva la cantidad`() {
        assertEquals(3.0, convertir(3.0, Unidad.PIEZA, Unidad.PIEZA)!!, 0.0)
    }

    @Test
    fun `piezas a gramos no se convierte sin equivalencia`() {
        assertNull(convertir(2.0, Unidad.PIEZA, Unidad.GRAMO))
    }

    @Test
    fun `peso a volumen no se convierte`() {
        assertNull(convertir(1.0, Unidad.KILOGRAMO, Unidad.LITRO))
    }

    @Test
    fun `la conversion no arrastra errores de punto flotante`() {
        assertEquals(0.3, convertir(300.0, Unidad.GRAMO, Unidad.KILOGRAMO)!!, 0.0)
        assertEquals(300.0, convertir(0.3, Unidad.KILOGRAMO, Unidad.GRAMO)!!, 0.0)
    }

    @Test
    fun `compatibilidad depende de la magnitud`() {
        assertTrue(Unidad.GRAMO.esCompatibleCon(Unidad.KILOGRAMO))
        assertFalse(Unidad.PIEZA.esCompatibleCon(Unidad.GRAMO))
    }
}
