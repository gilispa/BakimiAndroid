package com.bakimi.domain.ingredientes

import java.text.Collator
import java.util.Locale

/** Qué tan fuerte es una coincidencia; un valor menor es mejor. */
private enum class Fuerza { EXACTA, EMPIEZA, PALABRA_EMPIEZA, CONTIENE, PARECIDA }

private data class Candidato(val ingrediente: Ingrediente, val fuerza: Fuerza, val porSinonimo: Boolean)

/**
 * Busca ingredientes del catálogo mientras el usuario escribe.
 *
 * Compara contra el nombre y los sinónimos sin distinguir mayúsculas, acentos ni plurales, y
 * tolera errores de escritura pequeños ("jitomte", "pechga"). Ordena de la coincidencia más
 * fuerte a la más débil: exacta, empieza igual, alguna palabra empieza igual, contiene el texto,
 * parecida. A igual fuerza, primero lo que coincide con el nombre y luego por orden alfabético.
 */
fun buscarIngredientes(texto: String, catalogo: List<Ingrediente>, limite: Int = 8): List<Ingrediente> {
    val buscado = normalizarNombre(texto)
    if (buscado.isEmpty()) return emptyList()

    val alfabetico = Collator.getInstance(Locale.forLanguageTag("es")).apply { strength = Collator.PRIMARY }
    return catalogo
        .mapNotNull { ingrediente -> mejorCoincidencia(ingrediente, buscado) }
        .sortedWith(
            compareBy<Candidato>({ it.fuerza }, { it.porSinonimo })
                .thenComparator { a, b -> alfabetico.compare(a.ingrediente.nombre, b.ingrediente.nombre) },
        )
        .take(limite)
        .map { it.ingrediente }
}

/** Ingrediente del catálogo que corresponde exactamente a [nombre], si existe. */
fun List<Ingrediente>.buscarPorNombre(nombre: String): Ingrediente? = firstOrNull { it.coincideCon(nombre) }

private fun mejorCoincidencia(ingrediente: Ingrediente, buscado: String): Candidato? {
    val nombre = fuerza(normalizarNombre(ingrediente.nombre), buscado)
        ?.let { Candidato(ingrediente, it, porSinonimo = false) }
    val sinonimo = ingrediente.sinonimos
        .mapNotNull { fuerza(normalizarNombre(it), buscado) }
        .minOrNull()
        ?.let { Candidato(ingrediente, it, porSinonimo = true) }
    return listOfNotNull(nombre, sinonimo).minWithOrNull(compareBy({ it.fuerza }, { it.porSinonimo }))
}

private fun fuerza(termino: String, buscado: String): Fuerza? {
    val palabras = termino.split(' ')
    return when {
        termino == buscado -> Fuerza.EXACTA
        termino.startsWith(buscado) -> Fuerza.EMPIEZA
        palabras.any { it.startsWith(buscado) } -> Fuerza.PALABRA_EMPIEZA
        buscado.length >= 3 && termino.contains(buscado) -> Fuerza.CONTIENE
        esParecido(buscado, palabras + termino) -> Fuerza.PARECIDA
        else -> null
    }
}

/**
 * Tolera 1 error en textos de 4 a 5 letras y 2 en textos más largos. Compara contra el inicio de
 * cada palabra para que funcione mientras el usuario todavía escribe.
 */
private fun esParecido(buscado: String, candidatos: List<String>): Boolean {
    if (buscado.length < 4) return false
    val tolerancia = if (buscado.length <= 5) 1 else 2
    return candidatos.any { candidato ->
        (buscado.length - 1..buscado.length + 1).any { largo ->
            largo <= candidato.length && distancia(buscado, candidato.take(largo)) <= tolerancia
        }
    }
}

/** Distancia de edición que cuenta como un solo error intercambiar dos letras vecinas ("platnao"). */
private fun distancia(a: String, b: String): Int {
    val d = Array(a.length + 1) { i -> IntArray(b.length + 1) { j -> if (i == 0) j else if (j == 0) i else 0 } }
    for (i in 1..a.length) {
        for (j in 1..b.length) {
            val costo = if (a[i - 1] == b[j - 1]) 0 else 1
            d[i][j] = minOf(d[i - 1][j] + 1, d[i][j - 1] + 1, d[i - 1][j - 1] + costo)
            if (i > 1 && j > 1 && a[i - 1] == b[j - 2] && a[i - 2] == b[j - 1]) {
                d[i][j] = minOf(d[i][j], d[i - 2][j - 2] + 1)
            }
        }
    }
    return d[a.length][b.length]
}
