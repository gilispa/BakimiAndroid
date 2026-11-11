package com.bakimi.ui.recetas

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.RemoveCircleOutline
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bakimi.R
import com.bakimi.domain.recetas.CreditoFoto
import com.bakimi.domain.recetas.EstadoIngrediente
import com.bakimi.domain.recetas.EvaluacionReceta
import com.bakimi.domain.recetas.IngredienteEvaluado
import com.bakimi.domain.recetas.Paso
import com.bakimi.ui.recetas.DetalleRecetaViewModel.Companion.MAX_PORCIONES
import com.bakimi.ui.recetas.DetalleRecetaViewModel.Companion.MIN_PORCIONES
import com.bakimi.ui.refri.formatearNumero
import com.bakimi.ui.refri.textoCantidad

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDetalleReceta(
    onVolver: () -> Unit,
    onIniciarCocina: (recetaId: String, porciones: Int) -> Unit,
    viewModel: DetalleRecetaViewModel = hiltViewModel(),
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    (estado as? DetalleUiState.Listo)?.let { Text(it.evaluacion.receta.nombre, maxLines = 1) }
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.form_volver))
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (val actual = estado) {
                DetalleUiState.Cargando -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                DetalleUiState.NoEncontrada -> Text(
                    stringResource(R.string.detalle_no_encontrada),
                    modifier = Modifier.align(Alignment.Center).padding(32.dp),
                )

                is DetalleUiState.Listo -> Detalle(
                    evaluacion = actual.evaluacion,
                    porcionesEnSemana = actual.porcionesEnSemana,
                    onCambiarPorciones = viewModel::cambiarPorciones,
                    onGuardarEnSemana = viewModel::guardarEnSemana,
                    onQuitarDeSemana = viewModel::quitarDeSemana,
                    onIniciarCocina = {
                        onIniciarCocina(actual.evaluacion.receta.id, actual.evaluacion.porciones)
                    },
                )
            }
        }
    }
}

@Composable
private fun Detalle(
    evaluacion: EvaluacionReceta,
    porcionesEnSemana: Int?,
    onCambiarPorciones: (Int) -> Unit,
    onGuardarEnSemana: () -> Unit,
    onQuitarDeSemana: () -> Unit,
    onIniciarCocina: () -> Unit,
) {
    val receta = evaluacion.receta
    LazyColumn(contentPadding = PaddingValues(bottom = 32.dp)) {
        item {
            ImagenReceta(receta.id, Modifier.fillMaxWidth().aspectRatio(3f / 2f))
            CreditoDeFoto(receta.creditoFoto)
        }
        item {
            Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    receta.nombre,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.semantics { heading() },
                )
                Text(receta.descripcion, style = MaterialTheme.typography.bodyLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    Dato(
                        titulo = null,
                        icono = { Icon(Icons.Outlined.Schedule, null, Modifier.size(18.dp)) },
                        valor = stringResource(R.string.receta_minutos_accesible, receta.minutos),
                    )
                    Dato(
                        titulo = stringResource(R.string.detalle_proteina),
                        icono = null,
                        valor = receta.proteinaPorPorcionG
                            ?.let { stringResource(R.string.detalle_proteina_valor, formatearNumero(it)) }
                            ?: stringResource(R.string.detalle_sin_dato),
                    )
                }
                SelectorPorciones(evaluacion.porciones, onCambiarPorciones)
                EtiquetaDisponibilidad(evaluacion)
                // Iniciar o abandonar el Modo Cocina no descuenta nada; solo la confirmación final.
                Button(onClick = onIniciarCocina, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                    Icon(Icons.Outlined.Restaurant, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.detalle_iniciar_cocina))
                }
                AccionSemana(
                    porciones = evaluacion.porciones,
                    porcionesEnSemana = porcionesEnSemana,
                    onGuardar = onGuardarEnSemana,
                    onQuitar = onQuitarDeSemana,
                )
            }
        }
        item { Titulo(R.string.detalle_ingredientes) }
        itemsIndexed(evaluacion.ingredientes, key = { _, it -> it.requerido.ingrediente.id }) { indice, ingrediente ->
            FilaIngrediente(ingrediente)
            if (indice < evaluacion.ingredientes.lastIndex) HorizontalDivider(Modifier.padding(start = 56.dp))
        }
        item { Titulo(R.string.detalle_preparacion) }
        itemsIndexed(receta.pasos) { indice, paso -> FilaPaso(indice + 1, paso) }
    }
}

