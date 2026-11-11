package com.bakimi.domain.shopping

import com.bakimi.domain.inventory.InventoryItem
import kotlinx.coroutines.flow.Flow

/** Weekly plan preferences. */
data class PlanSettings(
    val people: Int = 1,
    /** Approximate budget in MXN for the purchase; `null` if not set. */
    val budget: Double? = null,
)

/** Recipe chosen for the week, stored by id. */
data class PlanEntry(val recipeId: String, val servings: Int)

interface PlanRepository {
    fun observeSettings(): Flow<PlanSettings>

    fun observeRecipes(): Flow<List<PlanEntry>>

    suspend fun saveSettings(config: PlanSettings)

    /** Adds the recipe, or updates its servings if it was already there. */
    suspend fun saveRecipe(entry: PlanEntry)

    suspend fun removeRecipe(recipeId: String)
}

interface ShoppingRepository {
    fun observeMarks(): Flow<List<PurchaseMark>>

    fun observeManuals(): Flow<List<ManualItem>>

    suspend fun getMark(ingredientId: String): PurchaseMark?

    suspend fun getManual(id: Long): ManualItem?

    suspend fun saveMark(mark: PurchaseMark)

    suspend fun saveManual(purchaseItem: ManualItem): Long

    suspend fun deleteManual(id: Long)

    /**
  * Registers the purchase in a single transaction: saves the inventory records and deletes the
  * processed marks and manual products. If anything fails, nothing changes.
  */
    suspend fun registerPurchase(
        inventory: List<InventoryItem>,
        processedMarks: List<String>,
        processedManuals: List<Long>,
    )
}
