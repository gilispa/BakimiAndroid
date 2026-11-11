package com.bakimi.data.inventario

import com.bakimi.domain.inventario.ItemInventario
import com.bakimi.domain.inventario.Ubicacion
import com.bakimi.domain.inventario.Unidad
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class InventarioMapeoTest {

    @Test
    fun `un alimento con caducidad sobrevive la ida y vuelta`() {
        val item = ItemInventario(
            id = 7,
            nombre = "Pechuga de pollo",
            cantidad = 1.0,
            unidad = Unidad.KILOGRAMO,
            ubicacion = Ubicacion.CONGELADOR,
            caducidad = LocalDate.of(2026, 11, 20),
            ingredienteId = "pechuga_pollo",
        )

        assertEquals(item, item.aEntidad().aDominio())
    }

    @Test
    fun `un alimento fuera del catalogo conserva su vinculo vacio`() {
        val item = ItemInventario(
            nombre = "Salsa Valentina",
            cantidad = 1.0,
            unidad = Unidad.PIEZA,
            ubicacion = Ubicacion.DESPENSA,
        )

        assertEquals(null, item.aEntidad().ingredienteId)
        assertEquals(item, item.aEntidad().aDominio())
    }

    @Test
    fun `un alimento sin caducidad guarda null`() {
        val item = ItemInventario(
            nombre = "Arroz",
            cantidad = 500.0,
            unidad = Unidad.GRAMO,
            ubicacion = Ubicacion.DESPENSA,
        )

        assertNull(item.aEntidad().caducidadDiaEpoch)
    }

    @Test
    fun `los enums se guardan por nombre estable y no por posicion`() {
        val entidad = ItemInventario(
            nombre = "Leche",
            cantidad = 1.0,
            unidad = Unidad.LITRO,
            ubicacion = Ubicacion.REFRIGERADOR,
        ).aEntidad()

        assertEquals("LITRO", entidad.unidad)
        assertEquals("REFRIGERADOR", entidad.ubicacion)
    }
}
