package com.bakimi.data.di

import android.content.Context
import androidx.room.Room
import com.bakimi.data.BakimiDatabase
import com.bakimi.data.MIGRATION_1_2
import com.bakimi.data.MIGRATION_2_3
import com.bakimi.data.MIGRATION_3_4
import com.bakimi.data.cooking.RoomPreparationRepository
import com.bakimi.data.shopping.ShoppingDao
import com.bakimi.data.shopping.RoomShoppingRepository
import com.bakimi.data.shopping.PlanDao
import com.bakimi.data.shopping.RoomPlanRepository
import com.bakimi.data.ingredients.LocalIngredientCatalog
import com.bakimi.data.inventory.InventoryDao
import com.bakimi.data.inventory.RoomInventoryRepository
import com.bakimi.data.recipes.LocalRecipeCatalog
import com.bakimi.domain.cooking.CookingManager
import com.bakimi.domain.cooking.PreparationRepository
import com.bakimi.domain.shopping.ShoppingRepository
import com.bakimi.domain.shopping.ShoppingManager
import com.bakimi.domain.shopping.PlanRepository
import com.bakimi.domain.ingredients.IngredientRepository
import com.bakimi.domain.inventory.InventoryManager
import com.bakimi.domain.inventory.InventoryRepository
import com.bakimi.domain.recipes.RecipeRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindInventoryRepository(impl: RoomInventoryRepository): InventoryRepository

    @Binds
    @Singleton
    abstract fun bindRecipeRepository(impl: LocalRecipeCatalog): RecipeRepository

    @Binds
    @Singleton
    abstract fun bindIngredientRepository(impl: LocalIngredientCatalog): IngredientRepository

    @Binds
    @Singleton
    abstract fun bindPlanRepository(impl: RoomPlanRepository): PlanRepository

    @Binds
    @Singleton
    abstract fun bindShoppingRepository(impl: RoomShoppingRepository): ShoppingRepository

    @Binds
    @Singleton
    abstract fun bindPreparationRepository(impl: RoomPreparationRepository): PreparationRepository

    companion object {

        @Provides
        @Singleton
        fun provideDatabase(@ApplicationContext context: Context): BakimiDatabase =
            Room.databaseBuilder(context, BakimiDatabase::class.java, "bakimi.db")
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                .build()

        @Provides
        fun provideInventoryDao(db: BakimiDatabase): InventoryDao = db.inventoryDao()

        @Provides
        fun providePlanDao(db: BakimiDatabase): PlanDao = db.planDao()

        @Provides
        fun provideShoppingDao(db: BakimiDatabase): ShoppingDao = db.shoppingDao()

        /** A single instance so its lock protects every screen at once. */
        @Provides
        @Singleton
        fun provideInventoryManager(repository: InventoryRepository): InventoryManager =
            InventoryManager(repository)

        @Provides
        @Singleton
        fun provideCookingManager(inventory: InventoryRepository, preparations: PreparationRepository): CookingManager =
            CookingManager(inventory, preparations)

        /** A single instance so a purchase is never registered twice from two screens. */
        @Provides
        @Singleton
        fun provideShoppingManager(
            plan: PlanRepository,
            shopping: ShoppingRepository,
            recipes: RecipeRepository,
            ingredients: IngredientRepository,
            inventory: InventoryRepository,
        ): ShoppingManager = ShoppingManager(plan, shopping, recipes, ingredients, inventory)
    }
}
