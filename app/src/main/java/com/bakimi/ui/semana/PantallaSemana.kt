package com.bakimi.ui.semana

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bakimi.R
import com.bakimi.domain.compras.EstadoPresupuesto
import com.bakimi.domain.compras.GestorCompras.Companion.MAX_PORCIONES
import com.bakimi.domain.compras.GestorCompras.Companion.MIN_PORCIONES
import com.bakimi.domain.compras.RecetaPlaneada
import com.bakimi.domain.compras.Recomendacion
import com.bakimi.domain.compras.Semana
import com.bakimi.domain.compras.SugerenciaReceta
import com.bakimi.ui.compras.formatearPrecio
import com.bakimi.ui.compras.textoTotal
import com.bakimi.ui.recetas.EtiquetaDisponibilidad
import com.bakimi.ui.recetas.ImagenReceta
import com.bakimi.ui.refri.textoEditable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaSemana(
    onAbrirReceta: (String) -> Unit,
    onVerLista: () -> Unit,
    viewModel: SemanaViewModel = hiltViewModel(),
) {
    val semana by viewModel.estado.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val contexto = LocalContext.current
    LaunchedEffect(viewModel) {
        viewModel.errores.collect { snackbar.showSnackbar(contexto.getString(R.string.compras_error)) }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_semana)) },
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        bottomBar = {
            semana?.takeIf { it.recetas.isNotEmpty() }?.let { BarraResumen(it, onVerLista) }
        },
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        val actual = semana
        if (actual == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Contenido(actual, padding, viewModel, onAbrirReceta)
        }
    }
}

