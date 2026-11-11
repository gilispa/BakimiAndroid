package com.bakimi.domain.compras

import com.bakimi.domain.inventario.ItemInventario
import kotlinx.coroutines.flow.Flow

/** Preferencias del plan semanal. */
data class ConfigPlan(
    val personas: Int = 1,
    /** Presupuesto aproximado en MXN para la compra; `null` si no se definió. */
    val presupuesto: Double? = null,
)

/** Receta elegida para la semana, guardada por id. */
data class EntradaPlan(val recetaId: String, val porciones: Int)

interface PlanRepository {
    fun observarConfig(): Flow<ConfigPlan>

    fun observarRecetas(): Flow<List<EntradaPlan>>

    suspend fun guardarConfig(config: ConfigPlan)

    /** Agrega la receta o actualiza sus porciones si ya estaba. */
    suspend fun guardarReceta(entrada: EntradaPlan)

    suspend fun quitarReceta(recetaId: String)
}

interface ComprasRepository {
    fun observarMarcas(): Flow<List<MarcaCompra>>

    fun observarManuales(): Flow<List<ArticuloManual>>

    suspend fun obtenerMarca(ingredienteId: String): MarcaCompra?

    suspend fun obtenerManual(id: Long): ArticuloManual?

    suspend fun guardarMarca(marca: MarcaCompra)

    suspend fun guardarManual(articulo: ArticuloManual): Long

    suspend fun eliminarManual(id: Long)

    /**
     * Incorpora la compra en una sola transacción: guarda los registros de inventario y borra
     * las marcas y los productos manuales procesados. Si algo falla, no cambia nada.
     */
    suspend fun registrarCompra(
        inventario: List<ItemInventario>,
        marcasProcesadas: List<String>,
        manualesProcesados: List<Long>,
    )
}
