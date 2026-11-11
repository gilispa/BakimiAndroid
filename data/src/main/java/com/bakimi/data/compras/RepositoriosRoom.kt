package com.bakimi.data.compras

import androidx.room.withTransaction
import com.bakimi.data.BakimiDatabase
import com.bakimi.data.inventario.aEntidad
import com.bakimi.domain.compras.ArticuloManual
import com.bakimi.domain.compras.ComprasRepository
import com.bakimi.domain.compras.ConfigPlan
import com.bakimi.domain.compras.EntradaPlan
import com.bakimi.domain.compras.MarcaCompra
import com.bakimi.domain.compras.PlanRepository
import com.bakimi.domain.inventario.ItemInventario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PlanRepositoryRoom @Inject constructor(private val dao: PlanDao) : PlanRepository {

    override fun observarConfig(): Flow<ConfigPlan> = dao.observarConfig().map { it?.aDominio() ?: ConfigPlan() }

    override fun observarRecetas(): Flow<List<EntradaPlan>> =
        dao.observarRecetas().map { filas -> filas.map { it.aDominio() } }

    override suspend fun guardarConfig(config: ConfigPlan) = dao.guardarConfig(config.aEntidad())

    override suspend fun guardarReceta(entrada: EntradaPlan) {
        // Conserva el orden original si la receta ya estaba en el plan.
        val agregadaEn = dao.obtenerReceta(entrada.recetaId)?.agregadaEn ?: System.currentTimeMillis()
        dao.guardarReceta(PlanRecetaEntity(entrada.recetaId, entrada.porciones, agregadaEn))
    }

    override suspend fun quitarReceta(recetaId: String) = dao.quitarReceta(recetaId)
}

class ComprasRepositoryRoom @Inject constructor(
    private val db: BakimiDatabase,
    private val dao: ComprasDao,
) : ComprasRepository {

    override fun observarMarcas(): Flow<List<MarcaCompra>> =
        dao.observarMarcas().map { filas -> filas.map { it.aDominio() } }

    override fun observarManuales(): Flow<List<ArticuloManual>> =
        dao.observarManuales().map { filas -> filas.map { it.aDominio() } }

    override suspend fun obtenerMarca(ingredienteId: String): MarcaCompra? = dao.obtenerMarca(ingredienteId)?.aDominio()

    override suspend fun obtenerManual(id: Long): ArticuloManual? = dao.obtenerManual(id)?.aDominio()

    override suspend fun guardarMarca(marca: MarcaCompra) = dao.guardarMarca(marca.aEntidad())

    override suspend fun guardarManual(articulo: ArticuloManual): Long {
        val id = dao.guardarManual(articulo.aEntidad())
        return if (articulo.id == 0L) id else articulo.id
    }

    override suspend fun eliminarManual(id: Long) = dao.eliminarManual(id)

    override suspend fun registrarCompra(
        inventario: List<ItemInventario>,
        marcasProcesadas: List<String>,
        manualesProcesados: List<Long>,
    ) {
        db.withTransaction {
            val inventarioDao = db.inventarioDao()
            inventario.forEach { inventarioDao.guardar(it.aEntidad()) }
            if (marcasProcesadas.isNotEmpty()) dao.eliminarMarcas(marcasProcesadas)
            if (manualesProcesados.isNotEmpty()) dao.eliminarManuales(manualesProcesados)
        }
    }
}
