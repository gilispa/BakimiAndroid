package com.bakimi.domain.cooking

import com.bakimi.domain.inventory.InventoryRepository
import com.bakimi.domain.inventory.InventoryItem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Confirmed preparation of a recipe.
 *
 * @property id unique per Cooking Mode session; prevents registering the same preparation twice.
 */
data class Preparation(val id: String, val recipeId: String, val servings: Int, val dateMillis: Long)

interface PreparationRepository {
    /**
  * Saves the preparation and the inventory changes in a single transaction.
  *
  * @return `false`, without changing anything, if a preparation with the same id already existed.
  */
    suspend fun register(preparation: Preparation, changes: List<InventoryItem>): Boolean
}

sealed interface PreparationResult {
    data class Registered(val consumption: Consumption) : PreparationResult

    /** This preparation had already been confirmed; nothing was deducted again. */
    data object AlreadyRegistered : PreparationResult
}

/** Cooking Mode: opening or leaving does not touch the inventory; only [confirm] deducts. */
class CookingManager(
    private val inventory: InventoryRepository,
    private val preparations: PreparationRepository,
    private val now: () -> Long = System::currentTimeMillis,
) {
    private val lock = Mutex()

    /** Preview of the deduction, without saving anything. */
    suspend fun calculate(usedItems: List<UsedIngredient>): Consumption =
        calculateConsumption(usedItems, inventory.observeAll().first())

    /** Calculates with the latest inventory and registers everything in a single transaction. */
    suspend fun confirm(
        preparationId: String,
        recipeId: String,
        servings: Int,
        usedItems: List<UsedIngredient>,
    ): PreparationResult = lock.withLock {
        val consumption = calculate(usedItems)
        val registered = preparations.register(
            Preparation(preparationId, recipeId, servings, now()),
            consumption.changes,
        )
        if (registered) PreparationResult.Registered(consumption) else PreparationResult.AlreadyRegistered
    }
}
