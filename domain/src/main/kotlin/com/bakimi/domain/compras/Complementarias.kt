package com.bakimi.domain.compras

import com.bakimi.domain.ingredientes.Ingrediente
import com.bakimi.domain.ingredientes.convertir
import com.bakimi.domain.inventario.ItemInventario
import com.bakimi.domain.recetas.Receta

/**
 * Receta que aprovecha lo que sobraría después de cubrir el plan.
 *
 * @property compartidos ingredientes de la receta que sobran del plan (ya en casa o por comprar).
 */
data class Recomendacion(val receta: Receta, val compartidos: List<Ingrediente>)

/**
 * Recomienda recetas fuera del plan que usan ingredientes que sobrarían después de cubrirlo:
 * sobrante = existencia + compra en presentaciones completas − requerido por el plan.
 *
 * No cuenta los básicos (sal, aceite) ni ingredientes que el plan ya consume por completo, para
 * no usar dos veces la misma cantidad.
 */
fun recomendarComplementarias(
    plan: List<RecetaPlaneada>,
    inventario: List<ItemInventario>,
    catalogo: List<Receta>,
    limite: Int = 3,
): List<Recomendacion> {
    if (plan.isEmpty()) return emptyList()

    val sobrantes: Set<String> = consolidarPlan(plan, inventario)
        .filter { !it.ingrediente.basico && sobrante(it) > 0.0 }
        .map { it.ingrediente.id }
        .toSet()
    val enPlan = plan.map { it.receta.id }.toSet()

    return catalogo
        .filter { it.id !in enPlan }
        .mapNotNull { receta ->
            val compartidos = receta.ingredientes.map { it.ingrediente }.filter { it.id in sobrantes }.distinct()
            compartidos.takeIf { it.isNotEmpty() }?.let { Recomendacion(receta, it) }
        }
        .sortedWith(compareBy({ -it.compartidos.size }, { it.receta.minutos }, { it.receta.nombre }))
        .take(limite)
}

/** Lo que queda después del plan, contando la compra en presentaciones completas. */
private fun sobrante(necesidad: Necesidad): Double {
    val compra = calcularCompra(necesidad)
    val comprado = compra?.let {
        necesidad.ingrediente.convertir(it.cantidad, it.unidad, necesidad.unidad)?.cantidad
    } ?: 0.0
    return necesidad.disponible + comprado - necesidad.requerido
}
