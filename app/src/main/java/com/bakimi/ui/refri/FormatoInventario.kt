package com.bakimi.ui.refri

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.bakimi.R
import com.bakimi.domain.ingredientes.Categoria
import com.bakimi.domain.inventario.Ubicacion
import com.bakimi.domain.inventario.Unidad
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

fun formatearNumero(valor: Double): String =
    NumberFormat.getNumberInstance().apply { maximumFractionDigits = 3 }.format(valor)

/**
 * Texto para un campo editable: sin separador de miles y con punto decimal ("1500", "1.5"),
 * para que [com.bakimi.domain.inventario.parsearCantidad] lo lea sin ambigüedad.
 */
fun textoEditable(valor: Double): String =
    BigDecimal.valueOf(valor).stripTrailingZeros().toPlainString()

fun formatearFecha(fecha: LocalDate): String =
    fecha.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))

/** Cantidad con su unidad, por ejemplo "1,5 kg" o "12 piezas". */
@Composable
fun textoCantidad(cantidad: Double, unidad: Unidad): String {
    val numero = formatearNumero(cantidad)
    return when (unidad) {
        Unidad.GRAMO -> stringResource(R.string.cantidad_gramo, numero)
        Unidad.KILOGRAMO -> stringResource(R.string.cantidad_kilogramo, numero)
        Unidad.MILILITRO -> stringResource(R.string.cantidad_mililitro, numero)
        Unidad.LITRO -> stringResource(R.string.cantidad_litro, numero)
        Unidad.PIEZA -> pluralStringResource(R.plurals.cantidad_pieza, if (cantidad == 1.0) 1 else 2, numero)
    }
}

@get:StringRes
val Unidad.etiqueta: Int
    get() = when (this) {
        Unidad.GRAMO -> R.string.unidad_gramo
        Unidad.KILOGRAMO -> R.string.unidad_kilogramo
        Unidad.MILILITRO -> R.string.unidad_mililitro
        Unidad.LITRO -> R.string.unidad_litro
        Unidad.PIEZA -> R.string.unidad_pieza
    }

@get:StringRes
val Categoria.etiqueta: Int
    get() = when (this) {
        Categoria.FRUTAS_VERDURAS -> R.string.categoria_frutas_verduras
        Categoria.CARNES -> R.string.categoria_carnes
        Categoria.LACTEOS_HUEVO -> R.string.categoria_lacteos_huevo
        Categoria.DESPENSA -> R.string.categoria_despensa
    }

@get:StringRes
val Ubicacion.etiqueta: Int
    get() = when (this) {
        Ubicacion.REFRIGERADOR -> R.string.ubicacion_refrigerador
        Ubicacion.CONGELADOR -> R.string.ubicacion_congelador
        Ubicacion.DESPENSA -> R.string.ubicacion_despensa
    }
