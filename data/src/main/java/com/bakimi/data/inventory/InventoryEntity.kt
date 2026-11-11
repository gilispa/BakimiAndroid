package com.bakimi.data.inventory

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bakimi.data.measureUnitFromCode
import com.bakimi.data.storageCode
import com.bakimi.data.storageLocationFromCode
import com.bakimi.domain.inventory.InventoryItem
import java.time.LocalDate

/**
 * Inventory row. Table and column names keep their original Spanish names so existing databases
 * keep working; unit and location are stored as fixed codes (see StorageCodes.kt).
 */
@Entity(tableName = "inventario")
data class InventoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "nombre") val name: String,
    @ColumnInfo(name = "cantidad") val quantity: Double,
    @ColumnInfo(name = "unidad") val unit: String,
    @ColumnInfo(name = "ubicacion") val location: String,
    @ColumnInfo(name = "caducidad_dia_epoch") val expiryEpochDay: Long?,
    /** Catalog ingredient id; `null` for "other" foods or records from database version 1. */
    @ColumnInfo(name = "ingrediente_id") val ingredientId: String? = null,
)

fun InventoryItem.toEntity(): InventoryEntity = InventoryEntity(
    id = id,
    name = name,
    quantity = quantity,
    unit = unit.storageCode,
    location = location.storageCode,
    expiryEpochDay = expiry?.toEpochDay(),
    ingredientId = ingredientId,
)

fun InventoryEntity.toDomain(): InventoryItem = InventoryItem(
    id = id,
    name = name,
    quantity = quantity,
    unit = measureUnitFromCode(unit),
    location = storageLocationFromCode(location),
    expiry = expiryEpochDay?.let(LocalDate::ofEpochDay),
    ingredientId = ingredientId,
)
