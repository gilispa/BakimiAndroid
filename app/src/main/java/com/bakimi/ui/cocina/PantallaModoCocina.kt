package com.bakimi.ui.cocina

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bakimi.R
import com.bakimi.domain.compras.GestorCompras
import com.bakimi.ui.refri.etiqueta
import com.bakimi.ui.refri.textoCantidad
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaModoCocina(
    onSalir: () -> Unit,
    viewModel: ModoCocinaViewModel = hiltViewModel(),
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val actual = estado ?: run {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.detalle_no_encontrada))
        }
        return
    }

    // Con las manos ocupadas, la pantalla no debe apagarse mientras se cocina.
    val vista = LocalView.current
    DisposableEffect(vista) {
        vista.keepScreenOn = true
        onDispose { vista.keepScreenOn = false }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(actual.receta.nombre, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onSalir) { Icon(Icons.Default.Close, stringResource(R.string.cocina_salir)) }
                },
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        bottomBar = {
            if (actual.fase == Fase.COCINANDO) {
                Navegacion(
                    puedeRegresar = actual.paso > 0,
                    esUltimo = actual.esUltimoPaso,
                    onAnterior = viewModel::anterior,
                    onSiguiente = viewModel::siguiente,
                    onTerminar = viewModel::terminarDeCocinar,
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (actual.fase) {
                Fase.COCINANDO -> PasoActual(actual)
                Fase.CONFIRMANDO -> Confirmacion(actual, viewModel, onSalir)
                Fase.TERMINADO -> Terminado(actual.yaRegistrada, onSalir)
            }
        }
    }
}

@Composable
private fun PasoActual(estado: ModoCocinaUiState) {
    val paso = estado.receta.pasos[estado.paso]
    var verIngredientes by rememberSaveable { mutableStateOf(false) }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            stringResource(R.string.cocina_progreso, estado.paso + 1, estado.totalPasos),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            // Se anuncia al cambiar de paso para quien usa lector de pantalla.
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        )
        LinearProgressIndicator(
            progress = { (estado.paso + 1f) / estado.totalPasos },
            modifier = Modifier.fillMaxWidth(),
        )
        paso.imagen?.let { ImagenPaso(it, numero = estado.paso + 1) }
        Text(
            paso.instruccion,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.semantics { heading() },
        )
        paso.minutos?.let { minutos ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Schedule, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.detalle_paso_minutos, minutos), style = MaterialTheme.typography.titleMedium)
            }
        }
        TextButton(onClick = { verIngredientes = true }) { Text(stringResource(R.string.cocina_ver_ingredientes)) }
    }

    if (verIngredientes) {
        val factor = estado.porciones.toDouble() / estado.receta.porcionesBase
        AlertDialog(
            onDismissRequest = { verIngredientes = false },
            title = { Text(stringResource(R.string.cocina_ingredientes_titulo, estado.porciones)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    estado.receta.ingredientes.forEach {
                        Text("${it.ingrediente.nombre}: ${textoCantidad(it.cantidad * factor, it.unidad)}")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { verIngredientes = false }) { Text(stringResource(R.string.cocina_cerrar)) }
            },
        )
    }
}

private sealed interface CargaImagen {
    data object Cargando : CargaImagen
    data class Lista(val imagen: ImageBitmap) : CargaImagen
    data object NoDisponible : CargaImagen
}

/**
 * Imagen de referencia del paso, leída de assets fuera del hilo principal.
 *
 * Las imágenes y videos son opcionales: si el archivo no existe o no se puede leer, el paso se
 * muestra solo con su texto, igual que un paso sin imagen.
 */
@Composable
private fun ImagenPaso(ruta: String, numero: Int) {
    val contexto = LocalContext.current
    val carga by produceState<CargaImagen>(initialValue = CargaImagen.Cargando, ruta) {
        value = withContext(Dispatchers.IO) {
            runCatching { contexto.assets.open(ruta).use { BitmapFactory.decodeStream(it) } }
                .getOrNull()
                ?.let { CargaImagen.Lista(it.asImageBitmap()) }
                ?: CargaImagen.NoDisponible
        }
    }
    val modificador = Modifier.fillMaxWidth().aspectRatio(4f / 3f).clip(MaterialTheme.shapes.large)
    when (val actual = carga) {
        // Reserva el espacio mientras carga para que el texto no salte.
        CargaImagen.Cargando -> Surface(modificador, color = MaterialTheme.colorScheme.surfaceVariant) {}
        is CargaImagen.Lista -> Image(
            bitmap = actual.imagen,
            contentDescription = stringResource(R.string.cocina_imagen_paso, numero),
            contentScale = ContentScale.Crop,
            modifier = modificador,
        )
        CargaImagen.NoDisponible -> Unit
    }
}

