package com.bakimi.domain.inventario

import org.junit.Assert.assertEquals
import org.junit.Test

class FaltanteTest {

    @Test
    fun `cuando la existencia no alcanza devuelve la diferencia`() {
        assertEquals(50.0, calcularFaltante(requerido = 200.0, existencia = 150.0), 0.0)
    }

    @Test
    fun `cuando la existencia sobra devuelve cero`() {
        assertEquals(0.0, calcularFaltante(requerido = 100.0, existencia = 300.0), 0.0)
    }
}
