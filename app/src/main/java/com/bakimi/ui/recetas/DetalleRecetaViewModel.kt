package com.bakimi.ui.recetas

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bakimi.domain.compras.GestorCompras
import com.bakimi.domain.inventario.GestorInventario
import com.bakimi.domain.recetas.EvaluacionReceta
import com.bakimi.domain.recetas.RecetaRepository
import com.bakimi.domain.recetas.evaluarReceta
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface DetalleUiState {
    data object Cargando : DetalleUiState
    data object NoEncontrada : DetalleUiState

    /** @property porcionesEnSemana porciones guardadas en Mi semana; `null` si no está en el plan. */
    data class Listo(val evaluacion: EvaluacionReceta, val porcionesEnSemana: Int?) : DetalleUiState
}

@HiltViewModel
class DetalleRecetaViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    recetas: RecetaRepository,
    inventario: GestorInventario,
    private val compras: GestorCompras,
) : ViewModel() {

    private val receta = savedStateHandle.get<String>(ARG_ID)?.let(recetas::obtener)

    /** Se guarda en el SavedStateHandle para conservarla si Android cierra el proceso. */
    private val porciones: StateFlow<Int> =
        savedStateHandle.getStateFlow(CLAVE_PORCIONES, receta?.porcionesBase ?: 1)

    val estado: StateFlow<DetalleUiState> = if (receta == null) {
        flowOf<DetalleUiState>(DetalleUiState.NoEncontrada)
            .stateIn(viewModelScope, SharingStarted.Eagerly, DetalleUiState.NoEncontrada)
    } else {
        combine(inventario.observar(), porciones, compras.observarSemana()) { existencias, porciones, semana ->
            DetalleUiState.Listo(
                evaluacion = evaluarReceta(receta, porciones, existencias),
                porcionesEnSemana = semana.recetas.find { it.receta.id == receta.id }?.porciones,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DetalleUiState.Cargando)
    }

    fun cambiarPorciones(delta: Int) {
        savedStateHandle[CLAVE_PORCIONES] = (porciones.value + delta).coerceIn(MIN_PORCIONES, MAX_PORCIONES)
    }

    /** Agrega la receta a Mi semana con las porciones elegidas, o actualiza las que tenía. */
    fun guardarEnSemana() = ejecutar { receta?.let { compras.agregarAlPlan(it.id, porciones.value) } }

    fun quitarDeSemana() = ejecutar { receta?.let { compras.quitarDelPlan(it.id) } }

    private fun ejecutar(accion: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                accion()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // La pantalla refleja el estado real del plan; si falla, el botón sigue igual.
            }
        }
    }

    companion object {
        const val ARG_ID = "recetaId"
        const val MIN_PORCIONES = GestorCompras.MIN_PORCIONES
        const val MAX_PORCIONES = GestorCompras.MAX_PORCIONES
        private const val CLAVE_PORCIONES = "porciones"
    }
}