@Composable
private fun Navegacion(
    puedeRegresar: Boolean,
    esUltimo: Boolean,
    onAnterior: () -> Unit,
    onSiguiente: () -> Unit,
    onTerminar: () -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onAnterior, enabled = puedeRegresar, modifier = Modifier.weight(1f).height(56.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.cocina_anterior))
            }
            if (esUltimo) {
                Button(onClick = onTerminar, modifier = Modifier.weight(1f).height(56.dp)) {
                    Text(stringResource(R.string.cocina_termine), textAlign = TextAlign.Center)
                }
            } else {
                Button(onClick = onSiguiente, modifier = Modifier.weight(1f).height(56.dp)) {
                    Text(stringResource(R.string.cocina_siguiente))
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                }
            }
        }
    }
}

@Composable
private fun Confirmacion(estado: ModoCocinaUiState, viewModel: ModoCocinaViewModel, onSalir: () -> Unit) {
    Column(
        Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            stringResource(R.string.cocina_confirmar_titulo),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.semantics { heading() },
        )
        Text(stringResource(R.string.cocina_confirmar_ayuda), style = MaterialTheme.typography.bodyMedium)

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.cocina_porciones_preparadas),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f),
            )
            IconButton(
                onClick = { viewModel.cambiarPorcionesPreparadas(-1) },
                enabled = estado.porcionesPreparadas > GestorCompras.MIN_PORCIONES,
            ) { Icon(Icons.Default.Remove, stringResource(R.string.detalle_menos_porciones)) }
            Text(
                estado.porcionesPreparadas.toString(),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(min = 24.dp),
            )
            IconButton(
                onClick = { viewModel.cambiarPorcionesPreparadas(1) },
                enabled = estado.porcionesPreparadas < GestorCompras.MAX_PORCIONES,
            ) { Icon(Icons.Default.Add, stringResource(R.string.detalle_mas_porciones)) }
        }

        estado.usos.forEachIndexed { indice, uso ->
            OutlinedTextField(
                value = uso.texto,
                onValueChange = { viewModel.cambiarUso(indice, it) },
                label = {
                    Text(
                        stringResource(
                            R.string.cocina_cantidad_usada,
                            uso.usado.ingrediente.nombre,
                            stringResource(uso.usado.unidad.etiqueta),
                        ),
                    )
                },
                isError = uso.cantidad == null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Vista(estado)

        if (estado.error) {
            Text(stringResource(R.string.cocina_error), color = MaterialTheme.colorScheme.error)
        }
        Button(
            onClick = viewModel::confirmar,
            enabled = !estado.guardando && estado.usosValidos,
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) { Text(stringResource(R.string.cocina_descontar)) }
        OutlinedButton(onClick = viewModel::volverAPasos, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.cocina_volver_pasos))
        }
        TextButton(onClick = onSalir, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.cocina_salir_sin_descontar))
        }
    }
}

/** Vista previa de cómo quedará Mi refri, incluidas las diferencias, antes de confirmar. */
@Composable
private fun Vista(estado: ModoCocinaUiState) {
    val consumo = estado.consumo ?: return
    Surface(color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(stringResource(R.string.cocina_asi_quedara), style = MaterialTheme.typography.titleSmall)
            if (consumo.cambios.isEmpty()) {
                Text(stringResource(R.string.cocina_sin_cambios), style = MaterialTheme.typography.bodyMedium)
            }
            consumo.cambios.forEach { despues ->
                val antes = estado.antes[despues.id]
                Text(
                    stringResource(
                        R.string.cocina_cambio,
                        despues.nombre,
                        antes?.let { textoCantidad(it.cantidad, it.unidad) }.orEmpty(),
                        textoCantidad(despues.cantidad, despues.unidad),
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            consumo.diferencias.forEach { diferencia ->
                val registrado = consumo.cambios.any { it.ingredienteId == diferencia.ingrediente.id }
                val cantidad = textoCantidad(diferencia.sinRegistrar, diferencia.unidad)
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Outlined.Info, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (registrado) {
                            stringResource(R.string.cocina_usaste_mas, diferencia.ingrediente.nombre.lowercase(), cantidad)
                        } else {
                            stringResource(R.string.cocina_no_registrado, "${diferencia.ingrediente.nombre} ($cantidad)")
                        },
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            if (consumo.diferencias.isNotEmpty()) {
                Text(
                    stringResource(R.string.cocina_corregir_despues),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (consumo.aproximado) {
                Text(
                    stringResource(R.string.cocina_aproximado),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun Terminado(yaRegistrada: Boolean, onSalir: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.cocina_listo_titulo), style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(if (yaRegistrada) R.string.cocina_ya_registrada else R.string.cocina_listo_mensaje),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onSalir) { Text(stringResource(R.string.cocina_volver_receta)) }
    }
}
