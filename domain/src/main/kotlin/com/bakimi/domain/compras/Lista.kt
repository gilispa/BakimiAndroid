package com.bakimi.domain.compras

import com.bakimi.domain.ingredientes.Categoria
import com.bakimi.domain.ingredientes.Ingrediente
import com.bakimi.domain.ingredientes.convertir
import com.bakimi.domain.inventario.Unidad
import com.bakimi.domain.inventario.redondear

/** Lo que el usuario marca o corrige de un artículo del plan. Se guarda por ingrediente. */
data class MarcaCompra(
    val ingredienteId: String,
    val comprado: Boolean = false,
    /** Cantidad real en la unidad de compra del artículo; `null` si no la cambió. */
    val cantidadReal: Double? = null,
    /** Precio real pagado en MXN; `null` si no lo anotó. */
    val precioReal: Double? = null,
)

/** Producto agregado a mano a la lista (leche, café, snacks…). */
data class ArticuloManual(
    val id: Long = 0,
    val ingredienteId: String?,
    val nombre: String,
    val cantidad: Double,
    val unidad: Unidad,
    val precio: Double?,
    val comprado: Boolean = false,
)

/**
 * Renglón de la lista de compras.
 *
 * @property clave identifica el renglón: "plan:<ingrediente>" o "manual:<id>".
 * @property categoria grupo de la lista; `null` para productos fuera del catálogo ("Otros").
 * @property precio precio real si se anotó, si no el estimado; `null` si no se conoce.
 * @property recetas recetas del plan que lo necesitan (vacío para productos manuales).
 */
data class RenglonCompra(
    val clave: String,
    val ingrediente: Ingrediente?,
    val nombre: String,
    val categoria: Categoria?,
    val cantidad: Double,
    val unidad: Unidad,
    val presentaciones: Int?,
    val empaque: String?,
    val precio: Double?,
    val precioReal: Boolean,
    val aproximado: Boolean,
    val comprado: Boolean,
    val recetas: List<String>,
) {
    val manual: Boolean get() = clave.startsWith(PREFIJO_MANUAL)
}

data class ListaCompras(
    val renglones: List<RenglonCompra>,
    val total: TotalCompra,
) {
    val marcados: List<RenglonCompra> get() = renglones.filter { it.comprado }
}

const val PREFIJO_PLAN = "plan:"
const val PREFIJO_MANUAL = "manual:"

/**
 * Junta los artículos del plan con las marcas del usuario y los productos manuales.
 *
 * Una cantidad real cambia el precio estimado en proporción; un precio real reemplaza al
 * estimado. Las marcas de artículos que ya no están en el plan se ignoran.
 */
fun armarLista(
    articulos: List<ArticuloCompra>,
    marcas: List<MarcaCompra>,
    manuales: List<ArticuloManual>,
    ingrediente: (String) -> Ingrediente?,
): ListaCompras {
    val marcasPorId = marcas.associateBy { it.ingredienteId }
    val delPlan = articulos.map { articulo ->
        val ing = articulo.necesidad.ingrediente
        val marca = marcasPorId[ing.id]
        val cantidadReal = marca?.cantidadReal
        val estimado = articulo.precio?.let { precio ->
            if (cantidadReal == null) precio else redondear(precio * cantidadReal / articulo.cantidad, decimales = 2)
        }
        RenglonCompra(
            clave = PREFIJO_PLAN + ing.id,
            ingrediente = ing,
            nombre = ing.nombre,
            categoria = ing.categoria,
            cantidad = cantidadReal ?: articulo.cantidad,
            unidad = articulo.unidad,
            presentaciones = if (cantidadReal == null) articulo.presentaciones else null,
            empaque = if (cantidadReal == null) articulo.empaque else null,
            precio = marca?.precioReal ?: estimado,
            precioReal = marca?.precioReal != null,
            aproximado = articulo.aproximado && cantidadReal == null,
            comprado = marca?.comprado == true,
            recetas = articulo.necesidad.recetas,
        )
    }
    val agregados = manuales.map { manual ->
        val ing = manual.ingredienteId?.let(ingrediente)
        RenglonCompra(
            clave = PREFIJO_MANUAL + manual.id,
            ingrediente = ing,
            nombre = ing?.nombre ?: manual.nombre,
            categoria = ing?.categoria,
            cantidad = manual.cantidad,
            unidad = manual.unidad,
            presentaciones = null,
            empaque = null,
            precio = manual.precio ?: ing?.let { precioDeReferencia(it, manual.cantidad, manual.unidad) },
            precioReal = manual.precio != null,
            aproximado = false,
            comprado = manual.comprado,
            recetas = emptyList(),
        )
    }
    val renglones = delPlan + agregados
    return ListaCompras(renglones, totalDe(renglones.map { it.precio }))
}

/** Precio de referencia proporcional a la cantidad; `null` si no hay precio o no se puede convertir. */
private fun precioDeReferencia(ingrediente: Ingrediente, cantidad: Double, unidad: Unidad): Double? {
    val info = ingrediente.compra ?: return null
    val precio = info.precio ?: return null
    val enUnidadDeCompra = ingrediente.convertir(cantidad, unidad, info.unidad) ?: return null
    return redondear(precio * enUnidadDeCompra.cantidad / info.presentacion, decimales = 2)
}

/** Productos marcados como comprados, con la cantidad real, listos para incorporar a Mi refri. */
fun ListaCompras.comprados(): List<Comprado> =
    marcados.map { Comprado(it.ingrediente, it.nombre, it.cantidad, it.unidad) }