@Composable
private fun CreditoDeFoto(credito: CreditoFoto) {
    val uri = LocalUriHandler.current
    Text(
        stringResource(R.string.receta_credito_foto, credito.autor),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textDecoration = TextDecoration.Underline,
        modifier = Modifier
            .clickable(role = Role.Button) { uri.openUri(credito.urlFoto) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
    )
}

@Composable
private fun Dato(titulo: String?, icono: (@Composable () -> Unit)?, valor: String) {
    Column {
        if (titulo != null) {
            Text(titulo, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icono != null) {
                icono()
                Spacer(Modifier.width(4.dp))
            }
            Text(valor, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun SelectorPorciones(porciones: Int, onCambiar: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(stringResource(R.string.detalle_porciones), style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
        FilledTonalIconButton(onClick = { onCambiar(-1) }, enabled = porciones > MIN_PORCIONES) {
            Icon(Icons.Default.Remove, stringResource(R.string.detalle_menos_porciones))
        }
        Text(
            stringResource(R.string.detalle_porciones_valor, porciones),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.widthIn(min = 96.dp).padding(horizontal = 8.dp),
            textAlign = TextAlign.Center,
        )
        FilledTonalIconButton(onClick = { onCambiar(1) }, enabled = porciones < MAX_PORCIONES) {
            Icon(Icons.Default.Add, stringResource(R.string.detalle_mas_porciones))
        }
    }
}

@Composable
private fun AccionSemana(porciones: Int, porcionesEnSemana: Int?, onGuardar: () -> Unit, onQuitar: () -> Unit) {
    if (porcionesEnSemana == null) {
        Button(onClick = onGuardar, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.detalle_agregar_semana))
        }
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.detalle_en_semana, porcionesEnSemana), style = MaterialTheme.typography.bodyMedium)
        }
        if (porciones != porcionesEnSemana) {
            Button(onClick = onGuardar, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.detalle_actualizar_semana, porciones))
            }
        }
        OutlinedButton(onClick = onQuitar, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.detalle_quitar_semana))
        }
    }
}

@Composable
private fun Titulo(@StringRes texto: Int) {
    Text(
        stringResource(texto),
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, top = 28.dp, bottom = 8.dp)
            .semantics { heading() },
    )
}

@Composable
private fun FilaIngrediente(item: IngredienteEvaluado) {
    val requerido = item.requerido
    val (icono, color, estado) = when (item.estado) {
        EstadoIngrediente.COMPLETO -> Triple(
            Icons.Filled.CheckCircle,
            MaterialTheme.colorScheme.onSurface,
            if (item.aproximado) {
                stringResource(R.string.detalle_tienes_aprox, textoCantidad(item.disponible, requerido.unidad))
            } else {
                stringResource(R.string.detalle_tienes)
            },
        )
        EstadoIngrediente.INSUFICIENTE -> Triple(
            Icons.Outlined.RemoveCircleOutline,
            MaterialTheme.colorScheme.error,
            stringResource(
                if (item.aproximado) R.string.detalle_te_falta_aprox else R.string.detalle_te_falta,
                textoCantidad(item.faltante, requerido.unidad),
            ),
        )
        EstadoIngrediente.FALTA -> Triple(
            Icons.Outlined.Cancel,
            MaterialTheme.colorScheme.error,
            stringResource(R.string.detalle_no_tienes),
        )
        EstadoIngrediente.OTRA_UNIDAD -> Triple(
            Icons.AutoMirrored.Outlined.HelpOutline,
            MaterialTheme.colorScheme.onSurfaceVariant,
            stringResource(R.string.detalle_otra_unidad),
        )
    }
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp).semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icono, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(requerido.ingrediente.nombre, style = MaterialTheme.typography.bodyLarge)
            Text(estado, style = MaterialTheme.typography.bodyMedium, color = color)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(textoCantidad(requerido.cantidad, requerido.unidad), style = MaterialTheme.typography.bodyLarge)
            requerido.nota?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun FilaPaso(numero: Int, paso: Paso) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    numero.toString(),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(paso.instruccion, style = MaterialTheme.typography.bodyLarge)
            paso.minutos?.let {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Schedule,
                        null,
                        Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        stringResource(R.string.detalle_paso_minutos, it),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
