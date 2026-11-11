package com.bakimi.data.shopping

import androidx.room.withTransaction
import com.bakimi.data.BakimiDatabase
import com.bakimi.data.inventory.toEntity
import com.bakimi.domain.shopping.ManualItem
import com.bakimi.domain.shopping.ShoppingRepository
import com.bakimi.domain.shopping.PlanSettings
import com.bakimi.domain.shopping.PlanEntry
import com.bakimi.domain.shopping.PurchaseMark
import com.bakimi.domain.shopping.PlanRepository
import com.bakimi.domain.inventory.InventoryItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RoomPlanRepository @Inject constructor(private val dao: PlanDao) : PlanRepository {

    override fun observeSettings(): Flow<PlanSettings> = dao.observeSettings().map { it?.toDomain() ?: PlanSettings() }

    override fun observeRecipes(): Flow<List<PlanEntry>> =
        dao.observeRecipes().map { rows -> rows.map { it.toDomain() } }

    override suspend fun saveSettings(config: PlanSettings) = dao.saveSettings(config.toEntity())

    override suspend fun saveRecipe(entry: PlanEntry) {
        // Keeps the original order if the recipe was already in the plan.
        val addedAt = dao.getRecipe(entry.recipeId)?.addedAt ?: System.currentTimeMillis()
        dao.saveRecipe(PlanRecipeEntity(entry.recipeId, entry.servings, addedAt))
    }

    override suspend fun removeRecipe(recipeId: String) = dao.removeRecipe(recipeId)
}

class RoomShoppingRepository @Inject constructor(
    private val db: BakimiDatabase,
    private val dao: ShoppingDao,
) : ShoppingRepository {

    override fun observeMarks(): Flow<List<PurchaseMark>> =
        dao.observeMarks().map { rows -> rows.map { it.toDomain() } }

    override fun observeManuals(): Flow<List<ManualItem>> =
        dao.observeManuals().map { rows -> rows.map { it.toDomain() } }

    override suspend fun getMark(ingredientId: String): PurchaseMark? = dao.getMark(ingredientId)?.toDomain()

    override suspend fun getManual(id: Long): ManualItem? = dao.getManual(id)?.toDomain()

    override suspend fun saveMark(mark: PurchaseMark) = dao.saveMark(mark.toEntity())

    override suspend fun saveManual(purchaseItem: ManualItem): Long {
        val id = dao.saveManual(purchaseItem.toEntity())
        return if (purchaseItem.id == 0L) id else purchaseItem.id
    }

    override suspend fun deleteManual(id: Long) = dao.deleteManual(id)

    override suspend fun registerPurchase(
        inventory: List<InventoryItem>,
        processedMarks: List<String>,
        processedManuals: List<Long>,
    ) {
        db.withTransaction {
            val inventoryDao = db.inventoryDao()
            inventory.forEach { inventoryDao.save(it.toEntity()) }
            if (processedMarks.isNotEmpty()) dao.deleteMarks(processedMarks)
            if (processedManuals.isNotEmpty()) dao.deleteManuals(processedManuals)
        }
    }
}
