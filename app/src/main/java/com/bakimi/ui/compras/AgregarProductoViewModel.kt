package com.bakimi.ui.compras

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bakimi.domain.compras.ArticuloManual
import com.bakimi.domain.compras.GestorCompras
import com.bakimi.domain.ingredientes.Ingrediente
import com.bakimi.domain.ingredientes.IngredienteRepository
import com.bakimi.domain.ingredientes.buscarIngredientes
import com.bakimi.domain.ingredientes.buscarPorNombre
import com.bakimi.domain.inventario.Unidad
import com.bakimi.domain.inventario.parsearCantidad
import com.bakimi.ui.refri.Seleccion
import com.bakimi.ui.refri.textoEditable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AgregarProductoUiState(
    val seleccion: Seleccion? = null,
    val busqueda: String = "",
    val resultados: List<Ingrediente> = emptyList(),
    val ofrecerOtro: Boolean = false,
    val cantidadTexto: String = "1",
    val unidad: Unidad = Unidad.PIEZA,
    val precioTexto: String = "",
    val errorNombre: Boolean = false,
    val errorCantidad: Boolean = false,
    val errorPrecio: Boolean = false,
    val errorAlGuardar: Boolean = false,
    val guardando: Boolean = false,
    val guardado: Boolean = false,
)

@HiltViewModel
class AgregarProductoViewModel @Inject constructor(
    private val gestor: GestorCompras,
    ingredientes: IngredienteRepository,
) : ViewModel() {

    private val catalogo = ingredientes.todos()

    private val _estado = MutableStateFlow(AgregarProductoUiState())
    val estado: StateFlow<AgregarProductoUiState> = _estado.asStateFlow()

    fun cambiarBusqueda(texto: String) = _estado.update {
        it.copy(
            busqueda = texto,
            resultados = buscarIngredientes(texto, catalogo),
            ofrecerOtro = texto.isNotBlank() && catalogo.buscarPorNombre(texto) == null,
            errorNombre = false,
        )
    }

    /** Propone la presentación habitual de compra (un litro de leche, una bolsa de arroz). */
    fun elegir(ingrediente: Ingrediente) = _estado.update {
        val compra = ingrediente.compra
        it.copy(
            seleccion = Seleccion.DelCatalogo(ingrediente),
            unidad = compra?.unidad ?: ingrediente.unidadSugerida,
            cantidadTexto = textoEditable(compra?.presentacion ?: 1.0),
            busqueda = "",
            resultados = emptyList(),
            ofrecerOtro = false,
        )
    }

    fun elegirOtro() = _estado.update {
        val nombre = it.busqueda.trim()
        if (nombre.isEmpty()) it else it.copy(seleccion = Seleccion.Otro(nombre), resultados = emptyList(), ofrecerOtro = false)
    }

    fun cambiarSeleccion() {
        val nombre = _estado.value.seleccion?.nombre.orEmpty()
        _estado.update { it.copy(seleccion = null) }
        cambiarBusqueda(nombre)
    }

    fun cambiarCantidad(texto: String) = _estado.update { it.copy(cantidadTexto = texto, errorCantidad = false) }

    fun cambiarUnidad(unidad: Unidad) = _estado.update { it.copy(unidad = unidad) }

    fun cambiarPrecio(texto: String) = _estado.update { it.copy(precioTexto = texto, errorPrecio = false) }

    fun guardar() {
        val actual = _estado.value
        if (actual.guardando) return
        val seleccion = actual.seleccion
        val cantidad = parsearCantidad(actual.cantidadTexto)?.takeIf { it > 0.0 }
        val precio = actual.precioTexto.takeIf { it.isNotBlank() }?.let(::parsearCantidad)
        val errorPrecio = actual.precioTexto.isNotBlank() && precio == null
        if (seleccion == null || cantidad == null || errorPrecio) {
            _estado.update {
                it.copy(errorNombre = seleccion == null, errorCantidad = cantidad == null, errorPrecio = errorPrecio)
            }
            return
        }

        _estado.update { it.copy(guardando = true, errorAlGuardar = false) }
        viewModelScope.launch {
            try {
                gestor.agregarManual(
                    ArticuloManual(
                        ingredienteId = (seleccion as? Seleccion.DelCatalogo)?.ingrediente?.id,
                        nombre = seleccion.nombre,
                        cantidad = cantidad,
                        unidad = actual.unidad,
                        precio = precio,
                    ),
                )
                _estado.update { it.copy(guardando = false, guardado = true) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _estado.update { it.copy(guardando = false, errorAlGuardar = true) }
            }
        }
    }
}
