package com.bakimi.ui.compras

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.bakimi.R
import com.bakimi.domain.compras.RenglonCompra
import com.bakimi.domain.compras.TotalCompra
import com.bakimi.ui.refri.textoCantidad
import java.text.NumberFormat

/** Número de un precio: sin decimales si es entero ("34"), con dos si no ("12.50"). */
fun formatearPrecio(valor: Double): String = NumberFormat.getNumberInstance().apply {
    val entero = valor % 1.0 == 0.0
    minimumFractionDigits = if (entero) 0 else 2
    maximumFractionDigits = if (entero) 0 else 2
}.format(valor)

/** "$34 aprox." para un estimado, "$34" para un precio real, "Sin precio" si no se conoce. */
@Composable
fun textoPrecio(precio: Double?, real: Boolean): String = when {
    precio == null -> stringResource(R.string.precio_sin_dato)
    real -> stringResource(R.string.precio, formatearPrecio(precio))
    else -> stringResource(R.string.precio_aprox, formatearPrecio(precio))
}

/** Total estimado; siempre aproximado porque mezcla precios de referencia. */
@Composable
fun textoTotal(total: TotalCompra): String = stringResource(R.string.precio_aprox, formatearPrecio(total.total))

/** "1 × bolsa de 1 kg", "3 piezas" o "500 g aprox.". */
@Composable
fun textoCantidadCompra(renglon: RenglonCompra): String {
    val presentaciones = renglon.presentaciones
    val base = if (presentaciones != null && renglon.empaque != null) {
        stringResource(R.string.compras_cantidad_presentacion, presentaciones, renglon.empaque!!)
    } else {
        textoCantidad(renglon.cantidad, renglon.unidad)
    }
    return if (renglon.aproximado) stringResource(R.string.compras_aprox, base) else base
}
