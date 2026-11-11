package com.bakimi.domain.recipes

/** Maximum minutes for the "Listo en menos de 20 minutos" section. */
const val QUICK_MINUTES = 20

data class HomeSections(
    /** Carousel recipes: the best ones according to what is at home. */
    val featured: List<RecipeEvaluation>,
    val readyToCook: List<RecipeEvaluation>,
    /** Recipes missing one or two ingredients. */
    val almostReady: List<RecipeEvaluation>,
    val quick: List<RecipeEvaluation>,
    /** The rest of the recipes. */
    val moreIdeas: List<RecipeEvaluation>,
)

/**
 * Sorts recipes by fewer missing ingredients, then more ingredients used, then less time, and
 * splits them into the Home sections.
 */
fun buildHome(evaluations: List<RecipeEvaluation>, maxFeatured: Int = 6): HomeSections {
    val sorted = evaluations.sortedWith(
        compareBy<RecipeEvaluation>(
            { it.missing },
            { -it.covered },
            { it.recipe.minutes },
            { it.recipe.name },
        ),
    )
    return HomeSections(
        featured = sorted.take(maxFeatured),
        readyToCook = sorted.filter { it.isReady },
        almostReady = sorted.filter { it.missing in 1..2 },
        quick = sorted.filter { it.recipe.minutes <= QUICK_MINUTES },
        moreIdeas = sorted.filter { it.missing > 2 },
    )
}
