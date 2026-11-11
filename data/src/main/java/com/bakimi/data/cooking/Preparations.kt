package com.bakimi.data.cooking

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.withTransaction
import com.bakimi.data.BakimiDatabase
import com.bakimi.data.inventory.toEntity
import com.bakimi.domain.cooking.Preparation
import com.bakimi.domain.cooking.PreparationRepository
import com.bakimi.domain.inventory.InventoryItem
import javax.inject.Inject

/** History of confirmed preparations. The id prevents registering the same preparation twice. */
@Entity(tableName = "preparacion")
data class PreparationEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "receta_id") val recipeId: String,
    @ColumnInfo(name = "porciones") val servings: Int,
    @ColumnInfo(name = "fecha_millis") val dateMillis: Long,
)

@Dao
interface PreparationDao {
    /** Returns -1 if a preparation with that id already existed. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(preparation: PreparationEntity): Long
}

class RoomPreparationRepository @Inject constructor(
    private val db: BakimiDatabase,
) : PreparationRepository {

    override suspend fun register(preparation: Preparation, changes: List<InventoryItem>): Boolean =
        db.withTransaction {
            val inserted = db.preparationDao().insert(
                PreparationEntity(preparation.id, preparation.recipeId, preparation.servings, preparation.dateMillis),
            )
            if (inserted == -1L) return@withTransaction false
            val inventoryDao = db.inventoryDao()
            changes.forEach { inventoryDao.save(it.toEntity()) }
            true
        }
}
