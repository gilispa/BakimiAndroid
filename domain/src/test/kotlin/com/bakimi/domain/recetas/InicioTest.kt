package com.bakimi.domain.recetas

import com.bakimi.domain.ingredientes.Ingrediente
import com.bakimi.domain.inventario.Unidad
import org.junit.Assert.assertEquals
import org.junit.Test

class InicioTest {

    /** Evaluación con [faltan] ingredientes faltantes de un total de [total]. */
    private fun evaluacion(id: String, faltan: Int, total: Int = 4, minutos: Int = 30): EvaluacionReceta {
        val ingredientes = (1..total).map { i ->
            val requerido = necesita(Ingrediente("$id$i", "$id$i"), 1.0, Unidad.PIEZA)
            val estado = if (i <= faltan) EstadoIngrediente.FALTA else EstadoIngrediente.COMPLETO
            IngredienteEvaluado(requerido, disponible = 0.0, faltante = 0.0, estado = estado)
        }
        return EvaluacionReceta(receta(id, minutos = minutos), porciones = 2, ingredientes = ingredientes)
    }

    private fun List<EvaluacionReceta>.ids() = map { it.receta.id }

    @Test
    fun `ordena por menos faltantes, luego mas ingredientes aprovechados, luego menos tiempo`() {
        val inicio = armarInicio(
            listOf(
                evaluacion("tres_faltan", faltan = 3),
                evaluacion("lista_lenta", faltan = 0, minutos = 40),
                evaluacion("una_falta_de_cuatro", faltan = 1, total = 4),
                evaluacion("lista_rapida", faltan = 0, minutos = 10),
                evaluacion("una_falta_de_dos", faltan = 1, total = 2),
            ),
        )

        assertEquals(
            listOf("lista_rapida", "lista_lenta", "una_falta_de_cuatro", "una_falta_de_dos", "tres_faltan"),
            inicio.destacadas.ids(),
        )
    }

    @Test
    fun `limita el carrusel`() {
        val inicio = armarInicio((1..10).map { evaluacion("r$it", faltan = 0) }, maxDestacadas = 6)

        assertEquals(6, inicio.destacadas.size)
    }

    @Test
    fun `reparte las secciones segun faltantes y tiempo`() {
        val inicio = armarInicio(
            listOf(
                evaluacion("lista", faltan = 0, minutos = 15),
                evaluacion("falta_una", faltan = 1, minutos = 45),
                evaluacion("faltan_dos", faltan = 2, minutos = 20),
                evaluacion("faltan_tres", faltan = 3, minutos = 10),
            ),
        )

        assertEquals(listOf("lista"), inicio.puedesCocinar.ids())
        assertEquals(listOf("falta_una", "faltan_dos"), inicio.faltanPocos.ids())
        assertEquals(listOf("lista", "faltan_dos", "faltan_tres"), inicio.rapidas.ids())
        assertEquals(listOf("faltan_tres"), inicio.masIdeas.ids())
    }
}
