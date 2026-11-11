package com.bakimi.ui.cooking

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bakimi.domain.cooking.Consumption
import com.bakimi.domain.cooking.CookingManager
import com.bakimi.domain.cooking.UsedIngredient
import com.bakimi.domain.cooking.PreparationResult
import com.bakimi.domain.cooking.defaultUsage
import com.bakimi.domain.shopping.ShoppingManager
import com.bakimi.domain.inventory.InventoryManager
import com.bakimi.domain.inventory.InventoryItem
import com.bakimi.domain.inventory.parseQuantity
import com.bakimi.domain.recipes.Recipe
import com.bakimi.domain.recipes.RecipeRepository
import com.bakimi.ui.fridge.editableText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

enum class CookingPhase { COOKING, CONFIRMING, FINISHED }

/** What the user says they used of an ingredient, as editable text. */
data class EditableUsage(val used: UsedIngredient, val text: String) {
    val quantity: Double? get() = parseQuantity(text)
}

data class CookingModeUiState(
    val recipe: Recipe,
    val servings: Int,
    val step: Int = 0,
    val phase: CookingPhase = CookingPhase.COOKING,
    val preparedServings: Int = servings,
    val usages: List<EditableUsage> = emptyList(),
    val consumption: Consumption? = null,
    /** Quantities before the deduction, to show "12 → 8". */
    val before: Map<Long, InventoryItem> = emptyMap(),
    val saving: Boolean = false,
    val alreadyRegistered: Boolean = false,
    val error: Boolean = false,
) {
    val totalSteps: Int get() = recipe.steps.size
    val isLastStep: Boolean get() = step == totalSteps - 1
    val usagesValid: Boolean get() = usages.all { it.quantity != null }
}

@HiltViewModel
class CookingModeViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    recipes: RecipeRepository,
    private val cooking: CookingManager,
    private val inventory: InventoryManager,
) : ViewModel() {

    private val recipe: Recipe? = savedStateHandle.get<String>(ARG_RECIPE)?.let(recipes::get)

    /** One id per session: even if confirmed twice, it is deducted only once. */
    private val preparationId: String =
        savedStateHandle.get<String>(KEY_ID) ?: UUID.randomUUID().toString().also { savedStateHandle[KEY_ID] = it }

    private val _state = MutableStateFlow(
        recipe?.let { r ->
            val servings = (savedStateHandle.get<Int>(ARG_SERVINGS) ?: r.baseServings)
                .coerceIn(ShoppingManager.MIN_SERVINGS, ShoppingManager.MAX_SERVINGS)
            // The current step is kept if Android recreates the screen.
            CookingModeUiState(r, servings, step = savedStateHandle.get<Int>(KEY_STEP) ?: 0)
        },
    )
    val state: StateFlow<CookingModeUiState?> = _state.asStateFlow()

    fun next() = moveTo((_state.value?.step ?: 0) + 1)

    fun previous() = moveTo((_state.value?.step ?: 0) - 1)

    private fun moveTo(step: Int) {
        _state.update { current ->
            current?.copy(step = step.coerceIn(0, current.totalSteps - 1))
        }
        savedStateHandle[KEY_STEP] = _state.value?.step
    }

    /** Moves to confirmation; it does not touch the inventory yet. */
    fun finishCooking() {
        val current = _state.value ?: return
        _state.update { it?.copy(phase = CookingPhase.CONFIRMING) }
        prefill(current.preparedServings)
    }

    fun backToSteps() = _state.update { it?.copy(phase = CookingPhase.COOKING) }

    /** Changing the servings recalculates the used quantities from the recipe. */
    fun changePreparedServings(delta: Int) {
        val current = _state.value ?: return
        val servings = (current.preparedServings + delta).coerceIn(ShoppingManager.MIN_SERVINGS, ShoppingManager.MAX_SERVINGS)
        _state.update { it?.copy(preparedServings = servings) }
        prefill(servings)
    }

    fun changeUsage(index: Int, text: String) {
        _state.update { current ->
            current?.copy(usages = current.usages.mapIndexed { i, usage -> if (i == index) usage.copy(text = text) else usage })
        }
        recalculate()
    }

    private fun prefill(servings: Int) {
        val r = recipe ?: return
        _state.update { current ->
            current?.copy(usages = defaultUsage(r, servings).map { EditableUsage(it, editableText(it.quantity)) })
        }
        recalculate()
    }

    private var calculation: Job? = null

    private fun recalculate() {
        val current = _state.value ?: return
        if (!current.usagesValid) return
        // If the user keeps typing, the previous calculation is no longer useful.
        calculation?.cancel()
        calculation = viewModelScope.launch {
            val before = inventory.observe().first().associateBy { it.id }
            val consumption = cooking.calculate(usedItems(current))
            _state.update { it?.copy(consumption = consumption, before = before) }
        }
    }

    fun confirm() {
        val current = _state.value ?: return
        if (current.saving || !current.usagesValid) return
        _state.update { it?.copy(saving = true, error = false) }
        viewModelScope.launch {
            try {
                val result = cooking.confirm(preparationId, current.recipe.id, current.preparedServings, usedItems(current))
                _state.update {
                    it?.copy(
                        saving = false,
                        phase = CookingPhase.FINISHED,
                        alreadyRegistered = result == PreparationResult.AlreadyRegistered,
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it?.copy(saving = false, error = true) }
            }
        }
    }

    private fun usedItems(state: CookingModeUiState): List<UsedIngredient> =
        state.usages.map { it.used.copy(quantity = it.quantity ?: 0.0) }

    companion object {
        const val ARG_RECIPE = "recipeId"
        const val ARG_SERVINGS = "servings"
        private const val KEY_STEP = "step"
        private const val KEY_ID = "preparationId"
    }
}
