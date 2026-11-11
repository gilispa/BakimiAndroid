package com.bakimi.domain.shopping

import com.bakimi.domain.ingredients.IngredientRepository
import com.bakimi.domain.inventory.InventoryRepository
import com.bakimi.domain.recipes.RecipeEvaluation
import com.bakimi.domain.recipes.RecipeRepository
import com.bakimi.domain.recipes.evaluateRecipe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Recipe outside the plan, with what buying its missing ingredients would cost with the current inventory. */
data class RecipeSuggestion(val evaluation: RecipeEvaluation, val missingPurchase: PurchaseTotal)

/** Everything Mi semana and Compras show. */
data class Week(
    val config: PlanSettings,
    val recipes: List<PlannedRecipe>,
    val shoppingList: ShoppingList,
    val budgetStatus: BudgetStatus,
    val recommendations: List<Recommendation>,
    val suggestions: List<RecipeSuggestion>,
)

/** Weekly plan and shopping list. A single instance so its lock covers the whole app. */
class ShoppingManager(
    private val plan: PlanRepository,
    private val shopping: ShoppingRepository,
    private val recipes: RecipeRepository,
    private val ingredients: IngredientRepository,
    private val inventory: InventoryRepository,
) {
    private val lock = Mutex()

    fun observeWeek(): Flow<Week> = combine(
        plan.observeSettings(),
        plan.observeRecipes(),
        inventory.observeAll(),
        shopping.observeMarks(),
        shopping.observeManuals(),
    ) { config, entries, stockItems, marks, manualItems ->
        val plannedRecipes = entries.mapNotNull { entry ->
            recipes.get(entry.recipeId)?.let { PlannedRecipe(it, entry.servings) }
        }
        val shoppingList = buildShoppingList(shoppingListFor(plannedRecipes, stockItems), marks, manualItems, ingredients::get)
        val inPlan = plannedRecipes.map { it.recipe.id }.toSet()
        val suggestions = recipes.all()
            .filter { it.id !in inPlan }
            .map { recipe ->
                val servings = config.people
                RecipeSuggestion(
                    evaluation = evaluateRecipe(recipe, servings, stockItems),
                    missingPurchase = totalOf(
                        shoppingListFor(listOf(PlannedRecipe(recipe, servings)), stockItems).map { it.price },
                    ),
                )
            }
            .sortedWith(
                compareBy<RecipeSuggestion>(
                    { it.evaluation.missing },
                    { -it.evaluation.covered },
                    { it.evaluation.recipe.minutes },
                ),
            )
        Week(
            config = config,
            recipes = plannedRecipes,
            shoppingList = shoppingList,
            budgetStatus = budgetStatus(shoppingList.total, config.budget),
            recommendations = recommendComplementary(plannedRecipes, stockItems, recipes.all()),
            suggestions = suggestions,
        )
    }

    suspend fun saveSettings(config: PlanSettings) {
        plan.saveSettings(config.copy(people = config.people.coerceIn(MIN_SERVINGS, MAX_SERVINGS)))
    }

    /** Adds a recipe to the plan; without explicit servings it uses the number of people. */
    suspend fun addToPlan(recipeId: String, servings: Int? = null) {
        val quantity = servings ?: plan.observeSettings().first().people
        plan.saveRecipe(PlanEntry(recipeId, quantity.coerceIn(MIN_SERVINGS, MAX_SERVINGS)))
    }

    suspend fun removeFromPlan(recipeId: String) = plan.removeRecipe(recipeId)

    suspend fun changeServings(recipeId: String, servings: Int) =
        plan.saveRecipe(PlanEntry(recipeId, servings.coerceIn(MIN_SERVINGS, MAX_SERVINGS)))

    /** Marking does not change the inventory; only [finishShopping] does. */
    suspend fun markPurchased(key: String, purchased: Boolean) = lock.withLock {
        when {
            key.startsWith(PLAN_PREFIX) -> {
                val id = key.removePrefix(PLAN_PREFIX)
                shopping.saveMark((shopping.getMark(id) ?: PurchaseMark(id)).copy(purchased = purchased))
            }
            key.startsWith(MANUAL_PREFIX) -> {
                shopping.getManual(key.removePrefix(MANUAL_PREFIX).toLong())
                    ?.let { shopping.saveManual(it.copy(purchased = purchased)) }
            }
        }
    }

    /** Corrects a line's actual quantity or actual price; `null` keeps the estimate. */
    suspend fun correct(key: String, quantity: Double?, price: Double?) = lock.withLock {
        when {
            key.startsWith(PLAN_PREFIX) -> {
                val id = key.removePrefix(PLAN_PREFIX)
                val mark = shopping.getMark(id) ?: PurchaseMark(id)
                shopping.saveMark(mark.copy(actualQuantity = quantity, actualPrice = price))
            }
            key.startsWith(MANUAL_PREFIX) -> {
                shopping.getManual(key.removePrefix(MANUAL_PREFIX).toLong())?.let {
                    shopping.saveManual(it.copy(quantity = quantity ?: it.quantity, price = price))
                }
            }
        }
    }

    suspend fun addManual(purchaseItem: ManualItem) {
        shopping.saveManual(purchaseItem.copy(id = 0, name = purchaseItem.name.trim()))
    }

    suspend fun deleteManual(id: Long) = shopping.deleteManual(id)

    /**
  * Adds only the checked products to Mi refri, with their actual quantity, and removes them from
  * the list. Repeating it duplicates nothing: what was already added is no longer checked.
  *
  * @return number of products added.
  */
    suspend fun finishShopping(): Int = lock.withLock {
        val shoppingList = observeWeek().first().shoppingList
        val checked = shoppingList.checked
        if (checked.isEmpty()) return@withLock 0

        val changes = restock(inventory.observeAll().first(), shoppingList.purchasedItems())
        shopping.registerPurchase(
            inventory = changes,
            processedMarks = checked.filter { !it.manual }.map { it.key.removePrefix(PLAN_PREFIX) },
            processedManuals = checked.filter { it.manual }.map { it.key.removePrefix(MANUAL_PREFIX).toLong() },
        )
        checked.size
    }

    companion object {
        const val MIN_SERVINGS = 1
        const val MAX_SERVINGS = 12
    }
}
