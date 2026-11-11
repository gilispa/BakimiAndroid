package com.bakimi.domain.ingredientes

import java.text.Normalizer
import java.util.Locale

private val MARCAS_DIACRITICAS = Regex("\\p{Mn}+")
private val ESPACIOS = Regex("\\s+")

/** Consonantes tras las que el plural en español agrega "es" (limón → limones, pan → panes). */
private const val CONSONANTES_PLURAL_ES = "lnrdzj"

/**
 * Normaliza un nombre para compararlo: minúsculas, sin acentos, sin espacios repetidos y cada
 * palabra en una forma canónica que iguala singular y plural ("Jitomates" → "jitomate",
 * "Limones" → "limon").
 *
 * La forma canónica solo sirve para comparar; no siempre es una palabra real.
 */
fun normalizarNombre(texto: String): String {
    val sinAcentos = Normalizer.normalize(texto, Normalizer.Form.NFD).replace(MARCAS_DIACRITICAS, "")
    return sinAcentos.lowercase(Locale.ROOT)
        .trim()
        .split(ESPACIOS)
        .joinToString(" ") { formaCanonica(it) }
}

private fun formaCanonica(palabra: String): String {
    // Las palabras cortas ("de", "con") no son plurales relevantes.
    if (palabra.length <= 3) return palabra
    var raiz = palabra.removeSuffix("s")
    // "limones" → "limone" → "limon"; "limon" se queda igual. Así singular y plural coinciden.
    if (raiz.length > 3 && raiz.endsWith("e") && raiz[raiz.length - 2] in CONSONANTES_PLURAL_ES) {
        raiz = raiz.dropLast(1)
    }
    return raiz
}

/** Indica si un nombre escrito en Mi refri corresponde a este ingrediente o a uno de sus sinónimos. */
fun Ingrediente.coincideCon(nombreInventario: String): Boolean {
    val buscado = normalizarNombre(nombreInventario)
    return (listOf(nombre) + sinonimos).any { normalizarNombre(it) == buscado }
}
