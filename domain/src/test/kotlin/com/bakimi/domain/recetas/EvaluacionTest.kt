package com.bakimi.domain.recetas

import com.bakimi.domain.inventario.Ubicacion
import com.bakimi.domain.ingredientes.Ingrediente
import com.bakimi.domain.inventario.Unidad
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EvaluacionTest {

    private val polloConArroz = receta(
        "pollo_arroz",
        porcionesBase = 2,
        ingredientes = arrayOf(
            necesita(POLLO, 600.0, Unidad.GRAMO),
            necesita(ARROZ, 200.0, Unidad.GRAMO),
        ),
    )

    private fun evaluado(evaluacion: EvaluacionReceta, ingrediente: Ingrediente) =
        evaluacion.ingredientes.single { it.requerido.ingrediente == ingrediente }

    @Test
    fun `una receta con cantidades suficientes aparece lista`() {
        val evaluacion = evaluarReceta(
            polloConArroz,
            porciones = 2,
            inventario = listOf(hay("Pollo", 1.0, Unidad.KILOGRAMO), hay("Arroz", 500.0, Unidad.GRAMO)),
        )

        assertTrue(evaluacion.lista)
        assertEquals(0, evaluacion.faltantes)
    }

    @Test
    fun `requiere 600 g de pollo y hay 400 g, faltan 200 g`() {
        val evaluacion = evaluarReceta(
            polloConArroz,
            porciones = 2,
            inventario = listOf(hay("Pechuga de pollo", 400.0, Unidad.GRAMO), hay("Arroz", 1.0, Unidad.KILOGRAMO)),
        )

        val pollo = evaluado(evaluacion, POLLO)
        assertEquals(EstadoIngrediente.INSUFICIENTE, pollo.estado)
        assertEquals(200.0, pollo.faltante, 0.0)
        assertFalse(evaluacion.lista)
        assertEquals(1, evaluacion.faltantes)
    }

    @Test
    fun `un ingrediente sin registro falta por completo`() {
        val evaluacion = evaluarReceta(polloConArroz, porciones = 2, inventario = emptyList())

        val arroz = evaluado(evaluacion, ARROZ)
        assertEquals(EstadoIngrediente.FALTA, arroz.estado)
        assertEquals(200.0, arroz.faltante, 0.0)
    }

    @Test
    fun `un ingrediente agotado cuenta como faltante`() {
        val evaluacion = evaluarReceta(
            polloConArroz,
            porciones = 2,
            inventario = listOf(hay("Arroz", 0.0, Unidad.GRAMO)),
        )

        assertEquals(EstadoIngrediente.FALTA, evaluado(evaluacion, ARROZ).estado)
    }

    @Test
    fun `suma registros del mismo ingrediente en distintas ubicaciones`() {
        val evaluacion = evaluarReceta(
            polloConArroz,
            porciones = 2,
            inventario = listOf(
                hay("Pollo", 300.0, Unidad.GRAMO, Ubicacion.REFRIGERADOR),
                hay("Pollo", 0.5, Unidad.KILOGRAMO, Ubicacion.CONGELADOR),
            ),
        )

        val pollo = evaluado(evaluacion, POLLO)
        assertEquals(EstadoIngrediente.COMPLETO, pollo.estado)
        assertEquals(800.0, pollo.disponible, 0.0)
    }

    @Test
    fun `ajusta las cantidades a las porciones elegidas`() {
        val evaluacion = evaluarReceta(
            polloConArroz,
            porciones = 1,
            inventario = listOf(hay("Pollo", 400.0, Unidad.GRAMO)),
        )

        val pollo = evaluado(evaluacion, POLLO)
        assertEquals(300.0, pollo.requerido.cantidad, 0.0)
        assertEquals(EstadoIngrediente.COMPLETO, pollo.estado)
    }

    @Test
    fun `no convierte piezas a gramos`() {
        val evaluacion = evaluarReceta(
            polloConArroz,
            porciones = 2,
            inventario = listOf(hay("Pollo", 2.0, Unidad.PIEZA)),
        )

        val pollo = evaluado(evaluacion, POLLO)
        assertEquals(EstadoIngrediente.OTRA_UNIDAD, pollo.estado)
        assertEquals(600.0, pollo.faltante, 0.0)
    }

    private val tortilla = Ingrediente("tortilla_maiz", "Tortilla de maíz", gramosPorPieza = 30.0)
    private val quesadillas = receta(
        "quesadillas",
        porcionesBase = 2,
        ingredientes = arrayOf(necesita(tortilla, 6.0, Unidad.PIEZA)),
    )

    @Test
    fun `un kilo de tortillas alcanza para 6 tortillas, de forma aproximada`() {
        val evaluacion = evaluarReceta(
            quesadillas,
            porciones = 2,
            inventario = listOf(hay("Tortilla de maíz", 1.0, Unidad.KILOGRAMO)),
        )

        val tortillas = evaluado(evaluacion, tortilla)
        assertEquals(EstadoIngrediente.COMPLETO, tortillas.estado)
        assertEquals(33.3, tortillas.disponible, 0.0)
        assertTrue(tortillas.aproximado)
        assertTrue(evaluacion.lista)
    }

    @Test
    fun `100 g de tortillas no alcanzan y el faltante es aproximado`() {
        val evaluacion = evaluarReceta(
            quesadillas,
            porciones = 2,
            inventario = listOf(hay("Tortilla de maíz", 100.0, Unidad.GRAMO)),
        )

        val tortillas = evaluado(evaluacion, tortilla)
        assertEquals(EstadoIngrediente.INSUFICIENTE, tortillas.estado)
        assertEquals(2.7, tortillas.faltante, 0.0)
        assertTrue(tortillas.aproximado)
    }

    @Test
    fun `sin conversion aproximada el resultado no se marca como aproximado`() {
        val evaluacion = evaluarReceta(
            quesadillas,
            porciones = 2,
            inventario = listOf(hay("Tortilla de maíz", 10.0, Unidad.PIEZA)),
        )

        assertFalse(evaluado(evaluacion, tortilla).aproximado)
    }

    @Test
    fun `con suficiente en unidad compatible ignora el registro en otra unidad`() {
        val evaluacion = evaluarReceta(
            polloConArroz,
            porciones = 2,
            inventario = listOf(hay("Pollo", 2.0, Unidad.PIEZA), hay("Pollo", 1.0, Unidad.KILOGRAMO)),
        )

        assertEquals(EstadoIngrediente.COMPLETO, evaluado(evaluacion, POLLO).estado)
    }

    @Test
    fun `un registro vinculado al catalogo cuenta aunque su nombre sea distinto`() {
        val evaluacion = evaluarReceta(
            polloConArroz,
            porciones = 2,
            inventario = listOf(hay("Pollo de rancho", 1.0, Unidad.KILOGRAMO, ingredienteId = POLLO.id)),
        )

        assertEquals(EstadoIngrediente.COMPLETO, evaluado(evaluacion, POLLO).estado)
    }

    @Test
    fun `un registro vinculado a otro ingrediente no cuenta aunque su nombre coincida`() {
        val evaluacion = evaluarReceta(
            polloConArroz,
            porciones = 2,
            inventario = listOf(hay("Pollo", 1.0, Unidad.KILOGRAMO, ingredienteId = "caldo_pollo")),
        )

        assertEquals(EstadoIngrediente.FALTA, evaluado(evaluacion, POLLO).estado)
    }

    @Test
    fun `no confunde un producto distinto con el ingrediente`() {
        val evaluacion = evaluarReceta(
            polloConArroz,
            porciones = 2,
            inventario = listOf(hay("Caldo de pollo", 1.0, Unidad.LITRO)),
        )

        assertEquals(EstadoIngrediente.FALTA, evaluado(evaluacion, POLLO).estado)
    }
}
