package com.bakimi.data.compras

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface PlanDao {
    @Query("SELECT * FROM plan_config WHERE id = ${PlanConfigEntity.ID_UNICO}")
    fun observarConfig(): Flow<PlanConfigEntity?>

    @Upsert
    suspend fun guardarConfig(config: PlanConfigEntity)

    @Query("SELECT * FROM plan_receta ORDER BY agregada_en")
    fun observarRecetas(): Flow<List<PlanRecetaEntity>>

    @Query("SELECT * FROM plan_receta WHERE receta_id = :recetaId")
    suspend fun obtenerReceta(recetaId: String): PlanRecetaEntity?

    @Upsert
    suspend fun guardarReceta(receta: PlanRecetaEntity)

    @Query("DELETE FROM plan_receta WHERE receta_id = :recetaId")
    suspend fun quitarReceta(recetaId: String)
}

@Dao
interface ComprasDao {
    @Query("SELECT * FROM compra_marca")
    fun observarMarcas(): Flow<List<MarcaCompraEntity>>

    @Query("SELECT * FROM compra_manual ORDER BY id")
    fun observarManuales(): Flow<List<ArticuloManualEntity>>

    @Query("SELECT * FROM compra_marca WHERE ingrediente_id = :ingredienteId")
    suspend fun obtenerMarca(ingredienteId: String): MarcaCompraEntity?

    @Query("SELECT * FROM compra_manual WHERE id = :id")
    suspend fun obtenerManual(id: Long): ArticuloManualEntity?

    @Upsert
    suspend fun guardarMarca(marca: MarcaCompraEntity)

    /** Devuelve el id nuevo al insertar, o -1 al actualizar. */
    @Upsert
    suspend fun guardarManual(articulo: ArticuloManualEntity): Long

    @Query("DELETE FROM compra_manual WHERE id = :id")
    suspend fun eliminarManual(id: Long)

    @Query("DELETE FROM compra_marca WHERE ingrediente_id IN (:ids)")
    suspend fun eliminarMarcas(ids: List<String>)

    @Query("DELETE FROM compra_manual WHERE id IN (:ids)")
    suspend fun eliminarManuales(ids: List<Long>)
}
