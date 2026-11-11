package com.bakimi.domain.ingredients

import com.bakimi.domain.inventory.StorageLocation
import com.bakimi.domain.inventory.MeasureUnit

/** Shopping list groups (product definition, section 13). */
enum class Category { PRODUCE, MEAT, DAIRY_EGGS, PANTRY }

/**
 * Catalog ingredient. Mi refri and the recipes use the same catalog so names match regardless of
 * how the user types them.
 *
 * @property synonyms other names of the same product ("tomate" for jitomate). Never substitutes.
 * @property suggestedUnit unit preselected when adding it in Mi refri.
 * @property suggestedLocation where it is usually stored.
 * @property gramsPerPiece approximate weight of one piece; allows comparing "1 kg de tortillas" with
 * "6 tortillas". `null` if there is no reasonable equivalence (for example, garlic: head or clove?).
 * @property purchase how it is bought and its reference price; `null` if there is no data.
 * @property staple seasoning or staple (salt, oil) that almost every recipe uses; it does not count
 * as a "shared ingredient" when recommending recipes.
 */
data class Ingredient(
    val id: String,
    val name: String,
    val synonyms: List<String> = emptyList(),
    val category: Category = Category.PANTRY,
    val suggestedUnit: MeasureUnit = MeasureUnit.PIECE,
    val suggestedLocation: StorageLocation = StorageLocation.PANTRY,
    val gramsPerPiece: Double? = null,
    val purchase: PurchaseInfo? = null,
    val staple: Boolean = false,
)

/**
 * How an ingredient is bought.
 *
 * @property unit unit in which it is bought.
 * @property packageSize minimum purchase size in [unit]: 1 piece, 100 g loose or a 1,000 g bag.
 * Whatever is missing is rounded up to whole packages.
 * @property packageName package name ("bolsa de 1 kg"); `null` if bought loose or by the piece.
 * @property price approximate reference price of one package, in MXN; `null` if unknown.
 */
data class PurchaseInfo(
    val unit: MeasureUnit,
    val packageSize: Double,
    val packageName: String? = null,
    val price: Double? = null,
)

/** Ingredient catalog. The implementation lives in :data. */
interface IngredientRepository {
    fun all(): List<Ingredient>

    fun get(id: String): Ingredient?
}
