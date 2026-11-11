package com.bakimi.domain.ingredients

import java.text.Normalizer
import java.util.Locale

private val DIACRITIC_MARKS = Regex("\\p{Mn}+")
private val WHITESPACE = Regex("\\s+")

/** Consonants after which the Spanish plural adds "es" (limón → limones, pan → panes). */
private const val PLURAL_ES_CONSONANTS = "lnrdzj"

/**
 * Normalizes a name for comparison: lowercase, no accents, no repeated spaces and each word in a
 * canonical form that makes singular and plural equal ("Jitomates" → "jitomate",
 * "Limones" → "limon").
 *
 * The canonical form is only for comparing; it is not always a real word.
 */
fun normalizeName(text: String): String {
    val withoutAccents = Normalizer.normalize(text, Normalizer.Form.NFD).replace(DIACRITIC_MARKS, "")
    return withoutAccents.lowercase(Locale.ROOT)
        .trim()
        .split(WHITESPACE)
        .joinToString(" ") { canonicalForm(it) }
}

private fun canonicalForm(word: String): String {
    // Short words ("de", "con") are not relevant plurals.
    if (word.length <= 3) return word
    var root = word.removeSuffix("s")
    // "limones" → "limone" → "limon"; "limon" stays the same, so singular and plural match.
    if (root.length > 3 && root.endsWith("e") && root[root.length - 2] in PLURAL_ES_CONSONANTS) {
        root = root.dropLast(1)
    }
    return root
}

/** Tells whether a name typed in Mi refri corresponds to this ingredient or one of its synonyms. */
fun Ingredient.matches(inventoryName: String): Boolean {
    val query = normalizeName(inventoryName)
    return (listOf(name) + synonyms).any { normalizeName(it) == query }
}
