package com.bakimi.ui.compras

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bakimi.domain.compras.GestorCompras
import com.bakimi.domain.compras.PREFIJO_MANUAL
import com.bakimi.domain.compras.RenglonCompra
import com.bakimi.domain.compras.Semana
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

sealed interface EventoCompras {
    data class Incorporados(val cantidad: Int) : EventoCompras
    data object Error : EventoCompras
}

@HiltViewModel
class ComprasViewModel @Inject constructor(
    private val gestor: GestorCompras,
) : ViewModel() {

    /** `null` mientras carga. */
    val estado: StateFlow<Semana?> = gestor.observarSemana()
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Evita confirmar dos veces mientras se guarda. */
    private val _terminando = MutableStateFlow(false)
    val terminando: StateFlow<Boolean> = _terminando.asStateFlow()

    private val _eventos = Channel<EventoCompras>(Channel.BUFFERED)
    val eventos: Flow<EventoCompras> = _eventos.receiveAsFlow()

    fun marcar(renglon: RenglonCompra, comprado: Boolean) = ejecutar { gestor.marcarComprado(renglon.clave, comprado) }

    fun corregir(renglon: RenglonCompra, cantidad: Double?, precio: Double?) =
        ejecutar { gestor.corregir(renglon.clave, cantidad, precio) }

    fun eliminar(renglon: RenglonCompra) = ejecutar {
        if (renglon.manual) gestor.eliminarManual(renglon.clave.removePrefix(PREFIJO_MANUAL).toLong())
    }

    fun terminarCompra() {
        if (_terminando.value) return
        _terminando.value = true
        viewModelScope.launch {
            try {
                _eventos.send(EventoCompras.Incorporados(gestor.terminarCompra()))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _eventos.send(EventoCompras.Error)
            } finally {
                _terminando.value = false
            }
        }
    }

    private fun ejecutar(accion: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                accion()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _eventos.send(EventoCompras.Error)
            }
        }
    }
}
