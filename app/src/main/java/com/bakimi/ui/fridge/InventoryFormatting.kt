package com.bakimi.ui.fridge

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.bakimi.R
import com.bakimi.domain.ingredients.Category
import com.bakimi.domain.inventory.StorageLocation
import com.bakimi.domain.inventory.MeasureUnit
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

fun formatNumber(value: Double): String =
    NumberFormat.getNumberInstance().apply { maximumFractionDigits = 3 }.format(value)

/**
 * Text for an editable field: no thousands separator and a decimal point ("1500", "1.5"), so
 * [com.bakimi.domain.inventory.parseQuantity] reads it without ambiguity.
 */
fun editableText(value: Double): String =
    BigDecimal.valueOf(value).stripTrailingZeros().toPlainString()

fun formatDate(date: LocalDate): String =
    date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))

/** Quantity with its unit, for example "1,5 kg" or "12 piezas". */
@Composable
fun quantityLabel(quantity: Double, unit: MeasureUnit): String {
    val number = formatNumber(quantity)
    return when (unit) {
        MeasureUnit.GRAM -> stringResource(R.string.quantity_gram, number)
        MeasureUnit.KILOGRAM -> stringResource(R.string.quantity_kilogram, number)
        MeasureUnit.MILLILITER -> stringResource(R.string.quantity_milliliter, number)
        MeasureUnit.LITER -> stringResource(R.string.quantity_liter, number)
        MeasureUnit.PIECE -> pluralStringResource(R.plurals.quantity_piece, if (quantity == 1.0) 1 else 2, number)
    }
}

@get:StringRes
val MeasureUnit.label: Int
    get() = when (this) {
        MeasureUnit.GRAM -> R.string.unit_gram
        MeasureUnit.KILOGRAM -> R.string.unit_kilogram
        MeasureUnit.MILLILITER -> R.string.unit_milliliter
        MeasureUnit.LITER -> R.string.unit_liter
        MeasureUnit.PIECE -> R.string.unit_piece
    }

@get:StringRes
val Category.label: Int
    get() = when (this) {
        Category.PRODUCE -> R.string.category_produce
        Category.MEAT -> R.string.category_meat
        Category.DAIRY_EGGS -> R.string.category_dairy_eggs
        Category.PANTRY -> R.string.category_pantry
    }

@get:StringRes
val StorageLocation.label: Int
    get() = when (this) {
        StorageLocation.FRIDGE -> R.string.location_fridge
        StorageLocation.FREEZER -> R.string.location_freezer
        StorageLocation.PANTRY -> R.string.location_pantry
    }
