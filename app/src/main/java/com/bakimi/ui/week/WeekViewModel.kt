package com.bakimi.ui.week

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bakimi.domain.shopping.ShoppingManager
import com.bakimi.domain.shopping.Week
import com.bakimi.domain.inventory.parseQuantity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeekViewModel @Inject constructor(
    private val manager: ShoppingManager,
) : ViewModel() {

    /** `null` while loading. */
    val state: StateFlow<Week?> = manager.observeWeek()
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _errors = Channel<Unit>(Channel.BUFFERED)
    val errors: Flow<Unit> = _errors.receiveAsFlow()

    fun changePeople(delta: Int) = run {
        state.value?.config?.let { manager.saveSettings(it.copy(people = it.people + delta)) }
    }

    /** Empty text removes the budget; invalid text is ignored until it is a number. */
    fun changeBudget(text: String) = run {
        val config = state.value?.config ?: return@run
        if (text.isBlank()) {
            manager.saveSettings(config.copy(budget = null))
        } else {
            parseQuantity(text)?.let { manager.saveSettings(config.copy(budget = it)) }
        }
    }

    fun add(recipeId: String) = run { manager.addToPlan(recipeId) }

    fun remove(recipeId: String) = run { manager.removeFromPlan(recipeId) }

    fun changeServings(recipeId: String, servings: Int) = run { manager.changeServings(recipeId, servings) }

    private fun run(action: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                action()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _errors.send(Unit)
            }
        }
    }
}
