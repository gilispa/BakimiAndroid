package com.bakimi.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bakimi.data.cooking.PreparationDao
import com.bakimi.data.cooking.PreparationEntity
import com.bakimi.data.shopping.ManualItemEntity
import com.bakimi.data.shopping.ShoppingDao
import com.bakimi.data.shopping.PurchaseMarkEntity
import com.bakimi.data.shopping.PlanSettingsEntity
import com.bakimi.data.shopping.PlanDao
import com.bakimi.data.shopping.PlanRecipeEntity
import com.bakimi.data.inventory.InventoryDao
import com.bakimi.data.inventory.InventoryEntity

/**
 * Bakimi's local database. The schema is exported to data/schemas to write migrations when it
 * changes; destructive migration is never used.
 */
@Database(
    entities = [
        InventoryEntity::class,
        PlanSettingsEntity::class,
        PlanRecipeEntity::class,
        PurchaseMarkEntity::class,
        ManualItemEntity::class,
        PreparationEntity::class,
    ],
    version = 4,
    exportSchema = true,
)
abstract class BakimiDatabase : RoomDatabase() {
    abstract fun inventoryDao(): InventoryDao
    abstract fun planDao(): PlanDao
    abstract fun shoppingDao(): ShoppingDao
    abstract fun preparationDao(): PreparationDao
}

/** Version 4: history of preparations confirmed in Cooking Mode. */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Same SQL that Room exports in data/schemas/.../4.json.
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `preparacion` (`id` TEXT NOT NULL, `receta_id` TEXT NOT NULL, " +
                "`porciones` INTEGER NOT NULL, `fecha_millis` INTEGER NOT NULL, PRIMARY KEY(`id`))",
        )
    }
}

/**
 * Version 2: optional link from each food to the ingredient catalog. Existing records stay
 * unlinked and keep matching recipes by name.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE inventario ADD COLUMN ingrediente_id TEXT")
    }
}

/** Version 3: weekly plan and shopping list. Only adds tables; does not touch the inventory. */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Same SQL that Room exports in data/schemas/.../3.json, so validation matches.
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
