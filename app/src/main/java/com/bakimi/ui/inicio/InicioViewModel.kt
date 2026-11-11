package com.bakimi.ui.inicio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bakimi.domain.inventario.GestorInventario
import com.bakimi.domain.recetas.RecetaRepository
import com.bakimi.domain.recetas.SeccionesInicio
import com.bakimi.domain.recetas.armarInicio
import com.bakimi.domain.recetas.evaluarReceta
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface InicioUiState {
    data object Cargando : InicioUiState

    data class Listo(
        val secciones: SeccionesInicio,
        /** Mi refri está vacío: se invita a registrar alimentos. */
        val refriVacio: Boolean,
    ) : InicioUiState
}

@HiltViewModel
class InicioViewModel @Inject constructor(
    recetas: RecetaRepository,
    gestor: GestorInventario,
) : ViewModel() {

    /** Se recalcula cada vez que cambia el inventario. */
    val estado: StateFlow<InicioUiState> = gestor.observar()
        .map { inventario ->
            val evaluaciones = recetas.todas().map { evaluarReceta(it, it.porcionesBase, inventario) }
            InicioUiState.Listo(armarInicio(evaluaciones), refriVacio = inventario.isEmpty())
        }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InicioUiState.Cargando)
}
