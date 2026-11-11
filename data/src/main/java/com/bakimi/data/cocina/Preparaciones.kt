package com.bakimi.data.cocina

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.withTransaction
import com.bakimi.data.BakimiDatabase
import com.bakimi.data.inventario.aEntidad
import com.bakimi.domain.cocina.Preparacion
import com.bakimi.domain.cocina.PreparacionRepository
import com.bakimi.domain.inventario.ItemInventario
import javax.inject.Inject

/** Historial de preparaciones confirmadas. El id evita registrar la misma preparación dos veces. */
@Entity(tableName = "preparacion")
data class PreparacionEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "receta_id") val recetaId: String,
    val porciones: Int,
    @ColumnInfo(name = "fecha_millis") val fechaMillis: Long,
)

@Dao
interface PreparacionDao {
    /** Devuelve -1 si ya existía una preparación con ese id. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertar(preparacion: PreparacionEntity): Long
}

class PreparacionRepositoryRoom @Inject constructor(
    private val db: BakimiDatabase,
) : PreparacionRepository {

    override suspend fun registrar(preparacion: Preparacion, cambios: List<ItemInventario>): Boolean =
        db.withTransaction {
            val insertada = db.preparacionDao().insertar(
                PreparacionEntity(preparacion.id, preparacion.recetaId, preparacion.porciones, preparacion.fechaMillis),
            )
            if (insertada == -1L) return@withTransaction false
            val inventarioDao = db.inventarioDao()
            cambios.forEach { inventarioDao.guardar(it.aEntidad()) }
            true
        }
}
