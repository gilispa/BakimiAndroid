package com.bakimi.data.inventario

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface InventarioDao {

    @Query("SELECT * FROM inventario")
    fun observarTodos(): Flow<List<InventarioEntity>>

    @Query("SELECT * FROM inventario WHERE id = :id")
    suspend fun obtener(id: Long): InventarioEntity?

    /** Devuelve el id nuevo al insertar, o -1 cuando actualiza una fila existente. */
    @Upsert
    suspend fun guardar(entidad: InventarioEntity): Long

    @Query("DELETE FROM inventario WHERE id = :id")
    suspend fun eliminar(id: Long)
}
