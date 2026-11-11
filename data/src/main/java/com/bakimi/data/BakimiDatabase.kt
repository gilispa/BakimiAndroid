package com.bakimi.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bakimi.data.cocina.PreparacionDao
import com.bakimi.data.cocina.PreparacionEntity
import com.bakimi.data.compras.ArticuloManualEntity
import com.bakimi.data.compras.ComprasDao
import com.bakimi.data.compras.MarcaCompraEntity
import com.bakimi.data.compras.PlanConfigEntity
import com.bakimi.data.compras.PlanDao
import com.bakimi.data.compras.PlanRecetaEntity
import com.bakimi.data.inventario.InventarioDao
import com.bakimi.data.inventario.InventarioEntity

/**
 * Base de datos local de Bakimi. El esquema se exporta a data/schemas para escribir
 * migraciones cuando cambie; nunca se usa la migración destructiva.
 */
@Database(
    entities = [
        InventarioEntity::class,
        PlanConfigEntity::class,
        PlanRecetaEntity::class,
        MarcaCompraEntity::class,
        ArticuloManualEntity::class,
        PreparacionEntity::class,
    ],
    version = 4,
    exportSchema = true,
)
abstract class BakimiDatabase : RoomDatabase() {
    abstract fun inventarioDao(): InventarioDao
    abstract fun planDao(): PlanDao
    abstract fun comprasDao(): ComprasDao
    abstract fun preparacionDao(): PreparacionDao
}

/** Versión 4: historial de preparaciones confirmadas en el Modo Cocina. */
val MIGRACION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Mismo SQL que Room exporta en data/schemas/.../4.json.
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `preparacion` (`id` TEXT NOT NULL, `receta_id` TEXT NOT NULL, " +
                "`porciones` INTEGER NOT NULL, `fecha_millis` INTEGER NOT NULL, PRIMARY KEY(`id`))",
        )
    }
}

/**
 * Versión 2: vínculo opcional de cada alimento con el catálogo de ingredientes. Los registros
 * existentes quedan sin vínculo y se siguen relacionando con las recetas por su nombre.
 */
val MIGRACION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE inventario ADD COLUMN ingrediente_id TEXT")
    }
}

/** Versión 3: plan semanal y lista de compras. Solo agrega tablas; no toca el inventario. */
val MIGRACION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Mismo SQL que Room exporta en data/schemas/.../3.json, para que la validación coincida.
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `plan_config` (`id` INTEGER NOT NULL, `personas` INTEGER NOT NULL, " +
                "`presupuesto` REAL, PRIMARY KEY(`id`))",
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `plan_receta` (`receta_id` TEXT NOT NULL, `porciones` INTEGER NOT NULL, " +
                "`agregada_en` INTEGER NOT NULL, PRIMARY KEY(`receta_id`))",
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `compra_marca` (`ingrediente_id` TEXT NOT NULL, `comprado` INTEGER NOT NULL, " +
                "`cantidad_real` REAL, `precio_real` REAL, PRIMARY KEY(`ingrediente_id`))",
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `compra_manual` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`ingrediente_id` TEXT, `nombre` TEXT NOT NULL, `cantidad` REAL NOT NULL, `unidad` TEXT NOT NULL, " +
                "`precio` REAL, `comprado` INTEGER NOT NULL)",
        )
    }
}