@Composable
private fun Contenido(
    semana: Semana,
    padding: PaddingValues,
    viewModel: SemanaViewModel,
    onAbrirReceta: (String) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(
            top = padding.calculateTopPadding(),
            bottom = padding.calculateBottomPadding() + 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                stringResource(R.string.semana_lema),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
        item { Configuracion(semana, viewModel) }

        titulo(R.string.semana_tu_semana)
        if (semana.recetas.isEmpty()) {
            item {
                Text(
                    stringResource(R.string.semana_vacia),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        } else {
            items(semana.recetas, key = { "plan_" + it.receta.id }) { planeada ->
                FilaPlaneada(
                    planeada = planeada,
                    onAbrir = { onAbrirReceta(planeada.receta.id) },
                    onPorciones = { viewModel.cambiarPorciones(planeada.receta.id, it) },
                    onQuitar = { viewModel.quitar(planeada.receta.id) },
                )
            }
        }

        if (semana.recomendaciones.isNotEmpty()) {
            titulo(R.string.semana_aprovecha)
            items(semana.recomendaciones, key = { "rec_" + it.receta.id }) { recomendacion ->
                TarjetaRecomendacion(
                    recomendacion,
                    onAbrir = { onAbrirReceta(recomendacion.receta.id) },
                    onAgregar = { viewModel.agregar(recomendacion.receta.id) },
                )
            }
        }

        if (semana.sugerencias.isNotEmpty()) {
            titulo(R.string.semana_agrega)
            items(semana.sugerencias, key = { "sug_" + it.evaluacion.receta.id }) { sugerencia ->
                TarjetaSugerencia(
                    sugerencia,
                    onAbrir = { onAbrirReceta(sugerencia.evaluacion.receta.id) },
                    onAgregar = { viewModel.agregar(sugerencia.evaluacion.receta.id) },
                )
            }
        }
    }
}

private fun LazyListScope.titulo(texto: Int) {
    item(key = "titulo_$texto") {
        Text(
            stringResource(texto),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 12.dp)
                .semantics { heading() },
        )
    }
}

@Composable
private fun Configuracion(semana: Semana, viewModel: SemanaViewModel) {
    // El campo conserva lo que el usuario escribe aunque todavía no sea un número válido.
    var presupuesto by rememberSaveable { mutableStateOf(semana.config.presupuesto?.let(::textoEditable).orEmpty()) }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Stepper(
                etiqueta = stringResource(R.string.semana_personas),
                valor = semana.config.personas,
                descripcionMenos = stringResource(R.string.semana_menos_personas),
                descripcionMas = stringResource(R.string.semana_mas_personas),
                onCambiar = viewModel::cambiarPersonas,
            )
            OutlinedTextField(
                value = presupuesto,
                onValueChange = {
                    presupuesto = it
                    viewModel.cambiarPresupuesto(it)
                },
                label = { Text(stringResource(R.string.semana_presupuesto)) },
                prefix = { Text("$") },
                supportingText = { Text(stringResource(R.string.semana_presupuesto_ayuda)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun Stepper(
    etiqueta: String,
    valor: Int,
    descripcionMenos: String,
    descripcionMas: String,
    onCambiar: (Int) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(etiqueta, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
        IconButton(onClick = { onCambiar(-1) }, enabled = valor > MIN_PORCIONES) {
            Icon(Icons.Default.Remove, descripcionMenos)
        }
        Text(valor.toString(), style = MaterialTheme.typography.titleMedium, modifier = Modifier.widthIn(min = 24.dp))
        IconButton(onClick = { onCambiar(1) }, enabled = valor < MAX_PORCIONES) {
            Icon(Icons.Default.Add, descripcionMas)
        }
    }
}

@Composable
private fun FilaPlaneada(
    planeada: RecetaPlaneada,
    onAbrir: () -> Unit,
    onPorciones: (Int) -> Unit,
    onQuitar: () -> Unit,
) {
    val nombre = planeada.receta.nombre
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ImagenReceta(
            planeada.receta.id,
            Modifier.size(56.dp).clip(MaterialTheme.shapes.medium).clickable(onClick = onAbrir),
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f).clickable(onClick = onAbrir)) {
            Text(nombre, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(
                stringResource(R.string.detalle_porciones_valor, planeada.porciones),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = { onPorciones(planeada.porciones - 1) }, enabled = planeada.porciones > MIN_PORCIONES) {
            Icon(Icons.Default.Remove, stringResource(R.string.semana_menos_porciones, nombre))
        }
        IconButton(onClick = { onPorciones(planeada.porciones + 1) }, enabled = planeada.porciones < MAX_PORCIONES) {
            Icon(Icons.Default.Add, stringResource(R.string.semana_mas_porciones, nombre))
        }
        IconButton(onClick = onQuitar) {
            Icon(Icons.Default.Close, stringResource(R.string.semana_quitar, nombre))
        }
    }
}

@Composable
private fun TarjetaRecomendacion(recomendacion: Recomendacion, onAbrir: () -> Unit, onAgregar: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            ImagenReceta(
                recomendacion.receta.id,
                Modifier.size(56.dp).clip(MaterialTheme.shapes.medium).clickable(onClick = onAbrir),
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f).clickable(onClick = onAbrir)) {
                Text(recomendacion.receta.nombre, style = MaterialTheme.typography.titleSmall)
                Text(
                    stringResource(
                        R.string.semana_usa,
                        recomendacion.compartidos.joinToString(", ") { it.nombre.lowercase() },
                    ),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            FilledTonalButton(onClick = onAgregar) { Text(stringResource(R.string.semana_agregar)) }
        }
    }
}

@Composable
private fun TarjetaSugerencia(sugerencia: SugerenciaReceta, onAbrir: () -> Unit, onAgregar: () -> Unit) {
    val evaluacion = sugerencia.evaluacion
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(Modifier.padding(12.dp)) {
            ImagenReceta(
                evaluacion.receta.id,
                Modifier.size(88.dp).clip(MaterialTheme.shapes.medium).clickable(onClick = onAbrir),
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Column(Modifier.clickable(onClick = onAbrir)) {
                    Text(evaluacion.receta.nombre, style = MaterialTheme.typography.titleSmall)
                    Text(
                        stringResource(R.string.semana_tiempo_porciones, evaluacion.receta.minutos, evaluacion.porciones),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                EtiquetaDisponibilidad(evaluacion)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        textoCompraFaltante(sugerencia),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                    )
                    FilledTonalButton(onClick = onAgregar) { Text(stringResource(R.string.semana_agregar)) }
                }
            }
        }
    }
}

/** La tarjeta muestra lo que costaría comprar lo que le falta a esa receta con el inventario actual. */
@Composable
private fun textoCompraFaltante(sugerencia: SugerenciaReceta): String {
    val total = sugerencia.compraFaltante
    return when {
        sugerencia.evaluacion.lista -> stringResource(R.string.semana_sin_compra)
        total.total == 0.0 && total.parcial -> stringResource(R.string.precio_sin_dato)
        else -> stringResource(R.string.semana_compra_faltante, textoTotal(total))
    }
}

@Composable
private fun BarraResumen(semana: Semana, onVerLista: () -> Unit) {
    val total = semana.lista.total
    val presupuesto = semana.config.presupuesto
    val comidas = pluralStringResource(R.plurals.semana_comidas, semana.recetas.size, semana.recetas.size)
    val compra = if (presupuesto == null) {
        stringResource(R.string.semana_compra_estimada, textoTotal(total))
    } else {
        stringResource(
            R.string.semana_compra_vs_presupuesto,
            textoTotal(total),
            stringResource(R.string.precio, formatearPrecio(presupuesto)),
        )
    }
    val (aviso, colorAviso) = when (semana.presupuesto) {
        EstadoPresupuesto.EXCEDIDO -> stringResource(R.string.semana_presupuesto_excedido) to MaterialTheme.colorScheme.error
        EstadoPresupuesto.INCIERTO -> stringResource(R.string.semana_presupuesto_incierto) to MaterialTheme.colorScheme.onSurfaceVariant
        EstadoPresupuesto.DENTRO -> stringResource(R.string.semana_presupuesto_dentro) to MaterialTheme.colorScheme.onSurfaceVariant
        EstadoPresupuesto.SIN_PRESUPUESTO ->
            (if (total.parcial) stringResource(R.string.semana_faltan_precios) else null) to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("$comidas · $compra", style = MaterialTheme.typography.titleSmall)
            aviso?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = colorAviso) }
            Button(onClick = onVerLista, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Outlined.ShoppingCart, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.semana_ver_lista))
            }
        }
    }
}
