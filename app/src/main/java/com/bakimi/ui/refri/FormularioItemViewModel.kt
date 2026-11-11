package com.bakimi.ui.refri

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bakimi.domain.ingredientes.Ingrediente
import com.bakimi.domain.ingredientes.IngredienteRepository
import com.bakimi.domain.ingredientes.buscarIngredientes
import com.bakimi.domain.ingredientes.buscarPorNombre
import com.bakimi.domain.inventario.ErrorItem
import com.bakimi.domain.inventario.GestorInventario
import com.bakimi.domain.inventario.ItemInventario
import com.bakimi.domain.inventario.ResultadoGuardado
import com.bakimi.domain.inventario.Ubicacion
import com.bakimi.domain.inventario.Unidad
import com.bakimi.domain.inventario.parsearCantidad
import com.bakimi.domain.inventario.validar
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/** Alimento elegido en el buscador. */
sealed interface Seleccion {
    val nombre: String

    data class DelCatalogo(val ingrediente: Ingrediente) : Seleccion {
        override val nombre: String get() = ingrediente.nombre
    }

    /** Alimento que no está en el catálogo; se guarda con el nombre que escribió el usuario. */
    data class Otro(override val nombre: String) : Seleccion
}

data class FormularioUiState(
    val esEdicion: Boolean = false,
    val cargando: Boolean = false,
    val seleccion: Seleccion? = null,
    val busqueda: String = "",
    val resultados: List<Ingrediente> = emptyList(),
    /** La búsqueda no coincide exactamente con nada del catálogo: se ofrece agregarla como "otro". */
    val ofrecerOtro: Boolean = false,
    val cantidadTexto: String = "1",
    val unidad: Unidad = Unidad.PIEZA,
    val ubicacion: Ubicacion = Ubicacion.REFRIGERADOR,
    val caducidad: LocalDate? = null,
    val errores: Set<ErrorItem> = emptySet(),
    val errorAlGuardar: Boolean = false,
    val guardando: Boolean = false,
    val guardado: Boolean = false,
)

@HiltViewModel
class FormularioItemViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val gestor: GestorInventario,
    ingredientes: IngredienteRepository,
) : ViewModel() {

    private val id: Long = savedStateHandle.get<Long>(ARG_ID) ?: 0L
    private val catalogo: List<Ingrediente> = ingredientes.todos()

    private val _estado = MutableStateFlow(FormularioUiState(esEdicion = id != 0L, cargando = id != 0L))
    val estado: StateFlow<FormularioUiState> = _estado.asStateFlow()

    init {
        if (id != 0L) {
            viewModelScope.launch {
                val item = gestor.obtener(id)
                _estado.update { actual ->
                    if (item == null) {
                        // Ya no existe (por ejemplo, se eliminó): se trata como alimento nuevo.
                        actual.copy(esEdicion = false, cargando = false)
                    } else {
                        actual.copy(
                            cargando = false,
                            seleccion = seleccionDe(item),
                            cantidadTexto = textoEditable(item.cantidad),
                            unidad = item.unidad,
                            ubicacion = item.ubicacion,
                            caducidad = item.caducidad,
                        )
                    }
                }
            }
        }
    }

    /**
     * Los registros anteriores al catálogo no tienen vínculo: si su nombre corresponde a un
     * ingrediente, se vinculan al guardar.
     */
    private fun seleccionDe(item: ItemInventario): Seleccion {
        val ingrediente = item.ingredienteId?.let { id -> catalogo.firstOrNull { it.id == id } }
            ?: catalogo.buscarPorNombre(item.nombre)
        return ingrediente?.let { Seleccion.DelCatalogo(it) } ?: Seleccion.Otro(item.nombre)
    }

    fun cambiarBusqueda(texto: String) = _estado.update {
        it.copy(
            busqueda = texto,
            resultados = buscarIngredientes(texto, catalogo),
            ofrecerOtro = texto.isNotBlank() && catalogo.buscarPorNombre(texto) == null,
            errores = it.errores - ErrorItem.NOMBRE_VACIO,
        )
    }

    /** Elegir del catálogo también propone su unidad y ubicación habituales. */
    fun elegir(ingrediente: Ingrediente) = _estado.update {
        it.copy(
            seleccion = Seleccion.DelCatalogo(ingrediente),
            unidad = ingrediente.unidadSugerida,
            ubicacion = ingrediente.ubicacionSugerida,
            busqueda = "",
            resultados = emptyList(),
            ofrecerOtro = false,
        )
    }

    fun elegirOtro() = _estado.update {
        val nombre = it.busqueda.trim()
        if (nombre.isEmpty()) it else it.copy(seleccion = Seleccion.Otro(nombre), resultados = emptyList(), ofrecerOtro = false)
    }

    /** Vuelve al buscador con el nombre actual escrito, para corregirlo rápido. */
    fun cambiarSeleccion() {
        val nombre = _estado.value.seleccion?.nombre.orEmpty()
        _estado.update { it.copy(seleccion = null) }
        cambiarBusqueda(nombre)
    }

    fun cambiarCantidad(valor: String) =
        _estado.update { it.copy(cantidadTexto = valor, errores = it.errores - ErrorItem.CANTIDAD_INVALIDA) }

    fun cambiarUnidad(valor: Unidad) = _estado.update { it.copy(unidad = valor) }

    fun cambiarUbicacion(valor: Ubicacion) = _estado.update { it.copy(ubicacion = valor) }

    fun cambiarCaducidad(valor: LocalDate?) = _estado.update { it.copy(caducidad = valor) }

    fun guardar() {
        val actual = _estado.value
        if (actual.guardando || actual.cargando) return

        val cantidad = parsearCantidad(actual.cantidadTexto)
        val item = ItemInventario(
            id = if (actual.esEdicion) id else 0L,
            nombre = actual.seleccion?.nombre.orEmpty(),
            cantidad = cantidad ?: 0.0,
            unidad = actual.unidad,
            ubicacion = actual.ubicacion,
            caducidad = actual.caducidad,
            ingredienteId = (actual.seleccion as? Seleccion.DelCatalogo)?.ingrediente?.id,
        )
        val errores = validar(item) + if (cantidad == null) setOf(ErrorItem.CANTIDAD_INVALIDA) else emptySet()
        if (errores.isNotEmpty()) {
            _estado.update { it.copy(errores = errores) }
            return
        }

        _estado.update { it.copy(guardando = true, errorAlGuardar = false) }
        viewModelScope.launch {
            try {
                when (val resultado = gestor.guardar(item)) {
                    is ResultadoGuardado.Guardado ->
                        _estado.update { it.copy(guardando = false, guardado = true) }

                    is ResultadoGuardado.Invalido ->
                        _estado.update { it.copy(guardando = false, errores = resultado.errores) }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _estado.update { it.copy(guardando = false, errorAlGuardar = true) }
            }
        }
    }

    companion object {
        const val ARG_ID = "id"
    }
}
