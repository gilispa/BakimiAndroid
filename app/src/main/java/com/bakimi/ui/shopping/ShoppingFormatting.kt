package com.bakimi.ui.shopping

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.bakimi.R
import com.bakimi.domain.shopping.ShoppingLine
import com.bakimi.domain.shopping.PurchaseTotal
import com.bakimi.ui.fridge.quantityLabel
import java.text.NumberFormat

/** Price number: no decimals if whole ("34"), two otherwise ("12.50"). */
fun formatPrice(value: Double): String = NumberFormat.getNumberInstance().apply {
    val whole = value % 1.0 == 0.0
    minimumFractionDigits = if (whole) 0 else 2
    maximumFractionDigits = if (whole) 0 else 2
}.format(value)

/** "$34 approx." for an estimate, "$34" for an actual price, "Sin precio" if unknown. */
@Composable
fun priceLabel(price: Double?, real: Boolean): String = when {
    price == null -> stringResource(R.string.price_unknown)
    real -> stringResource(R.string.price, formatPrice(price))
    else -> stringResource(R.string.price_approx, formatPrice(price))
}

/** Estimated total; always approximate because it mixes reference prices. */
@Composable
fun totalLabel(total: PurchaseTotal): String = stringResource(R.string.price_approx, formatPrice(total.total))

/** "1 × bolsa de 1 kg", "3 piezas" or "500 g aprox.". */
@Composable
fun purchaseQuantityLabel(line: ShoppingLine): String {
    val packages = line.packages
    val base = if (packages != null && line.packageName != null) {
        stringResource(R.string.shopping_quantity_package, packages, line.packageName!!)
    } else {
        quantityLabel(line.quantity, line.unit)
    }
    return if (line.approximate) stringResource(R.string.shopping_approx, base) else base
}
