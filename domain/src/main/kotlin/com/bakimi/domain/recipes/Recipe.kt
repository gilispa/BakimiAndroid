package com.bakimi.domain.recipes

import com.bakimi.domain.ingredients.Ingredient
import com.bakimi.domain.inventory.MeasureUnit

/** Quantity of an ingredient for the recipe's base servings. */
data class RecipeIngredient(
    val ingredient: Ingredient,
    val quantity: Double,
    val unit: MeasureUnit,
    /** Optional clarification, for example "rebanadas" or "picado". */
    val note: String? = null,
)

data class Step(
    val instruction: String,
    /** Step minutes when timing matters (boiling, browning); `null` if not applicable. */
    val minutes: Int? = null,
    /** Reference image for the step (path inside assets); `null` if there is none yet. */
    val image: String? = null,
    /** Short video for the step; `null` if there is none yet. The written instruction is enough without it. */
    val video: String? = null,
)

/** Credit for an Unsplash photo. */
data class PhotoCredit(
    val author: String,
    val username: String,
    val photoId: String,
) {
    val photoUrl: String get() = "https://unsplash.com/photos/$photoId"
    val authorUrl: String get() = "https://unsplash.com/@$username"
}

data class Recipe(
    val id: String,
    val name: String,
    val description: String,
    val minutes: Int,
    val baseServings: Int,
    val ingredients: List<RecipeIngredient>,
    val steps: List<Step>,
    val photoCredit: PhotoCredit,
    /** Estimated protein per serving; `null` until there is a reliable source. */
    val proteinPerServingG: Double? = null,
)

/** Recipe catalog. The implementation lives in :data. */
interface RecipeRepository {
    fun all(): List<Recipe>

    fun get(id: String): Recipe?
}
