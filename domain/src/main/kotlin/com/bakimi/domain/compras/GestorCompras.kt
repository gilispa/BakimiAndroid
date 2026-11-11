package com.bakimi.domain.compras

import com.bakimi.domain.ingredientes.IngredienteRepository
import com.bakimi.domain.inventario.InventarioRepository
import com.bakimi.domain.recetas.EvaluacionReceta
import com.bakimi.domain.recetas.RecetaRepository
import com.bakimi.domain.recetas.evaluarReceta
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Receta fuera del plan, con lo que costaría comprar lo que le falta con el inventario actual. */
data class SugerenciaReceta(val evaluacion: EvaluacionReceta, val compraFaltante: TotalCompra)

/** Todo lo que muestran Mi semana y Compras. */
data class Semana(
    val config: ConfigPlan,
    val recetas: List<RecetaPlaneada>,
    val lista: ListaCompras,
    val presupuesto: EstadoPresupuesto,
    val recomendaciones: List<Recomendacion>,
    val sugerencias: List<SugerenciaReceta>,
)

/** Plan semanal y lista de compras. Una sola instancia para que su cerrojo cubra toda la app. */
class GestorCompras(
    private val plan: PlanRepository,
    private val compras: ComprasRepository,
    private val recetas: RecetaRepository,
    private val ingredientes: IngredienteRepository,
    private val inventario: InventarioRepository,
) {
    private val cerrojo = Mutex()

    fun observarSemana(): Flow<Semana> = combine(
        plan.observarConfig(),
        plan.observarRecetas(),
        inventario.observarTodos(),
        compras.observarMarcas(),
        compras.observarManuales(),
    ) { config, entradas, existencias, marcas, manuales ->
        val planeadas = entradas.mapNotNull { entrada ->
            recetas.obtener(entrada.recetaId)?.let { RecetaPlaneada(it, entrada.porciones) }
        }
        val lista = armarLista(listaDeCompra(planeadas, existencias), marcas, manuales, ingredientes::obtener)
        val enPlan = planeadas.map { it.receta.id }.toSet()
        val sugerencias = recetas.todas()
            .filter { it.id !in enPlan }
            .map { receta ->
                val porciones = config.personas
                SugerenciaReceta(
                    evaluacion = evaluarReceta(receta, porciones, existencias),
                    compraFaltante = totalDe(
                        listaDeCompra(listOf(RecetaPlaneada(receta, porciones)), existencias).map { it.precio },
                    ),
                )
            }
            .sortedWith(
                compareBy<SugerenciaReceta>(
                    { it.evaluacion.faltantes },
                    { -it.evaluacion.completos },
                    { it.evaluacion.receta.minutos },
                ),
            )
        Semana(
            config = config,
            recetas = planeadas,
            lista = lista,
            presupuesto = estadoPresupuesto(lista.total, config.presupuesto),
            recomendaciones = recomendarComplementarias(planeadas, existencias, recetas.todas()),
            sugerencias = sugerencias,
        )
    }

    suspend fun guardarConfig(config: ConfigPlan) {
        plan.guardarConfig(config.copy(personas = config.personas.coerceIn(MIN_PORCIONES, MAX_PORCIONES)))
    }

    /** Agrega una receta al plan; sin porciones indicadas usa el número de personas. */
    suspend fun agregarAlPlan(recetaId: String, porciones: Int? = null) {
        val cantidad = porciones ?: plan.observarConfig().first().personas
        plan.guardarReceta(EntradaPlan(recetaId, cantidad.coerceIn(MIN_PORCIONES, MAX_PORCIONES)))
    }

    suspend fun quitarDelPlan(recetaId: String) = plan.quitarReceta(recetaId)

    suspend fun cambiarPorciones(recetaId: String, porciones: Int) =
        plan.guardarReceta(EntradaPlan(recetaId, porciones.coerceIn(MIN_PORCIONES, MAX_PORCIONES)))

    /** Marcar no cambia el inventario; solo [terminarCompra] lo hace. */
    suspend fun marcarComprado(clave: String, comprado: Boolean) = cerrojo.withLock {
        when {
            clave.startsWith(PREFIJO_PLAN) -> {
                val id = clave.removePrefix(PREFIJO_PLAN)
                compras.guardarMarca((compras.obtenerMarca(id) ?: MarcaCompra(id)).copy(comprado = comprado))
            }
            clave.startsWith(PREFIJO_MANUAL) -> {
                compras.obtenerManual(clave.removePrefix(PREFIJO_MANUAL).toLong())
                    ?.let { compras.guardarManual(it.copy(comprado = comprado)) }
            }
        }
    }

    /** Corrige la cantidad real o el precio real de un renglón; `null` deja el estimado. */
    suspend fun corregir(clave: String, cantidad: Double?, precio: Double?) = cerrojo.withLock {
        when {
            clave.startsWith(PREFIJO_PLAN) -> {
                val id = clave.removePrefix(PREFIJO_PLAN)
                val marca = compras.obtenerMarca(id) ?: MarcaCompra(id)
                compras.guardarMarca(marca.copy(cantidadReal = cantidad, precioReal = precio))
            }
            clave.startsWith(PREFIJO_MANUAL) -> {
                compras.obtenerManual(clave.removePrefix(PREFIJO_MANUAL).toLong())?.let {
                    compras.guardarManual(it.copy(cantidad = cantidad ?: it.cantidad, precio = precio))
                }
            }
        }
    }

    suspend fun agregarManual(articulo: ArticuloManual) {
        compras.guardarManual(articulo.copy(id = 0, nombre = articulo.nombre.trim()))
    }

    suspend fun eliminarManual(id: Long) = compras.eliminarManual(id)

    /**
     * Incorpora a Mi refri solo los productos marcados, con su cantidad real, y los quita de la
     * lista. Repetirla no duplica nada: lo ya incorporado deja de estar marcado.
     *
     * @return número de productos incorporados.
     */
    suspend fun terminarCompra(): Int = cerrojo.withLock {
        val lista = observarSemana().first().lista
        val marcados = lista.marcados
        if (marcados.isEmpty()) return@withLock 0

        val cambios = incorporarCompra(inventario.observarTodos().first(), lista.comprados())
        compras.registrarCompra(
            inventario = cambios,
            marcasProcesadas = marcados.filter { !it.manual }.map { it.clave.removePrefix(PREFIJO_PLAN) },
            manualesProcesados = marcados.filter { it.manual }.map { it.clave.removePrefix(PREFIJO_MANUAL).toLong() },
        )
        marcados.size
    }

    companion object {
        const val MIN_PORCIONES = 1
        const val MAX_PORCIONES = 12
    }
}
