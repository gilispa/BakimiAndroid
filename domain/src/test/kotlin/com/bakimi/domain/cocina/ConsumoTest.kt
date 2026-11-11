package com.bakimi.domain.cocina

import com.bakimi.domain.ingredientes.Ingrediente
import com.bakimi.domain.inventario.ItemInventario
import com.bakimi.domain.inventario.Ubicacion
import com.bakimi.domain.inventario.Unidad
import com.bakimi.domain.recetas.necesita
import com.bakimi.domain.recetas.receta
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ConsumoTest {

    private val huevo = Ingrediente("huevo", "Huevo")
    private val pollo = Ingrediente("pechuga_pollo", "Pechuga de pollo")
    private val tortilla = Ingrediente("tortilla_maiz", "Tortilla de maíz", gramosPorPieza = 30.0)
    private val ajo = Ingrediente("ajo", "Ajo")

    private fun item(
        id: Long,
        ingrediente: Ingrediente,
        cantidad: Double,
        unidad: Unidad,
        caducidad: LocalDate? = null,
    ) = ItemInventario(id, ingrediente.nombre, cantidad, unidad, Ubicacion.REFRIGERADOR, caducidad, ingrediente.id)

    private fun usa(ingrediente: Ingrediente, cantidad: Double, unidad: Unidad) =
        IngredienteUsado(ingrediente, cantidad, unidad)

    @Test
    fun `descuenta lo usado del registro`() {
        val consumo = calcularConsumo(listOf(usa(huevo, 4.0, Unidad.PIEZA)), listOf(item(1, huevo, 12.0, Unidad.PIEZA)))

        assertEquals(8.0, consumo.cambios.single().cantidad, 0.0)
        assertTrue(consumo.diferencias.isEmpty())
    }

    @Test
    fun `cocinar menos porciones descuenta las cantidades correspondientes`() {
        val omelette = receta("omelette", porcionesBase = 2, ingredientes = arrayOf(necesita(huevo, 4.0, Unidad.PIEZA)))

        assertEquals(listOf(usa(huevo, 2.0, Unidad.PIEZA)), usadosPorDefecto(omelette, porciones = 1))
    }

    @Test
    fun `convierte entre unidades compatibles`() {
        val consumo = calcularConsumo(listOf(usa(pollo, 300.0, Unidad.GRAMO)), listOf(item(1, pollo, 1.0, Unidad.KILOGRAMO)))

        assertEquals(0.7, consumo.cambios.single().cantidad, 0.0)
        assertFalse(consumo.aproximado)
    }

    @Test
    fun `usa primero lo que caduca antes`() {
        val consumo = calcularConsumo(
            listOf(usa(pollo, 200.0, Unidad.GRAMO)),
            listOf(
                item(1, pollo, 500.0, Unidad.GRAMO, LocalDate.of(2026, 11, 20)),
                item(2, pollo, 500.0, Unidad.GRAMO, LocalDate.of(2026, 11, 15)),
            ),
        )

        assertEquals(2L, consumo.cambios.single().id)
        assertEquals(300.0, consumo.cambios.single().cantidad, 0.0)
    }

    @Test
    fun `reparte entre varios registros cuando uno no alcanza`() {
        val consumo = calcularConsumo(
            listOf(usa(pollo, 500.0, Unidad.GRAMO)),
            listOf(item(1, pollo, 300.0, Unidad.GRAMO), item(2, pollo, 400.0, Unidad.GRAMO)),
        )

        assertEquals(listOf(0.0, 200.0), consumo.cambios.sortedBy { it.id }.map { it.cantidad })
    }

    @Test
    fun `usar mas de lo registrado deja el registro en cero y reporta la diferencia`() {
        val consumo = calcularConsumo(listOf(usa(pollo, 300.0, Unidad.GRAMO)), listOf(item(1, pollo, 200.0, Unidad.GRAMO)))

        assertEquals(0.0, consumo.cambios.single().cantidad, 0.0)
        assertEquals(listOf(Diferencia(pollo, 100.0, Unidad.GRAMO)), consumo.diferencias)
    }

    @Test
    fun `lo que no estaba registrado se reporta sin tocar el inventario`() {
        val consumo = calcularConsumo(listOf(usa(huevo, 2.0, Unidad.PIEZA)), emptyList())

        assertTrue(consumo.cambios.isEmpty())
        assertEquals(listOf(Diferencia(huevo, 2.0, Unidad.PIEZA)), consumo.diferencias)
    }

    @Test
    fun `usa el peso por pieza y marca el descuento como aproximado`() {
        val consumo = calcularConsumo(
            listOf(usa(tortilla, 6.0, Unidad.PIEZA)),
            listOf(item(1, tortilla, 1.0, Unidad.KILOGRAMO)),
        )

        assertEquals(0.82, consumo.cambios.single().cantidad, 0.0)
        assertTrue(consumo.aproximado)
    }

    @Test
    fun `sin equivalencia no descuenta de un registro en otra unidad`() {
        val consumo = calcularConsumo(listOf(usa(ajo, 2.0, Unidad.PIEZA)), listOf(item(1, ajo, 100.0, Unidad.GRAMO)))

        assertTrue(consumo.cambios.isEmpty())
        assertEquals(listOf(Diferencia(ajo, 2.0, Unidad.PIEZA)), consumo.diferencias)
    }

    @Test
    fun `una cantidad usada en cero no descuenta nada`() {
        val consumo = calcularConsumo(listOf(usa(huevo, 0.0, Unidad.PIEZA)), listOf(item(1, huevo, 12.0, Unidad.PIEZA)))

        assertTrue(consumo.cambios.isEmpty())
        assertTrue(consumo.diferencias.isEmpty())
    }
}
