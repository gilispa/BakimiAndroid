package com.bakimi.ui.recipes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.ShoppingBasket
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bakimi.R
import com.bakimi.domain.recipes.RecipeEvaluation

@Composable
fun availabilityLabel(evaluation: RecipeEvaluation): String =
    if (evaluation.isReady) {
        stringResource(R.string.recipe_ready)
    } else {
        pluralStringResource(R.plurals.recipe_missing, evaluation.missing, evaluation.missing)
    }

/** Availability tag: icon and text in addition to color, so it does not depend on color alone. */
@Composable
fun AvailabilityTag(evaluation: RecipeEvaluation, modifier: Modifier = Modifier) {
    val isReady = evaluation.isReady
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = if (isReady) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary,
        contentColor = if (isReady) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onTertiary,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                if (isReady) Icons.Filled.CheckCircle else Icons.Outlined.ShoppingBasket,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
            Text(availabilityLabel(evaluation), style = MaterialTheme.typography.labelMedium)
        }
    }
}
