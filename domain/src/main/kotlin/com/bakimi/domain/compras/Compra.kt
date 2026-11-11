package com.bakimi.domain.compras

import com.bakimi.domain.ingredientes.convertir
import com.bakimi.domain.inventario.ItemInventario
import com.bakimi.domain.inventario.Unidad
import com.bakimi.domain.inventario.redondear
import kotlin.math.ceil

/**
 * Lo que hay que comprar de un ingrediente, en presentaciones completas.
 *
 * @property cantidad total a comprar en [unidad] (por ejemplo, 1 000 g aunque falten 200 g).
 * @property presentaciones número de presentaciones (bolsas, piezas, porciones de 100 g).
 * @property precio desembolso estimado en MXN; `null` si no hay precio de referencia.
 * @property aproximado la cantidad salió de un peso aproximado por pieza.
 */
data class ArticuloCompra(
    val necesidad: Necesidad,
    val unidad: Unidad,
    val cantidad: Double,
    val presentaciones: Int,
    val empaque: String?,
    val precio: Double?,
    val aproximado: Boolean,
)

/** Calcula la compra de una necesidad; `null` si no falta nada. */
fun calcularCompra(necesidad: Necesidad): ArticuloCompra? {
    if (necesidad.faltante <= 0.0) return null
    val info = necesidad.ingrediente.compra
    val conversion = info?.let { necesidad.ingrediente.convertir(necesidad.faltante, necesidad.unidad, it.unidad) }

    if (info == null || conversion == null) {
        // Sin datos de compra: se compra lo que falta; las piezas, completas.
        val porPieza = necesidad.unidad == Unidad.PIEZA
        val cantidad = if (porPieza) ceil(necesidad.faltante - TOLERANCIA) else necesidad.faltante
        return ArticuloCompra(
            necesidad = necesidad,
            unidad = necesidad.unidad,
            cantidad = cantidad,
            presentaciones = if (porPieza) cantidad.toInt() else 1,
            empaque = null,
            precio = null,
            aproximado = necesidad.aproximado,
        )
    }

    val presentaciones = ceil(conversion.cantidad / info.presentacion - TOLERANCIA).toInt().coerceAtLeast(1)
    return ArticuloCompra(
        necesidad = necesidad,
        unidad = info.unidad,
        cantidad = redondear(presentaciones * info.presentacion, decimales = 3),
        presentaciones = presentaciones,
        empaque = info.empaque,
        precio = info.precio?.let { redondear(it * presentaciones, decimales = 2) },
        aproximado = necesidad.aproximado || conversion.aproximada,
    )
}

/** Evita comprar una presentación de más por residuos de punto flotante (2.0000001 bolsas). */
private const val TOLERANCIA = 1e-6

/** Lista de compras del plan: solo los ingredientes con faltante. */
fun listaDeCompra(plan: List<RecetaPlaneada>, inventario: List<ItemInventario>): List<ArticuloCompra> =
    consolidarPlan(plan, inventario).mapNotNull(::calcularCompra)

/**
 * @property total suma de los precios conocidos.
 * @property parcial algún artículo no tiene precio; el total no lo incluye.
 */
data class TotalCompra(val total: Double, val parcial: Boolean)

/** Suma los precios. Un precio desconocido nunca cuenta como cero: marca el total como parcial. */
fun totalDe(precios: List<Double?>): TotalCompra =
    TotalCompra(total = redondear(precios.filterNotNull().sum(), decimales = 2), parcial = precios.any { it == null })

enum class EstadoPresupuesto {
    SIN_PRESUPUESTO,

    /** Todos los precios son conocidos y el total cabe. */
    DENTRO,

    /** El total conocido ya rebasa el presupuesto. */
    EXCEDIDO,

    /** Lo conocido cabe, pero faltan precios: no se puede asegurar. */
    INCIERTO,
}

fun estadoPresupuesto(total: TotalCompra, presupuesto: Double?): EstadoPresupuesto = when {
    presupuesto == null -> EstadoPresupuesto.SIN_PRESUPUESTO
    total.total > presupuesto -> EstadoPresupuesto.EXCEDIDO
    total.parcial -> EstadoPresupuesto.INCIERTO
    else -> EstadoPresupuesto.DENTRO
}
