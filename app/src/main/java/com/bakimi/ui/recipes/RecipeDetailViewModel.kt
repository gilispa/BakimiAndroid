package com.bakimi.ui.recipes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bakimi.domain.shopping.ShoppingManager
import com.bakimi.domain.inventory.InventoryManager
import com.bakimi.domain.recipes.RecipeEvaluation
import com.bakimi.domain.recipes.RecipeRepository
import com.bakimi.domain.recipes.evaluateRecipe
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface DetailUiState {
    data object Loading : DetailUiState
    data object NotFound : DetailUiState

    /** @property servingsInWeek servings saved in Mi semana; `null` if the recipe is not in the plan. */
    data class Ready(val evaluation: RecipeEvaluation, val servingsInWeek: Int?) : DetailUiState
}

@HiltViewModel
class RecipeDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    recipes: RecipeRepository,
    inventory: InventoryManager,
    private val shopping: ShoppingManager,
) : ViewModel() {

    private val recipe = savedStateHandle.get<String>(ARG_ID)?.let(recipes::get)

    /** Saved in the SavedStateHandle so it survives Android killing the process. */
    private val servings: StateFlow<Int> =
        savedStateHandle.getStateFlow(KEY_SERVINGS, recipe?.baseServings ?: 1)

    val state: StateFlow<DetailUiState> = if (recipe == null) {
        flowOf<DetailUiState>(DetailUiState.NotFound)
            .stateIn(viewModelScope, SharingStarted.Eagerly, DetailUiState.NotFound)
    } else {
        combine(inventory.observe(), servings, shopping.observeWeek()) { stockItems, servings, week ->
            DetailUiState.Ready(
                evaluation = evaluateRecipe(recipe, servings, stockItems),
                servingsInWeek = week.recipes.find { it.recipe.id == recipe.id }?.servings,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DetailUiState.Loading)
    }

    fun changeServings(delta: Int) {
        savedStateHandle[KEY_SERVINGS] = (servings.value + delta).coerceIn(MIN_SERVINGS, MAX_SERVINGS)
    }

    /** Adds the recipe to Mi semana with the chosen servings, or updates the ones it had. */
    fun saveToWeek() = run { recipe?.let { shopping.addToPlan(it.id, servings.value) } }

    fun removeFromWeek() = run { recipe?.let { shopping.removeFromPlan(it.id) } }

    private fun run(action: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                action()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // The screen reflects the actual plan state; if it fails, the button stays the same.
            }
        }
    }

    companion object {
        const val ARG_ID = "recipeId"
        const val MIN_SERVINGS = ShoppingManager.MIN_SERVINGS
        const val MAX_SERVINGS = ShoppingManager.MAX_SERVINGS
        private const val KEY_SERVINGS = "servings"
    }
}
