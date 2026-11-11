package com.bakimi.domain.inventario

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

sealed interface ResultadoGuardado {
    data class Guardado(val id: Long) : ResultadoGuardado
    data class Invalido(val errores: Set<ErrorItem>) : ResultadoGuardado
}

/** Operaciones de Mi refri: registrar, corregir, sumar o restar, marcar agotados y eliminar. */
class GestorInventario(private val repositorio: InventarioRepository) {

    /** Serializa las operaciones de leer y escribir para que los toques rápidos no se pisen. */
    private val cerrojo = Mutex()

    fun observar(): Flow<List<ItemInventario>> = repositorio.observarTodos()

    suspend fun obtener(id: Long): ItemInventario? = repositorio.obtener(id)

    /** Valida y guarda el alimento con el nombre sin espacios sobrantes. */
    suspend fun guardar(item: ItemInventario): ResultadoGuardado {
        val limpio = item.copy(nombre = item.nombre.trim())
        val errores = validar(limpio)
        if (errores.isNotEmpty()) return ResultadoGuardado.Invalido(errores)
        return cerrojo.withLock { ResultadoGuardado.Guardado(repositorio.guardar(limpio)) }
    }

    /**
     * Suma (`pasos > 0`) o resta (`pasos < 0`) el paso rápido de la unidad del alimento.
     * Nunca deja una cantidad negativa: al restar de más el alimento queda agotado.
     */
    suspend fun ajustar(id: Long, pasos: Int): ItemInventario? = cerrojo.withLock {
        val item = repositorio.obtener(id) ?: return@withLock null
        val nuevaCantidad = maxOf(0.0, item.cantidad + pasos * item.unidad.pasoRapido)
        val ajustado = item.copy(cantidad = redondear(nuevaCantidad, decimales = 3))
        repositorio.guardar(ajustado)
        ajustado
    }

    suspend fun marcarAgotado(id: Long) {
        cerrojo.withLock {
            val item = repositorio.obtener(id) ?: return@withLock
            repositorio.guardar(item.copy(cantidad = 0.0))
        }
    }

    suspend fun eliminar(id: Long) {
        cerrojo.withLock { repositorio.eliminar(id) }
    }
}
