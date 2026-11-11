package com.bakimi.domain.shopping

import com.bakimi.domain.ingredients.Ingredient
import com.bakimi.domain.ingredients.convert
import com.bakimi.domain.inventory.InventoryItem
import com.bakimi.domain.recipes.Recipe

/**
 * Recipe that uses what would be left over after covering the plan.
 *
 * @property shared recipe ingredients left over from the plan (already at home or to be bought).
 */
data class Recommendation(val recipe: Recipe, val shared: List<Ingredient>)

/**
 * Recommends recipes outside the plan that use ingredients left over after covering it:
 * leftover = stock + purchase in whole packages − required by the plan.
 *
 * Does not count staples (salt, oil) or ingredients the plan already uses up, so the same quantity
 * is never used twice.
 */
fun recommendComplementary(
    plan: List<PlannedRecipe>,
    inventory: List<InventoryItem>,
    catalog: List<Recipe>,
    limit: Int = 3,
): List<Recommendation> {
    if (plan.isEmpty()) return emptyList()

    val leftovers: Set<String> = consolidatePlan(plan, inventory)
        .filter { !it.ingredient.staple && leftover(it) > 0.0 }
        .map { it.ingredient.id }
        .toSet()
    val inPlan = plan.map { it.recipe.id }.toSet()

    return catalog
        .filter { it.id !in inPlan }
        .mapNotNull { recipe ->
            val shared = recipe.ingredients.map { it.ingredient }.filter { it.id in leftovers }.distinct()
            shared.takeIf { it.isNotEmpty() }?.let { Recommendation(recipe, it) }
        }
        .sortedWith(compareBy({ -it.shared.size }, { it.recipe.minutes }, { it.recipe.name }))
        .take(limit)
}

/** What is left after the plan, counting the purchase in whole packages. */
private fun leftover(requirement: Requirement): Double {
    val purchase = calculatePurchase(requirement)
    val purchased = purchase?.let {
        requirement.ingredient.convert(it.quantity, it.unit, requirement.unit)?.quantity
    } ?: 0.0
    return requirement.available + purchased - requirement.required
}
