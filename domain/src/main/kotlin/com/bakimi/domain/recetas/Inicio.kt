package com.bakimi.domain.recetas

/** Minutos máximos para la sección "Listo en menos de 20 minutos". */
const val MINUTOS_RAPIDA = 20

data class SeccionesInicio(
    /** Recetas del carrusel: las mejores según lo que hay en casa. */
    val destacadas: List<EvaluacionReceta>,
    val puedesCocinar: List<EvaluacionReceta>,
    /** Recetas a las que les faltan uno o dos ingredientes. */
    val faltanPocos: List<EvaluacionReceta>,
    val rapidas: List<EvaluacionReceta>,
    /** El resto de las recetas. */
    val masIdeas: List<EvaluacionReceta>,
)

/**
 * Ordena las recetas por menos faltantes, luego más ingredientes aprovechados, luego menos
 * tiempo, y las reparte en las secciones de Inicio.
 */
fun armarInicio(evaluaciones: List<EvaluacionReceta>, maxDestacadas: Int = 6): SeccionesInicio {
    val ordenadas = evaluaciones.sortedWith(
        compareBy<EvaluacionReceta>(
            { it.faltantes },
            { -it.completos },
            { it.receta.minutos },
            { it.receta.nombre },
        ),
    )
    return SeccionesInicio(
        destacadas = ordenadas.take(maxDestacadas),
        puedesCocinar = ordenadas.filter { it.lista },
        faltanPocos = ordenadas.filter { it.faltantes in 1..2 },
        rapidas = ordenadas.filter { it.receta.minutos <= MINUTOS_RAPIDA },
        masIdeas = ordenadas.filter { it.faltantes > 2 },
    )
}
