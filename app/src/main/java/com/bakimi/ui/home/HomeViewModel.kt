package com.bakimi.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bakimi.domain.inventory.InventoryManager
import com.bakimi.domain.recipes.RecipeRepository
import com.bakimi.domain.recipes.HomeSections
import com.bakimi.domain.recipes.buildHome
import com.bakimi.domain.recipes.evaluateRecipe
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface HomeUiState {
    data object Loading : HomeUiState

    data class Ready(
        val sections: HomeSections,
        /** Mi refri is empty: invite the user to add foods. */
        val fridgeEmpty: Boolean,
    ) : HomeUiState
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    recipes: RecipeRepository,
    manager: InventoryManager,
) : ViewModel() {

    /** Recalculated every time the inventory changes. */
    val state: StateFlow<HomeUiState> = manager.observe()
        .map { inventory ->
            val evaluations = recipes.all().map { evaluateRecipe(it, it.baseServings, inventory) }
            HomeUiState.Ready(buildHome(evaluations), fridgeEmpty = inventory.isEmpty())
        }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState.Loading)
}
