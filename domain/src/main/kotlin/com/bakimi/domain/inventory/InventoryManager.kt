package com.bakimi.domain.inventory

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

sealed interface SaveResult {
    data class Saved(val id: Long) : SaveResult
    data class Invalid(val errors: Set<ItemError>) : SaveResult
}

/** Mi refri operations: add, correct, increase or decrease, mark depleted and delete. */
class InventoryManager(private val repository: InventoryRepository) {

    /** Serializes read-and-write operations so quick taps do not overwrite each other. */
    private val lock = Mutex()

    fun observe(): Flow<List<InventoryItem>> = repository.observeAll()

    suspend fun get(id: Long): InventoryItem? = repository.get(id)

    /** Validates and saves the food with the name trimmed. */
    suspend fun save(item: InventoryItem): SaveResult {
        val cleaned = item.copy(name = item.name.trim())
        val errors = validate(cleaned)
        if (errors.isNotEmpty()) return SaveResult.Invalid(errors)
        return lock.withLock { SaveResult.Saved(repository.save(cleaned)) }
    }

    /**
  * Adds (`steps > 0`) or subtracts (`steps < 0`) the unit's quick step from the food.
  * Never leaves a negative quantity: subtracting too much leaves the food depleted.
  */
    suspend fun adjust(id: Long, steps: Int): InventoryItem? = lock.withLock {
        val item = repository.get(id) ?: return@withLock null
        val newQuantity = maxOf(0.0, item.quantity + steps * item.unit.quickStep)
        val adjusted = item.copy(quantity = round(newQuantity, decimals = 3))
        repository.save(adjusted)
        adjusted
    }

    suspend fun markDepleted(id: Long) {
        lock.withLock {
            val item = repository.get(id) ?: return@withLock
            repository.save(item.copy(quantity = 0.0))
        }
    }

    suspend fun delete(id: Long) {
        lock.withLock { repository.delete(id) }
    }
}
