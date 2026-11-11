package com.bakimi.ui.fridge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bakimi.domain.inventory.InventoryManager
import com.bakimi.domain.inventory.InventoryItem
import com.bakimi.domain.inventory.StorageLocation
import com.bakimi.domain.inventory.groupByLocation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface FridgeUiState {
    data object Loading : FridgeUiState
    data class Ready(val groups: Map<StorageLocation, List<InventoryItem>>) : FridgeUiState
}

sealed interface FridgeEvent {
    data object OperationError : FridgeEvent
    data class Deleted(val item: InventoryItem) : FridgeEvent
}

@HiltViewModel
class FridgeViewModel @Inject constructor(
    private val manager: InventoryManager,
) : ViewModel() {

    val state: StateFlow<FridgeUiState> = manager.observe()
        .map { FridgeUiState.Ready(groupByLocation(it)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FridgeUiState.Loading)

    private val _events = Channel<FridgeEvent>(Channel.BUFFERED)
    val events: Flow<FridgeEvent> = _events.receiveAsFlow()

    fun increase(id: Long) = run { manager.adjust(id, steps = 1) }

    fun decrease(id: Long) = run { manager.adjust(id, steps = -1) }

    fun markDepleted(id: Long) = run { manager.markDepleted(id) }

    fun delete(item: InventoryItem) = run {
        manager.delete(item.id)
        _events.send(FridgeEvent.Deleted(item))
    }

    /** Restores the food exactly as it was, with the same id. */
    fun undoDelete(item: InventoryItem) = run { manager.save(item) }

    private fun run(action: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                action()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _events.send(FridgeEvent.OperationError)
            }
        }
    }
}
