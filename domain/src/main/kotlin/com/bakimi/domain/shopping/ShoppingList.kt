package com.bakimi.domain.shopping

import com.bakimi.domain.ingredients.Category
import com.bakimi.domain.ingredients.Ingredient
import com.bakimi.domain.ingredients.convert
import com.bakimi.domain.inventory.MeasureUnit
import com.bakimi.domain.inventory.round

/** What the user marks or corrects on a plan item. Stored per ingredient. */
data class PurchaseMark(
    val ingredientId: String,
    val purchased: Boolean = false,
    /** Actual quantity in the item's purchase unit; `null` if the user did not change it. */
    val actualQuantity: Double? = null,
    /** Actual price paid in MXN; `null` if the user did not write it down. */
    val actualPrice: Double? = null,
)

/** Product added manually to the list (milk, coffee, snacks…). */
data class ManualItem(
    val id: Long = 0,
    val ingredientId: String?,
    val name: String,
    val quantity: Double,
    val unit: MeasureUnit,
    val price: Double?,
    val purchased: Boolean = false,
)

/**
 * Shopping list line.
 *
 * @property key identifies the line: "plan:<ingredient>" or "manual:<id>".
 * @property category list group; `null` for products outside the catalog ("Otros").
 * @property price actual price if written down, otherwise the estimate; `null` if unknown.
 * @property recipes plan recipes that need it (empty for manual products).
 */
data class ShoppingLine(
    val key: String,
    val ingredient: Ingredient?,
    val name: String,
    val category: Category?,
    val quantity: Double,
    val unit: MeasureUnit,
    val packages: Int?,
    val packageName: String?,
    val price: Double?,
    val actualPrice: Boolean,
    val approximate: Boolean,
    val purchased: Boolean,
    val recipes: List<String>,
) {
    val manual: Boolean get() = key.startsWith(MANUAL_PREFIX)
}

data class ShoppingList(
    val lines: List<ShoppingLine>,
    val total: PurchaseTotal,
) {
    val checked: List<ShoppingLine> get() = lines.filter { it.purchased }
}

const val PLAN_PREFIX = "plan:"
const val MANUAL_PREFIX = "manual:"

/**
 * Combines the plan's purchase items with the user's marks and the manual products.
 *
 * An actual quantity changes the estimated price proportionally; an actual price replaces the
 * estimate. Marks for items that are no longer in the plan are ignored.
 */
fun buildShoppingList(
    purchaseItems: List<PurchaseItem>,
    marks: List<PurchaseMark>,
    manualItems: List<ManualItem>,
    ingredient: (String) -> Ingredient?,
): ShoppingList {
    val marksById = marks.associateBy { it.ingredientId }
    val planLines = purchaseItems.map { purchaseItem ->
        val ing = purchaseItem.requirement.ingredient
        val mark = marksById[ing.id]
        val actualQuantity = mark?.actualQuantity
        val estimate = purchaseItem.price?.let { price ->
            if (actualQuantity == null) price else round(price * actualQuantity / purchaseItem.quantity, decimals = 2)
        }
        ShoppingLine(
            key = PLAN_PREFIX + ing.id,
            ingredient = ing,
            name = ing.name,
            category = ing.category,
            quantity = actualQuantity ?: purchaseItem.quantity,
            unit = purchaseItem.unit,
            packages = if (actualQuantity == null) purchaseItem.packages else null,
            packageName = if (actualQuantity == null) purchaseItem.packageName else null,
            price = mark?.actualPrice ?: estimate,
            actualPrice = mark?.actualPrice != null,
            approximate = purchaseItem.approximate && actualQuantity == null,
            purchased = mark?.purchased == true,
            recipes = purchaseItem.requirement.recipes,
        )
    }
    val manualLines = manualItems.map { manual ->
        val ing = manual.ingredientId?.let(ingredient)
        ShoppingLine(
            key = MANUAL_PREFIX + manual.id,
            ingredient = ing,
            name = ing?.name ?: manual.name,
            category = ing?.category,
            quantity = manual.quantity,
            unit = manual.unit,
            packages = null,
            packageName = null,
            price = manual.price ?: ing?.let { referencePrice(it, manual.quantity, manual.unit) },
            actualPrice = manual.price != null,
            approximate = false,
            purchased = manual.purchased,
            recipes = emptyList(),
        )
    }
    val lines = planLines + manualLines
    return ShoppingList(lines, totalOf(lines.map { it.price }))
}

/** Reference price proportional to the quantity; `null` if there is no price or it cannot be converted. */
private fun referencePrice(ingredient: Ingredient, quantity: Double, unit: MeasureUnit): Double? {
    val info = ingredient.purchase ?: return null
    val price = info.price ?: return null
    val inPurchaseUnit = ingredient.convert(quantity, unit, info.unit) ?: return null
    return round(price * inPurchaseUnit.quantity / info.packageSize, decimals = 2)
}

/** Products marked as purchased, with their actual quantity, ready to be added to Mi refri. */
fun ShoppingList.purchasedItems(): List<Purchased> =
    checked.map { Purchased(it.ingredient, it.name, it.quantity, it.unit) }
