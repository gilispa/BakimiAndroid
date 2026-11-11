package com.bakimi.domain.compras

import com.bakimi.domain.ingredientes.Ingrediente
import com.bakimi.domain.ingredientes.InfoCompra
import com.bakimi.domain.inventario.Unidad
import com.bakimi.domain.recetas.hay
import com.bakimi.domain.recetas.necesita
import com.bakimi.domain.recetas.receta
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CompraTest {

    private val pollo = Ingrediente(
        "pechuga_pollo",
        "Pechuga de pollo",
        compra = InfoCompra(Unidad.GRAMO, presentacion = 100.0, precio = 17.0),
    )
    private val arroz = Ingrediente(
        "arroz",
        "Arroz",
        compra = InfoCompra(Unidad.GRAMO, presentacion = 1000.0, empaque = "bolsa de 1 kg", precio = 34.0),
    )
    private val cebolla = Ingrediente("cebolla", "Cebolla")
    private val tortilla = Ingrediente(
        "tortilla_maiz",
        "Tortilla de maíz",
        gramosPorPieza = 30.0,
        compra = InfoCompra(Unidad.GRAMO, presentacion = 500.0, precio = 14.0),
    )

    private val polloConArroz = receta(
        "pollo_arroz",
        porcionesBase = 2,
        ingredientes = arrayOf(necesita(pollo, 600.0, Unidad.GRAMO), necesita(arroz, 200.0, Unidad.GRAMO)),
    )
    private val tacos = receta(
        "tacos",
        porcionesBase = 2,
        ingredientes = arrayOf(necesita(pollo, 600.0, Unidad.GRAMO), necesita(cebolla, 0.75, Unidad.PIEZA)),
    )

    private fun necesidad(plan: List<RecetaPlaneada>, ingrediente: Ingrediente, vararg inventario: com.bakimi.domain.inventario.ItemInventario) =
        consolidarPlan(plan, inventario.toList()).single { it.ingrediente == ingrediente }

    @Test
    fun `dos recetas con 1,2 kg de pollo y 1 kg en casa generan 200 g faltantes`() {
        val plan = listOf(RecetaPlaneada(polloConArroz, 2), RecetaPlaneada(tacos, 2))

        val pollo = necesidad(plan, pollo, hay("Pechuga de pollo", 1.0, Unidad.KILOGRAMO))

        assertEquals(1200.0, pollo.requerido, 0.0)
        assertEquals(200.0, pollo.faltante, 0.0)
    }

    @Test
    fun `un ingrediente compartido aparece una sola vez con las recetas que lo usan`() {
        val plan = listOf(RecetaPlaneada(polloConArroz, 2), RecetaPlaneada(tacos, 2))

        val necesidades = consolidarPlan(plan, emptyList())

        assertEquals(1, necesidades.count { it.ingrediente == pollo })
        assertEquals(listOf("pollo_arroz", "tacos"), necesidades.single { it.ingrediente == pollo }.recetas)
    }

    @Test
    fun `cambiar porciones recalcula lo que se necesita`() {
        val plan = listOf(RecetaPlaneada(polloConArroz, 1), RecetaPlaneada(tacos, 4))

        assertEquals(1500.0, necesidad(plan, pollo).requerido, 0.0)
    }

    @Test
    fun `si faltan 200 g de arroz se compra la bolsa completa de 1 kg con su precio completo`() {
        val plan = listOf(RecetaPlaneada(polloConArroz, 2))

        val compra = calcularCompra(necesidad(plan, arroz))!!

        assertEquals(1, compra.presentaciones)
        assertEquals(1000.0, compra.cantidad, 0.0)
        assertEquals("bolsa de 1 kg", compra.empaque)
        assertEquals(34.0, compra.precio!!, 0.0)
    }

    @Test
    fun `si no alcanza una bolsa se compran las necesarias`() {
        val plan = listOf(RecetaPlaneada(polloConArroz, 12))

        val compra = calcularCompra(necesidad(plan, arroz))!!

        assertEquals(2, compra.presentaciones)
        assertEquals(68.0, compra.precio!!, 0.0)
    }

    @Test
    fun `a granel se redondea al paso de compra`() {
        val plan = listOf(RecetaPlaneada(polloConArroz, 2))

        val compra = calcularCompra(necesidad(plan, pollo, hay("Pechuga de pollo", 350.0, Unidad.GRAMO)))!!

        assertEquals(300.0, compra.cantidad, 0.0)
        assertEquals(51.0, compra.precio!!, 0.0)
    }

    @Test
    fun `las piezas se compran completas y sin precio conocido el precio queda vacio`() {
        val plan = listOf(RecetaPlaneada(tacos, 2))

        val compra = calcularCompra(necesidad(plan, cebolla))!!

        assertEquals(Unidad.PIEZA, compra.unidad)
        assertEquals(1.0, compra.cantidad, 0.0)
        assertNull(compra.precio)
    }

    @Test
    fun `las piezas se convierten a la unidad de compra de forma aproximada`() {
        val quesadillas = receta("quesadillas", ingredientes = arrayOf(necesita(tortilla, 6.0, Unidad.PIEZA)))

        val compra = calcularCompra(necesidad(listOf(RecetaPlaneada(quesadillas, 2)), tortilla))!!

        assertEquals(Unidad.GRAMO, compra.unidad)
        assertEquals(500.0, compra.cantidad, 0.0)
        assertTrue(compra.aproximado)
    }

    @Test
    fun `lo que ya alcanza no entra a la lista`() {
        val plan = listOf(RecetaPlaneada(polloConArroz, 2))

        assertNull(calcularCompra(necesidad(plan, arroz, hay("Arroz", 1.0, Unidad.KILOGRAMO))))
    }

    @Test
    fun `un precio desconocido no cuenta como cero y marca el total como parcial`() {
        val total = totalDe(listOf(34.0, null, 51.0))

        assertEquals(85.0, total.total, 0.0)
        assertTrue(total.parcial)
        assertFalse(totalDe(listOf(34.0, 51.0)).parcial)
    }

    @Test
    fun `el presupuesto solo se da por cumplido si se conocen todos los precios`() {
        assertEquals(EstadoPresupuesto.DENTRO, estadoPresupuesto(TotalCompra(320.0, parcial = false), 500.0))
        assertEquals(EstadoPresupuesto.INCIERTO, estadoPresupuesto(TotalCompra(320.0, parcial = true), 500.0))
        assertEquals(EstadoPresupuesto.EXCEDIDO, estadoPresupuesto(TotalCompra(520.0, parcial = true), 500.0))
        assertEquals(EstadoPresupuesto.SIN_PRESUPUESTO, estadoPresupuesto(TotalCompra(520.0, parcial = false), null))
    }
}
