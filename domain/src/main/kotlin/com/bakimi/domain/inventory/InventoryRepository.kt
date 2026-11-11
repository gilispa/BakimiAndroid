package com.bakimi.domain.inventory

import kotlinx.coroutines.flow.Flow

/** Persistent access to the inventory. The implementation lives in :data. */
interface InventoryRepository {
    fun observeAll(): Flow<List<InventoryItem>>

    suspend fun get(id: Long): InventoryItem?

    /** Inserts if `item.id == 0`, otherwise updates. Returns the saved id. */
    suspend fun save(item: InventoryItem): Long

    suspend fun delete(id: Long)
}
