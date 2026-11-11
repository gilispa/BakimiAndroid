package com.bakimi.ui.cocina

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bakimi.domain.cocina.Consumo
import com.bakimi.domain.cocina.GestorCocina
import com.bakimi.domain.cocina.IngredienteUsado
import com.bakimi.domain.cocina.ResultadoPreparacion
import com.bakimi.domain.cocina.usadosPorDefecto
import com.bakimi.domain.compras.GestorCompras
import com.bakimi.domain.inventario.GestorInventario
import com.bakimi.domain.inventario.ItemInventario
import com.bakimi.domain.inventario.parsearCantidad
import com.bakimi.domain.recetas.Receta
import com.bakimi.domain.recetas.RecetaRepository
import com.bakimi.ui.refri.textoEditable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

enum class Fase { COCINANDO, CONFIRMANDO, TERMINADO }

/** Lo que el usuario dice haber usado de un ingrediente, como texto editable. */
data class UsoEditable(val usado: IngredienteUsado, val texto: String) {
    val cantidad: Double? get() = parsearCantidad(texto)
}

data class ModoCocinaUiState(
    val receta: Receta,
    val porciones: Int,
    val paso: Int = 0,
    val fase: Fase = Fase.COCINANDO,
    val porcionesPreparadas: Int = porciones,
    val usos: List<UsoEditable> = emptyList(),
    val consumo: Consumo? = null,
    /** Cantidades antes del descuento, para mostrar "12 → 8". */
    val antes: Map<Long, ItemInventario> = emptyMap(),
    val guardando: Boolean = false,
    val yaRegistrada: Boolean = false,
    val error: Boolean = false,
) {
    val totalPasos: Int get() = receta.pasos.size
    val esUltimoPaso: Boolean get() = paso == totalPasos - 1
    val usosValidos: Boolean get() = usos.all { it.cantidad != null }
}

@HiltViewModel
class ModoCocinaViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    recetas: RecetaRepository,
    private val cocina: GestorCocina,
    private val inventario: GestorInventario,
) : ViewModel() {

    private val receta: Receta? = savedStateHandle.get<String>(ARG_RECETA)?.let(recetas::obtener)

    /** Un id por sesión: aunque se confirme dos veces, solo se descuenta una. */
    private val idPreparacion: String =
        savedStateHandle.get<String>(CLAVE_ID) ?: UUID.randomUUID().toString().also { savedStateHandle[CLAVE_ID] = it }

    private val _estado = MutableStateFlow(
        receta?.let { r ->
            val porciones = (savedStateHandle.get<Int>(ARG_PORCIONES) ?: r.porcionesBase)
                .coerceIn(GestorCompras.MIN_PORCIONES, GestorCompras.MAX_PORCIONES)
            // El paso actual se conserva si Android recrea la pantalla.
            ModoCocinaUiState(r, porciones, paso = savedStateHandle.get<Int>(CLAVE_PASO) ?: 0)
        },
    )
    val estado: StateFlow<ModoCocinaUiState?> = _estado.asStateFlow()

    fun siguiente() = moverA((_estado.value?.paso ?: 0) + 1)

    fun anterior() = moverA((_estado.value?.paso ?: 0) - 1)

    private fun moverA(paso: Int) {
        _estado.update { actual ->
            actual?.copy(paso = paso.coerceIn(0, actual.totalPasos - 1))
        }
        savedStateHandle[CLAVE_PASO] = _estado.value?.paso
    }

    /** Pasa a confirmar; todavía no toca el inventario. */
    fun terminarDeCocinar() {
        val actual = _estado.value ?: return
        _estado.update { it?.copy(fase = Fase.CONFIRMANDO) }
        prellenar(actual.porcionesPreparadas)
    }

    fun volverAPasos() = _estado.update { it?.copy(fase = Fase.COCINANDO) }

    /** Cambiar las porciones vuelve a calcular las cantidades usadas desde la receta. */
    fun cambiarPorcionesPreparadas(delta: Int) {
        val actual = _estado.value ?: return
        val porciones = (actual.porcionesPreparadas + delta).coerceIn(GestorCompras.MIN_PORCIONES, GestorCompras.MAX_PORCIONES)
        _estado.update { it?.copy(porcionesPreparadas = porciones) }
        prellenar(porciones)
    }

    fun cambiarUso(indice: Int, texto: String) {
        _estado.update { actual ->
            actual?.copy(usos = actual.usos.mapIndexed { i, uso -> if (i == indice) uso.copy(texto = texto) else uso })
        }
        recalcular()
    }

    private fun prellenar(porciones: Int) {
        val r = receta ?: return
        _estado.update { actual ->
            actual?.copy(usos = usadosPorDefecto(r, porciones).map { UsoEditable(it, textoEditable(it.cantidad)) })
        }
        recalcular()
    }

    private var calculo: Job? = null

    private fun recalcular() {
        val actual = _estado.value ?: return
        if (!actual.usosValidos) return
        // Si el usuario sigue escribiendo, el cálculo anterior ya no sirve.
        calculo?.cancel()
        calculo = viewModelScope.launch {
            val antes = inventario.observar().first().associateBy { it.id }
            val consumo = cocina.calcular(usados(actual))
            _estado.update { it?.copy(consumo = consumo, antes = antes) }
        }
    }

    fun confirmar() {
        val actual = _estado.value ?: return
        if (actual.guardando || !actual.usosValidos) return
        _estado.update { it?.copy(guardando = true, error = false) }
        viewModelScope.launch {
            try {
                val resultado = cocina.confirmar(idPreparacion, actual.receta.id, actual.porcionesPreparadas, usados(actual))
                _estado.update {
                    it?.copy(
                        guardando = false,
                        fase = Fase.TERMINADO,
                        yaRegistrada = resultado == ResultadoPreparacion.YaRegistrada,
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _estado.update { it?.copy(guardando = false, error = true) }
            }
        }
    }

    private fun usados(estado: ModoCocinaUiState): List<IngredienteUsado> =
        estado.usos.map { it.usado.copy(cantidad = it.cantidad ?: 0.0) }

    companion object {
        const val ARG_RECETA = "recetaId"
        const val ARG_PORCIONES = "porciones"
        private const val CLAVE_PASO = "paso"
        private const val CLAVE_ID = "idPreparacion"
    }
}
