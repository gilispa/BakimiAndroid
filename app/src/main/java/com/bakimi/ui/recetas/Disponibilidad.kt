package com.bakimi.ui.recetas

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
import com.bakimi.domain.recetas.EvaluacionReceta

@Composable
fun textoDisponibilidad(evaluacion: EvaluacionReceta): String =
    if (evaluacion.lista) {
        stringResource(R.string.receta_lista)
    } else {
        pluralStringResource(R.plurals.receta_faltan, evaluacion.faltantes, evaluacion.faltantes)
    }

/** Etiqueta de disponibilidad: ícono y texto, además del color, para no depender solo de él. */
@Composable
fun EtiquetaDisponibilidad(evaluacion: EvaluacionReceta, modifier: Modifier = Modifier) {
    val lista = evaluacion.lista
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = if (lista) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary,
        contentColor = if (lista) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onTertiary,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                if (lista) Icons.Filled.CheckCircle else Icons.Outlined.ShoppingBasket,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
            Text(textoDisponibilidad(evaluacion), style = MaterialTheme.typography.labelMedium)
        }
    }
}
