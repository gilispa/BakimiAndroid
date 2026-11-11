package com.bakimi.data.di

import android.content.Context
import androidx.room.Room
import com.bakimi.data.BakimiDatabase
import com.bakimi.data.MIGRACION_1_2
import com.bakimi.data.MIGRACION_2_3
import com.bakimi.data.MIGRACION_3_4
import com.bakimi.data.cocina.PreparacionRepositoryRoom
import com.bakimi.data.compras.ComprasDao
import com.bakimi.data.compras.ComprasRepositoryRoom
import com.bakimi.data.compras.PlanDao
import com.bakimi.data.compras.PlanRepositoryRoom
import com.bakimi.data.ingredientes.CatalogoIngredientesLocal
import com.bakimi.data.inventario.InventarioDao
import com.bakimi.data.inventario.InventarioRepositoryRoom
import com.bakimi.data.recetas.CatalogoRecetasLocal
import com.bakimi.domain.cocina.GestorCocina
import com.bakimi.domain.cocina.PreparacionRepository
import com.bakimi.domain.compras.ComprasRepository
import com.bakimi.domain.compras.GestorCompras
import com.bakimi.domain.compras.PlanRepository
import com.bakimi.domain.ingredientes.IngredienteRepository
import com.bakimi.domain.inventario.GestorInventario
import com.bakimi.domain.inventario.InventarioRepository
import com.bakimi.domain.recetas.RecetaRepository
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
    abstract fun bindInventarioRepository(impl: InventarioRepositoryRoom): InventarioRepository

    @Binds
    @Singleton
    abstract fun bindRecetaRepository(impl: CatalogoRecetasLocal): RecetaRepository

    @Binds
    @Singleton
    abstract fun bindIngredienteRepository(impl: CatalogoIngredientesLocal): IngredienteRepository

    @Binds
    @Singleton
    abstract fun bindPlanRepository(impl: PlanRepositoryRoom): PlanRepository

    @Binds
    @Singleton
    abstract fun bindComprasRepository(impl: ComprasRepositoryRoom): ComprasRepository

    @Binds
    @Singleton
    abstract fun bindPreparacionRepository(impl: PreparacionRepositoryRoom): PreparacionRepository

    companion object {

        @Provides
        @Singleton
        fun provideDatabase(@ApplicationContext context: Context): BakimiDatabase =
            Room.databaseBuilder(context, BakimiDatabase::class.java, "bakimi.db")
                .addMigrations(MIGRACION_1_2, MIGRACION_2_3, MIGRACION_3_4)
                .build()

        @Provides
        fun provideInventarioDao(db: BakimiDatabase): InventarioDao = db.inventarioDao()

        @Provides
        fun providePlanDao(db: BakimiDatabase): PlanDao = db.planDao()

        @Provides
        fun provideComprasDao(db: BakimiDatabase): ComprasDao = db.comprasDao()

        /** Una sola instancia para que su cerrojo proteja todas las pantallas a la vez. */
        @Provides
        @Singleton
        fun provideGestorInventario(repositorio: InventarioRepository): GestorInventario =
            GestorInventario(repositorio)

        @Provides
        @Singleton
        fun provideGestorCocina(inventario: InventarioRepository, preparaciones: PreparacionRepository): GestorCocina =
            GestorCocina(inventario, preparaciones)

        /** Una sola instancia para que una compra no se registre dos veces desde dos pantallas. */
        @Provides
        @Singleton
        fun provideGestorCompras(
            plan: PlanRepository,
            compras: ComprasRepository,
            recetas: RecetaRepository,
            ingredientes: IngredienteRepository,
            inventario: InventarioRepository,
        ): GestorCompras = GestorCompras(plan, compras, recetas, ingredientes, inventario)
    }
}
