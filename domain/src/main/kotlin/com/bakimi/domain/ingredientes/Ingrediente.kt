package com.bakimi.domain.ingredientes

import com.bakimi.domain.inventario.Ubicacion
import com.bakimi.domain.inventario.Unidad

/** Grupos de la lista de compras (documento de producto, sección 13). */
enum class Categoria { FRUTAS_VERDURAS, CARNES, LACTEOS_HUEVO, DESPENSA }

/**
 * Ingrediente del catálogo. Mi refri y las recetas usan el mismo catálogo para que los nombres
 * coincidan sin depender de cómo escriba el usuario.
 *
 * @property sinonimos otros nombres del mismo producto ("tomate" para jitomate). Nunca sustitutos.
 * @property unidadSugerida unidad que se preselecciona al registrarlo en Mi refri.
 * @property ubicacionSugerida dónde se suele guardar.
 * @property gramosPorPieza peso aproximado de una pieza; permite comparar "1 kg de tortillas" con
 *   "6 tortillas". `null` si no hay una equivalencia razonable (por ejemplo, ajo: ¿cabeza o diente?).
 * @property compra cómo se compra y cuánto cuesta como referencia; `null` si no hay datos.
 * @property basico condimento o básico (sal, aceite) que casi toda receta usa; no cuenta como
 *   "ingrediente compartido" al recomendar recetas.
 */
data class Ingrediente(
    val id: String,
    val nombre: String,
    val sinonimos: List<String> = emptyList(),
    val categoria: Categoria = Categoria.DESPENSA,
    val unidadSugerida: Unidad = Unidad.PIEZA,
    val ubicacionSugerida: Ubicacion = Ubicacion.DESPENSA,
    val gramosPorPieza: Double? = null,
    val compra: InfoCompra? = null,
    val basico: Boolean = false,
)

/**
 * Cómo se compra un ingrediente.
 *
 * @property unidad unidad en la que se compra.
 * @property presentacion tamaño mínimo de compra en [unidad]: 1 pieza, 100 g a granel o una bolsa
 *   de 1 000 g. Lo que falte se redondea hacia arriba a presentaciones completas.
 * @property empaque nombre del empaque ("bolsa de 1 kg"); `null` si se compra a granel o por pieza.
 * @property precio precio de referencia aproximado de una presentación, en MXN; `null` si se desconoce.
 */
data class InfoCompra(
    val unidad: Unidad,
    val presentacion: Double,
    val empaque: String? = null,
    val precio: Double? = null,
)

/** Catálogo de ingredientes. La implementación vive en :data. */
interface IngredienteRepository {
    fun todos(): List<Ingrediente>

    fun obtener(id: String): Ingrediente?
}
