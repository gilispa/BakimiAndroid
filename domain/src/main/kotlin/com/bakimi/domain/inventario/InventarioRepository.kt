package com.bakimi.domain.inventario

import kotlinx.coroutines.flow.Flow

/** Acceso persistente al inventario. La implementación vive en :data. */
interface InventarioRepository {
    fun observarTodos(): Flow<List<ItemInventario>>

    suspend fun obtener(id: Long): ItemInventario?

    /** Inserta si `item.id == 0`, si no actualiza. Devuelve el id guardado. */
    suspend fun guardar(item: ItemInventario): Long

    suspend fun eliminar(id: Long)
}
