package com.bakimi.ui.refri

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bakimi.R
import com.bakimi.domain.ingredientes.Ingrediente
import com.bakimi.domain.inventario.ErrorItem
import com.bakimi.domain.inventario.Ubicacion
import com.bakimi.domain.inventario.Unidad
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaFormularioItem(
    onTerminar: () -> Unit,
    viewModel: FormularioItemViewModel = hiltViewModel(),
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    LaunchedEffect(estado.guardado) {
        if (estado.guardado) onTerminar()
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (estado.esEdicion) R.string.form_titulo_editar else R.string.form_titulo_agregar,
                        ),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onTerminar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.form_volver))
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        if (estado.cargando) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Formulario(
                estado = estado,
                viewModel = viewModel,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Formulario(
    estado: FormularioUiState,
    viewModel: FormularioItemViewModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        val seleccion = estado.seleccion
        if (seleccion == null) {
            BuscadorIngredientes(
                busqueda = estado.busqueda,
                resultados = estado.resultados,
                ofrecerOtro = estado.ofrecerOtro,
                error = ErrorItem.NOMBRE_VACIO in estado.errores,
                onBuscar = viewModel::cambiarBusqueda,
                onElegir = viewModel::elegir,
                onOtro = viewModel::elegirOtro,
            )
            return@Column
        }
        SeleccionElegida(seleccion, onCambiar = viewModel::cambiarSeleccion)

        val errorCantidad = ErrorItem.CANTIDAD_INVALIDA in estado.errores
        OutlinedTextField(
            value = estado.cantidadTexto,
            onValueChange = viewModel::cambiarCantidad,
            label = { Text(stringResource(R.string.form_cantidad)) },
            isError = errorCantidad,
            supportingText = {
                Text(
                    stringResource(
                        if (errorCantidad) R.string.form_cantidad_error else R.string.form_cantidad_ayuda,
                    ),
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth(),
        )

        Seccion(titulo = stringResource(R.string.form_unidad)) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Unidad.entries.forEach { unidad ->
                    Opcion(
                        texto = stringResource(unidad.etiqueta),
                        seleccionada = estado.unidad == unidad,
                        onClick = { viewModel.cambiarUnidad(unidad) },
                    )
                }
            }
        }

        Seccion(titulo = stringResource(R.string.form_ubicacion)) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Ubicacion.entries.forEach { ubicacion ->
                    Opcion(
                        texto = stringResource(ubicacion.etiqueta),
                        seleccionada = estado.ubicacion == ubicacion,
                        onClick = { viewModel.cambiarUbicacion(ubicacion) },
                    )
                }
            }
        }

        Seccion(titulo = stringResource(R.string.form_caducidad)) {
            SelectorCaducidad(fecha = estado.caducidad, onCambiar = viewModel::cambiarCaducidad)
        }

        if (estado.errorAlGuardar) {
            Text(
                stringResource(R.string.form_error_guardar),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Button(
            onClick = viewModel::guardar,
            enabled = !estado.guardando,
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            Text(stringResource(R.string.form_guardar))
        }
        Spacer(Modifier.height(8.dp))
    }
}

/** Buscador del catálogo de ingredientes, con la opción de agregar un alimento "otro". */
@Composable
internal fun BuscadorIngredientes(
    busqueda: String,
    resultados: List<Ingrediente>,
    ofrecerOtro: Boolean,
    error: Boolean,
    onBuscar: (String) -> Unit,
    onElegir: (Ingrediente) -> Unit,
    onOtro: () -> Unit,
) {
    val foco = remember { FocusRequester() }
    // Al abrir el formulario, el teclado aparece listo para buscar.
    LaunchedEffect(Unit) { foco.requestFocus() }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        OutlinedTextField(
            value = busqueda,
            onValueChange = onBuscar,
            label = { Text(stringResource(R.string.form_buscar)) },
            placeholder = { Text(stringResource(R.string.form_buscar_ejemplo)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            isError = error,
            supportingText = {
                Text(stringResource(if (error) R.string.form_nombre_error else R.string.form_buscar_ayuda))
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Search,
            ),
            modifier = Modifier.fillMaxWidth().focusRequester(foco),
        )

        resultados.forEach { ingrediente ->
            ListItem(
                headlineContent = { Text(ingrediente.nombre) },
                supportingContent = { Text(stringResource(ingrediente.categoria.etiqueta)) },
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background),
                modifier = Modifier.clickable { onElegir(ingrediente) },
            )
        }
        if (ofrecerOtro) {
            ListItem(
                leadingContent = { Icon(Icons.Default.Add, contentDescription = null) },
                headlineContent = { Text(stringResource(R.string.form_agregar_otro, busqueda.trim())) },
                supportingContent = { Text(stringResource(R.string.form_otro_explicacion)) },
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background),
                modifier = Modifier.clickable(onClick = onOtro),
            )
        }
    }
}

@Composable
internal fun SeleccionElegida(seleccion: Seleccion, onCambiar: () -> Unit) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(seleccion.nombre, style = MaterialTheme.typography.titleMedium)
                Text(
                    when (seleccion) {
                        is Seleccion.DelCatalogo -> stringResource(seleccion.ingrediente.categoria.etiqueta)
                        is Seleccion.Otro -> stringResource(R.string.form_fuera_catalogo)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            TextButton(onClick = onCambiar) { Text(stringResource(R.string.form_cambiar)) }
        }
    }
}

@Composable
internal fun Seccion(titulo: String, contenido: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            titulo,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.semantics { heading() },
        )
        contenido()
    }
}

@Composable
internal fun Opcion(texto: String, seleccionada: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = seleccionada,
        onClick = onClick,
        label = { Text(texto) },
        // La marca de verificación indica la selección además del color.
        leadingIcon = if (seleccionada) {
            { Icon(Icons.Default.Check, contentDescription = null) }
        } else {
            null
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.secondary,
            selectedLabelColor = MaterialTheme.colorScheme.onSecondary,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onSecondary,
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectorCaducidad(fecha: LocalDate?, onCambiar: (LocalDate?) -> Unit) {
    var dialogoAbierto by rememberSaveable { mutableStateOf(false) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedButton(onClick = { dialogoAbierto = true }) {
            Icon(Icons.Outlined.Event, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(fecha?.let(::formatearFecha) ?: stringResource(R.string.form_caducidad_agregar))
        }
        if (fecha != null) {
            IconButton(onClick = { onCambiar(null) }) {
                Icon(Icons.Default.Close, stringResource(R.string.form_caducidad_quitar))
            }
        }
    }

    if (dialogoAbierto) {
        // DatePicker trabaja en milisegundos UTC; se convierte en UTC para no desfasar el día.
        val estadoSelector = rememberDatePickerState(
            initialSelectedDateMillis = fecha?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { dialogoAbierto = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        estadoSelector.selectedDateMillis?.let { millis ->
                            onCambiar(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                        }
                        dialogoAbierto = false
                    },
                ) { Text(stringResource(R.string.form_aceptar)) }
            },
            dismissButton = {
                TextButton(onClick = { dialogoAbierto = false }) {
                    Text(stringResource(R.string.form_cancelar))
                }
            },
        ) {
            DatePicker(state = estadoSelector)
        }
    }
}
