package com.bakimi.data.shopping

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bakimi.data.measureUnitFromCode
import com.bakimi.data.storageCode
import com.bakimi.domain.shopping.ManualItem
import com.bakimi.domain.shopping.PlanEntry
import com.bakimi.domain.shopping.PlanSettings
import com.bakimi.domain.shopping.PurchaseMark

// Table and column names keep their original Spanish names so existing databases keep working.

/** Plan settings: a single row with id 1. */
@Entity(tableName = "plan_config")
data class PlanSettingsEntity(
    @PrimaryKey val id: Int = SINGLE_ID,
    @ColumnInfo(name = "personas") val people: Int,
    @ColumnInfo(name = "presupuesto") val budget: Double?,
) {
    companion object {
        const val SINGLE_ID = 1
    }
}

@Entity(tableName = "plan_receta")
data class PlanRecipeEntity(
    @PrimaryKey @ColumnInfo(name = "receta_id") val recipeId: String,
    @ColumnInfo(name = "porciones") val servings: Int,
    /** Order in which recipes were added, so they are shown the same way. */
    @ColumnInfo(name = "agregada_en") val addedAt: Long,
)

@Entity(tableName = "compra_marca")
data class PurchaseMarkEntity(
    @PrimaryKey @ColumnInfo(name = "ingrediente_id") val ingredientId: String,
    @ColumnInfo(name = "comprado") val purchased: Boolean,
    @ColumnInfo(name = "cantidad_real") val actualQuantity: Double?,
    @ColumnInfo(name = "precio_real") val actualPrice: Double?,
)

@Entity(tableName = "compra_manual")
data class ManualItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "ingrediente_id") val ingredientId: String?,
    @ColumnInfo(name = "nombre") val name: String,
    @ColumnInfo(name = "cantidad") val quantity: Double,
    @ColumnInfo(name = "unidad") val unit: String,
    @ColumnInfo(name = "precio") val price: Double?,
    @ColumnInfo(name = "comprado") val purchased: Boolean,
)

fun PlanSettingsEntity.toDomain() = PlanSettings(people = people, budget = budget)
fun PlanSettings.toEntity() = PlanSettingsEntity(people = people, budget = budget)

fun PlanRecipeEntity.toDomain() = PlanEntry(recipeId, servings)

fun PurchaseMarkEntity.toDomain() = PurchaseMark(ingredientId, purchased, actualQuantity, actualPrice)
fun PurchaseMark.toEntity() = PurchaseMarkEntity(ingredientId, purchased, actualQuantity, actualPrice)

fun ManualItemEntity.toDomain() =
    ManualItem(id, ingredientId, name, quantity, measureUnitFromCode(unit), price, purchased)

fun ManualItem.toEntity() =
    ManualItemEntity(id, ingredientId, name, quantity, unit.storageCode, price, purchased)
