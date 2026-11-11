package com.bakimi.data.inventory

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {

    @Query("SELECT * FROM inventario")
    fun observeAll(): Flow<List<InventoryEntity>>

    @Query("SELECT * FROM inventario WHERE id = :id")
    suspend fun get(id: Long): InventoryEntity?

    /** Returns the new id on insert, or -1 when it updates an existing row. */
    @Upsert
    suspend fun save(entity: InventoryEntity): Long

    @Query("DELETE FROM inventario WHERE id = :id")
    suspend fun delete(id: Long)
}
