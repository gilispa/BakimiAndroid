package com.bakimi.domain.ingredients

import java.text.Collator
import java.util.Locale

/** How strong a match is; a lower value is better. */
private enum class MatchStrength { EXACT, PREFIX, WORD_PREFIX, CONTAINS, SIMILAR }

private data class Candidate(val ingredient: Ingredient, val strength: MatchStrength, val bySynonym: Boolean)

/**
 * Searches the ingredient catalog while the user types.
 *
 * Compares against the name and synonyms ignoring case, accents and plurals, and tolerates small
 * typos ("jitomte", "pechga"). Orders from the strongest match to the weakest: exact, prefix,
 * word prefix, contains, similar. With equal strength, name matches come before synonym matches,
 * then alphabetical order.
 */
fun searchIngredients(text: String, catalog: List<Ingredient>, limit: Int = 8): List<Ingredient> {
    val query = normalizeName(text)
    if (query.isEmpty()) return emptyList()

    val alphabetical = Collator.getInstance(Locale.forLanguageTag("es")).apply { strength = Collator.PRIMARY }
    return catalog
        .mapNotNull { ingredient -> bestMatch(ingredient, query) }
        .sortedWith(
            compareBy<Candidate>({ it.strength }, { it.bySynonym })
                .thenComparator { a, b -> alphabetical.compare(a.ingredient.name, b.ingredient.name) },
        )
        .take(limit)
        .map { it.ingredient }
}

/** Catalog ingredient that corresponds exactly to [name], if any. */
fun List<Ingredient>.buscarPorNombre(name: String): Ingredient? = firstOrNull { it.matches(name) }

private fun bestMatch(ingredient: Ingredient, query: String): Candidate? {
    val name = strength(normalizeName(ingredient.name), query)
        ?.let { Candidate(ingredient, it, bySynonym = false) }
    val synonym = ingredient.synonyms
        .mapNotNull { strength(normalizeName(it), query) }
        .minOrNull()
        ?.let { Candidate(ingredient, it, bySynonym = true) }
    return listOfNotNull(name, synonym).minWithOrNull(compareBy({ it.strength }, { it.bySynonym }))
}

private fun strength(term: String, query: String): MatchStrength? {
    val words = term.split(' ')
    return when {
        term == query -> MatchStrength.EXACT
        term.startsWith(query) -> MatchStrength.PREFIX
        words.any { it.startsWith(query) } -> MatchStrength.WORD_PREFIX
        query.length >= 3 && term.contains(query) -> MatchStrength.CONTAINS
        isSimilar(query, words + term) -> MatchStrength.SIMILAR
        else -> null
    }
}

/**
 * Tolerates 1 error in 4–5 letter texts and 2 in longer ones. Compares against the beginning of
 * each word so it works while the user is still typing.
 */
private fun isSimilar(query: String, candidates: List<String>): Boolean {
    if (query.length < 4) return false
    val tolerance = if (query.length <= 5) 1 else 2
    return candidates.any { candidate ->
        (query.length - 1..query.length + 1).any { length ->
            length <= candidate.length && distance(query, candidate.take(length)) <= tolerance
        }
    }
}

/** Edit distance that counts swapping two adjacent letters as a single error ("platnao"). */
private fun distance(a: String, b: String): Int {
    val d = Array(a.length + 1) { i -> IntArray(b.length + 1) { j -> if (i == 0) j else if (j == 0) i else 0 } }
    for (i in 1..a.length) {
        for (j in 1..b.length) {
            val cost = if (a[i - 1] == b[j - 1]) 0 else 1
            d[i][j] = minOf(d[i - 1][j] + 1, d[i][j - 1] + 1, d[i - 1][j - 1] + cost)
            if (i > 1 && j > 1 && a[i - 1] == b[j - 2] && a[i - 2] == b[j - 1]) {
                d[i][j] = minOf(d[i][j], d[i - 2][j - 2] + 1)
            }
        }
    }
    return d[a.length][b.length]
}
