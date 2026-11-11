package com.bakimi.ui.shopping

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bakimi.domain.shopping.ShoppingManager
import com.bakimi.domain.shopping.MANUAL_PREFIX
import com.bakimi.domain.shopping.ShoppingLine
import com.bakimi.domain.shopping.Week
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ShoppingEvent {
    data class Added(val quantity: Int) : ShoppingEvent
    data object Error : ShoppingEvent
}

@HiltViewModel
class ShoppingViewModel @Inject constructor(
    private val manager: ShoppingManager,
) : ViewModel() {

    /** `null` while loading. */
    val state: StateFlow<Week?> = manager.observeWeek()
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Avoids confirming twice while saving. */
    private val _finishing = MutableStateFlow(false)
    val finishing: StateFlow<Boolean> = _finishing.asStateFlow()

    private val _events = Channel<ShoppingEvent>(Channel.BUFFERED)
    val events: Flow<ShoppingEvent> = _events.receiveAsFlow()

    fun mark(line: ShoppingLine, purchased: Boolean) = run { manager.markPurchased(line.key, purchased) }

    fun correct(line: ShoppingLine, quantity: Double?, price: Double?) =
        run { manager.correct(line.key, quantity, price) }

    fun delete(line: ShoppingLine) = run {
        if (line.manual) manager.deleteManual(line.key.removePrefix(MANUAL_PREFIX).toLong())
    }

    fun finishShopping() {
        if (_finishing.value) return
        _finishing.value = true
        viewModelScope.launch {
            try {
                _events.send(ShoppingEvent.Added(manager.finishShopping()))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _events.send(ShoppingEvent.Error)
            } finally {
                _finishing.value = false
            }
        }
    }

    private fun run(action: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                action()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _events.send(ShoppingEvent.Error)
            }
        }
    }
}
