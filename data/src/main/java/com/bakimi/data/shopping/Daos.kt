package com.bakimi.data.shopping

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface PlanDao {
    @Query("SELECT * FROM plan_config WHERE id = ${PlanSettingsEntity.SINGLE_ID}")
    fun observeSettings(): Flow<PlanSettingsEntity?>

    @Upsert
    suspend fun saveSettings(config: PlanSettingsEntity)

    @Query("SELECT * FROM plan_receta ORDER BY agregada_en")
    fun observeRecipes(): Flow<List<PlanRecipeEntity>>

    @Query("SELECT * FROM plan_receta WHERE receta_id = :recipeId")
    suspend fun getRecipe(recipeId: String): PlanRecipeEntity?

    @Upsert
    suspend fun saveRecipe(recipe: PlanRecipeEntity)

    @Query("DELETE FROM plan_receta WHERE receta_id = :recipeId")
    suspend fun removeRecipe(recipeId: String)
}

@Dao
interface ShoppingDao {
    @Query("SELECT * FROM compra_marca")
    fun observeMarks(): Flow<List<PurchaseMarkEntity>>

    @Query("SELECT * FROM compra_manual ORDER BY id")
    fun observeManuals(): Flow<List<ManualItemEntity>>

    @Query("SELECT * FROM compra_marca WHERE ingrediente_id = :ingredientId")
    suspend fun getMark(ingredientId: String): PurchaseMarkEntity?

    @Query("SELECT * FROM compra_manual WHERE id = :id")
    suspend fun getManual(id: Long): ManualItemEntity?

    @Upsert
    suspend fun saveMark(mark: PurchaseMarkEntity)

    /** Returns the new id on insert, or -1 on update. */
    @Upsert
    suspend fun saveManual(purchaseItem: ManualItemEntity): Long

    @Query("DELETE FROM compra_manual WHERE id = :id")
    suspend fun deleteManual(id: Long)

    @Query("DELETE FROM compra_marca WHERE ingrediente_id IN (:ids)")
    suspend fun deleteMarks(ids: List<String>)

    @Query("DELETE FROM compra_manual WHERE id IN (:ids)")
    suspend fun deleteManuals(ids: List<Long>)
}
