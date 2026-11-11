package com.bakimi.domain.cocina

import com.bakimi.domain.inventario.InventarioRepository
import com.bakimi.domain.inventario.ItemInventario
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Preparación confirmada de una receta.
 *
 * @property id único por sesión de Modo Cocina; impide registrar la misma preparación dos veces.
 */
data class Preparacion(val id: String, val recetaId: String, val porciones: Int, val fechaMillis: Long)

interface PreparacionRepository {
    /**
     * Guarda la preparación y los cambios de inventario en una sola transacción.
     *
     * @return `false`, sin cambiar nada, si ya había una preparación con el mismo id.
     */
    suspend fun registrar(preparacion: Preparacion, cambios: List<ItemInventario>): Boolean
}

sealed interface ResultadoPreparacion {
    data class Registrada(val consumo: Consumo) : ResultadoPreparacion

    /** Ya se había confirmado esta preparación; no se descontó otra vez. */
    data object YaRegistrada : ResultadoPreparacion
}

/** Modo Cocina: abrir o abandonar no toca el inventario; solo [confirmar] descuenta. */
class GestorCocina(
    private val inventario: InventarioRepository,
    private val preparaciones: PreparacionRepository,
    private val ahora: () -> Long = System::currentTimeMillis,
) {
    private val cerrojo = Mutex()

    /** Vista previa del descuento, sin guardar nada. */
    suspend fun calcular(usados: List<IngredienteUsado>): Consumo =
        calcularConsumo(usados, inventario.observarTodos().first())

    /** Calcula con el inventario más reciente y registra todo en una sola transacción. */
    suspend fun confirmar(
        idPreparacion: String,
        recetaId: String,
        porciones: Int,
        usados: List<IngredienteUsado>,
    ): ResultadoPreparacion = cerrojo.withLock {
        val consumo = calcular(usados)
        val registrada = preparaciones.registrar(
            Preparacion(idPreparacion, recetaId, porciones, ahora()),
            consumo.cambios,
        )
        if (registrada) ResultadoPreparacion.Registrada(consumo) else ResultadoPreparacion.YaRegistrada
    }
}
