package com.bakimi.domain.compras

import com.bakimi.domain.ingredientes.Ingrediente
import com.bakimi.domain.inventario.Unidad
import com.bakimi.domain.recetas.hay
import com.bakimi.domain.recetas.necesita
import com.bakimi.domain.recetas.receta
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ComplementariasTest {

    private val lechuga = Ingrediente("lechuga", "Lechuga")
    private val pollo = Ingrediente("pechuga_pollo", "Pechuga de pollo")
    private val pan = Ingrediente("pan_caja", "Pan de caja")
    private val sal = Ingrediente("sal", "Sal", basico = true)
    private val avena = Ingrediente("avena", "Avena")

    private val ensalada = receta(
        "ensalada",
        ingredientes = arrayOf(
            necesita(lechuga, 0.5, Unidad.PIEZA),
            necesita(pollo, 300.0, Unidad.GRAMO),
            necesita(sal, 2.0, Unidad.GRAMO),
        ),
    )
    private val sandwich = receta(
        "sandwich",
        minutos = 20,
        ingredientes = arrayOf(
            necesita(lechuga, 0.25, Unidad.PIEZA),
            necesita(pollo, 200.0, Unidad.GRAMO),
            necesita(pan, 4.0, Unidad.PIEZA),
        ),
    )
    private val tacos = receta(
        "tacos",
        minutos = 10,
        ingredientes = arrayOf(necesita(lechuga, 0.25, Unidad.PIEZA), necesita(pan, 2.0, Unidad.PIEZA)),
    )
    private val soloSal = receta("solo_sal", ingredientes = arrayOf(necesita(sal, 1.0, Unidad.GRAMO), necesita(avena, 50.0, Unidad.GRAMO)))

    private val catalogo = listOf(ensalada, sandwich, tacos, soloSal)

    private fun recomendar(vararg inventario: com.bakimi.domain.inventario.ItemInventario) =
        recomendarComplementarias(listOf(RecetaPlaneada(ensalada, 2)), inventario.toList(), catalogo)

    @Test
    fun `recomienda usar el resto de la lechuga que hay que comprar`() {
        val recomendaciones = recomendar()

        val sandwich = recomendaciones.single { it.receta.id == "sandwich" }
        assertEquals(listOf("lechuga"), sandwich.compartidos.map { it.id })
    }

    @Test
    fun `cuenta tambien lo que ya hay en casa y sobra`() {
        val recomendaciones = recomendar(hay("Pechuga de pollo", 1.0, Unidad.KILOGRAMO))

        val sandwich = recomendaciones.first()
        assertEquals("sandwich", sandwich.receta.id)
        assertEquals(setOf("lechuga", "pechuga_pollo"), sandwich.compartidos.map { it.id }.toSet())
    }

    @Test
    fun `ordena por mas ingredientes compartidos`() {
        val recomendaciones = recomendar(hay("Pechuga de pollo", 1.0, Unidad.KILOGRAMO))

        assertEquals(listOf("sandwich", "tacos"), recomendaciones.map { it.receta.id })
    }

    @Test
    fun `no recomienda por compartir solo basicos como la sal`() {
        assertTrue(recomendar(hay("Sal", 1.0, Unidad.KILOGRAMO)).none { it.receta.id == "solo_sal" })
    }

    @Test
    fun `si el plan consume todo el ingrediente no lo cuenta como compartido`() {
        // La ensalada usa justo la media lechuga que hay; no sobra nada.
        val recomendaciones = recomendar(hay("Lechuga", 0.5, Unidad.PIEZA))

        assertTrue(recomendaciones.none { r -> r.compartidos.any { it.id == "lechuga" } })
    }

    @Test
    fun `no recomienda recetas que ya estan en el plan`() {
        assertTrue(recomendar().none { it.receta.id == "ensalada" })
    }

    @Test
    fun `sin plan no hay recomendaciones`() {
        assertTrue(recomendarComplementarias(emptyList(), emptyList(), catalogo).isEmpty())
    }
}
