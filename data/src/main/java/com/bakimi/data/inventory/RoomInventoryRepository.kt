package com.bakimi.data.inventory

import com.bakimi.domain.inventory.InventoryRepository
import com.bakimi.domain.inventory.InventoryItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RoomInventoryRepository @Inject constructor(
    private val dao: InventoryDao,
) : InventoryRepository {

    override fun observeAll(): Flow<List<InventoryItem>> =
        dao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override suspend fun get(id: Long): InventoryItem? = dao.get(id)?.toDomain()

    override suspend fun save(item: InventoryItem): Long {
        val insertedId = dao.save(item.toEntity())
        return if (item.id == 0L) insertedId else item.id
    }

    override suspend fun delete(id: Long) = dao.delete(id)
}
