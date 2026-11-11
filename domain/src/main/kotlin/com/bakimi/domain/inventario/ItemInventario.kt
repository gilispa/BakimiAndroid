package com.bakimi.domain.inventario

import java.time.LocalDate

/** Dónde se guarda un alimento. El orden define el orden de los grupos en Mi refri. */
enum class Ubicacion { REFRIGERADOR, CONGELADOR, DESPENSA }

/**
 * Un alimento registrado en el inventario.
 *
 * @property id 0 mientras no se ha guardado.
 * @property caducidad fecha opcional; `null` si el usuario no la registró.
 * @property ingredienteId ingrediente del catálogo al que corresponde; `null` si el usuario lo
 *   registró como "otro" alimento fuera del catálogo (o en registros anteriores al catálogo).
 */
data class ItemInventario(
    val id: Long = 0,
    val nombre: String,
    val cantidad: Double,
    val unidad: Unidad,
    val ubicacion: Ubicacion,
    val caducidad: LocalDate? = null,
    val ingredienteId: String? = null,
) {
    /** Un alimento agotado conserva su registro con cantidad cero para poder reabastecerlo. */
    val agotado: Boolean get() = cantidad <= 0.0
}
