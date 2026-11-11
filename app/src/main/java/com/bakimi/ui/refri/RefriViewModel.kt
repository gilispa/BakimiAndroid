package com.bakimi.ui.refri

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bakimi.domain.inventario.GestorInventario
import com.bakimi.domain.inventario.ItemInventario
import com.bakimi.domain.inventario.Ubicacion
import com.bakimi.domain.inventario.agruparPorUbicacion
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

sealed interface RefriUiState {
    data object Cargando : RefriUiState
    data class Listo(val grupos: Map<Ubicacion, List<ItemInventario>>) : RefriUiState
}

sealed interface EventoRefri {
    data object ErrorOperacion : EventoRefri
    data class Eliminado(val item: ItemInventario) : EventoRefri
}

@HiltViewModel
class RefriViewModel @Inject constructor(
    private val gestor: GestorInventario,
) : ViewModel() {

    val estado: StateFlow<RefriUiState> = gestor.observar()
        .map { RefriUiState.Listo(agruparPorUbicacion(it)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RefriUiState.Cargando)

    private val _eventos = Channel<EventoRefri>(Channel.BUFFERED)
    val eventos: Flow<EventoRefri> = _eventos.receiveAsFlow()

    fun sumar(id: Long) = ejecutar { gestor.ajustar(id, pasos = 1) }

    fun restar(id: Long) = ejecutar { gestor.ajustar(id, pasos = -1) }

    fun marcarAgotado(id: Long) = ejecutar { gestor.marcarAgotado(id) }

    fun eliminar(item: ItemInventario) = ejecutar {
        gestor.eliminar(item.id)
        _eventos.send(EventoRefri.Eliminado(item))
    }

    /** Restaura el alimento tal como estaba, con su mismo id. */
    fun deshacerEliminacion(item: ItemInventario) = ejecutar { gestor.guardar(item) }

    private fun ejecutar(accion: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                accion()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _eventos.send(EventoRefri.ErrorOperacion)
            }
        }
    }
}
