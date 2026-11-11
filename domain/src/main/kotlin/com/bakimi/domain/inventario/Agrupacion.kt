package com.bakimi.domain.inventario

import java.text.Collator
import java.util.Locale

/**
 * Agrupa el inventario por ubicación, en el orden de [Ubicacion], omitiendo grupos vacíos.
 *
 * Dentro de cada grupo aparecen primero los disponibles y al final los agotados, ambos por nombre
 * sin distinguir mayúsculas ni acentos.
 */
fun agruparPorUbicacion(items: List<ItemInventario>): Map<Ubicacion, List<ItemInventario>> {
    val porNombre = Collator.getInstance(Locale.forLanguageTag("es")).apply {
        strength = Collator.PRIMARY
    }
    val orden = compareBy<ItemInventario> { it.agotado }.thenComparator { a, b ->
        porNombre.compare(a.nombre, b.nombre)
    }
    val porUbicacion = items.groupBy { it.ubicacion }
    return Ubicacion.entries
        .mapNotNull { ubicacion -> porUbicacion[ubicacion]?.let { ubicacion to it.sortedWith(orden) } }
        .toMap()
}
