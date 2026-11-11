package com.bakimi.ui.semana

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bakimi.domain.compras.GestorCompras
import com.bakimi.domain.compras.Semana
import com.bakimi.domain.inventario.parsearCantidad
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
class SemanaViewModel @Inject constructor(
    private val gestor: GestorCompras,
) : ViewModel() {

    /** `null` mientras carga. */
    val estado: StateFlow<Semana?> = gestor.observarSemana()
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _errores = Channel<Unit>(Channel.BUFFERED)
    val errores: Flow<Unit> = _errores.receiveAsFlow()

    fun cambiarPersonas(delta: Int) = ejecutar {
        estado.value?.config?.let { gestor.guardarConfig(it.copy(personas = it.personas + delta)) }
    }

    /** Texto vacío quita el presupuesto; un texto inválido se ignora hasta que sea un número. */
    fun cambiarPresupuesto(texto: String) = ejecutar {
        val config = estado.value?.config ?: return@ejecutar
        if (texto.isBlank()) {
            gestor.guardarConfig(config.copy(presupuesto = null))
        } else {
            parsearCantidad(texto)?.let { gestor.guardarConfig(config.copy(presupuesto = it)) }
        }
    }

    fun agregar(recetaId: String) = ejecutar { gestor.agregarAlPlan(recetaId) }

    fun quitar(recetaId: String) = ejecutar { gestor.quitarDelPlan(recetaId) }

    fun cambiarPorciones(recetaId: String, porciones: Int) = ejecutar { gestor.cambiarPorciones(recetaId, porciones) }

    private fun ejecutar(accion: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                accion()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _errores.send(Unit)
            }
        }
    }
}
