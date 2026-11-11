package com.bakimi.domain.shopping

import com.bakimi.domain.ingredients.convert
import com.bakimi.domain.inventory.InventoryItem
import com.bakimi.domain.inventory.MeasureUnit
import com.bakimi.domain.inventory.round
import kotlin.math.ceil

/**
 * What has to be bought of an ingredient, in whole packages.
 *
 * @property quantity total to buy in [unit] (for example, 1,000 g even if 200 g are missing).
 * @property packages number of packages (bags, pieces, 100 g portions).
 * @property price estimated spend in MXN; `null` if there is no reference price.
 * @property approximate the quantity came from an approximate weight per piece.
 */
data class PurchaseItem(
    val requirement: Requirement,
    val unit: MeasureUnit,
    val quantity: Double,
    val packages: Int,
    val packageName: String?,
    val price: Double?,
    val approximate: Boolean,
)

/** Calculates the purchase for a requirement; `null` if nothing is missing. */
fun calculatePurchase(requirement: Requirement): PurchaseItem? {
    if (requirement.shortage <= 0.0) return null
    val info = requirement.ingredient.purchase
    val conversion = info?.let { requirement.ingredient.convert(requirement.shortage, requirement.unit, it.unit) }

    if (info == null || conversion == null) {
        // Without purchase data: buy what is missing; pieces, whole.
        val byPiece = requirement.unit == MeasureUnit.PIECE
        val quantity = if (byPiece) ceil(requirement.shortage - TOLERANCE) else requirement.shortage
        return PurchaseItem(
            requirement = requirement,
            unit = requirement.unit,
            quantity = quantity,
            packages = if (byPiece) quantity.toInt() else 1,
            packageName = null,
            price = null,
            approximate = requirement.approximate,
        )
    }

    val packages = ceil(conversion.quantity / info.packageSize - TOLERANCE).toInt().coerceAtLeast(1)
    return PurchaseItem(
        requirement = requirement,
        unit = info.unit,
        quantity = round(packages * info.packageSize, decimals = 3),
        packages = packages,
        packageName = info.packageName,
        price = info.price?.let { round(it * packages, decimals = 2) },
        approximate = requirement.approximate || conversion.approximate,
    )
}

/** Avoids buying an extra package because of floating-point leftovers (2.0000001 bags). */
private const val TOLERANCE = 1e-6

/** Shopping list for the plan: only ingredients with a shortage. */
fun shoppingListFor(plan: List<PlannedRecipe>, inventory: List<InventoryItem>): List<PurchaseItem> =
    consolidatePlan(plan, inventory).mapNotNull(::calculatePurchase)

/**
 * @property total sum of the known prices.
 * @property partial some item has no price; the total does not include it.
 */
data class PurchaseTotal(val total: Double, val partial: Boolean)

/** Adds up the prices. An unknown price never counts as zero: it marks the total as partial. */
fun totalOf(prices: List<Double?>): PurchaseTotal =
    PurchaseTotal(total = round(prices.filterNotNull().sum(), decimals = 2), partial = prices.any { it == null })

enum class BudgetStatus {
    NO_BUDGET,

    /** All prices are known and the total fits. */
    WITHIN,

    /** The known total already exceeds the budget. */
    EXCEEDED,

    /** What is known fits, but prices are missing: it cannot be guaranteed. */
    UNCERTAIN,
}

fun budgetStatus(total: PurchaseTotal, budget: Double?): BudgetStatus = when {
    budget == null -> BudgetStatus.NO_BUDGET
    total.total > budget -> BudgetStatus.EXCEEDED
    total.partial -> BudgetStatus.UNCERTAIN
    else -> BudgetStatus.WITHIN
}
