package com.bakimi.ui.refri

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material.icons.outlined.RemoveShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bakimi.R
import com.bakimi.domain.inventario.ItemInventario
import com.bakimi.domain.inventario.Ubicacion
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaRefri(
    onAgregar: () -> Unit,
    onEditar: (Long) -> Unit,
    viewModel: RefriViewModel = hiltViewModel(),
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val contexto = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.eventos.collect { evento ->
            when (evento) {
                EventoRefri.ErrorOperacion ->
                    snackbarHostState.showSnackbar(contexto.getString(R.string.refri_error_operacion))

                is EventoRefri.Eliminado -> {
                    val resultado = snackbarHostState.showSnackbar(
                        message = contexto.getString(R.string.refri_eliminado, evento.item.nombre),
                        actionLabel = contexto.getString(R.string.refri_deshacer),
                        duration = SnackbarDuration.Long,
                    )
                    if (resultado == SnackbarResult.ActionPerformed) {
                        viewModel.deshacerEliminacion(evento.item)
                    }
                }
            }
        }
    }

    Scaffold(
        // El Scaffold raíz ya aplica las barras del sistema.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_refri)) },
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        floatingActionButton = {
            val grupos = (estado as? RefriUiState.Listo)?.grupos
            if (!grupos.isNullOrEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = onAgregar,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text(stringResource(R.string.refri_agregar)) },
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        when (val actual = estado) {
            RefriUiState.Cargando -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            is RefriUiState.Listo ->
                if (actual.grupos.isEmpty()) {
                    RefriVacio(onAgregar = onAgregar, modifier = Modifier.padding(padding))
                } else {
                    ListaInventario(
                        grupos = actual.grupos,
                        contentPadding = padding,
                        onEditar = onEditar,
                        onSumar = viewModel::sumar,
                        onRestar = viewModel::restar,
                        onMarcarAgotado = viewModel::marcarAgotado,
                        onEliminar = viewModel::eliminar,
                    )
                }
        }
    }
}

@Composable
private fun RefriVacio(onAgregar: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            Icons.Default.Kitchen,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.refri_vacio_titulo),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.refri_vacio_mensaje),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onAgregar) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.refri_vacio_accion))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ListaInventario(
    grupos: Map<Ubicacion, List<ItemInventario>>,
    contentPadding: PaddingValues,
    onEditar: (Long) -> Unit,
    onSumar: (Long) -> Unit,
    onRestar: (Long) -> Unit,
    onMarcarAgotado: (Long) -> Unit,
    onEliminar: (ItemInventario) -> Unit,
) {
    val hoy = remember { LocalDate.now() }
    LazyColumn(
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding(),
            // Espacio para que el botón flotante no tape el último alimento.
            bottom = contentPadding.calculateBottomPadding() + 88.dp,
        ),
    ) {
        grupos.forEach { (ubicacion, items) ->
            stickyHeader(key = ubicacion.name) {
                Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(
                            R.string.refri_encabezado_grupo,
                            stringResource(ubicacion.etiqueta),
                            items.size,
                        ),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .semantics { heading() },
                    )
                }
            }
            items(items, key = { it.id }) { item ->
                FilaInventario(
                    item = item,
                    hoy = hoy,
                    onEditar = { onEditar(item.id) },
                    onSumar = { onSumar(item.id) },
                    onRestar = { onRestar(item.id) },
                    onMarcarAgotado = { onMarcarAgotado(item.id) },
                    onEliminar = { onEliminar(item) },
                    modifier = Modifier.animateItem(),
                )
                HorizontalDivider(modifier = Modifier.padding(start = 16.dp))
            }
        }
    }
}

@Composable
private fun FilaInventario(
    item: ItemInventario,
    hoy: LocalDate,
    onEditar: () -> Unit,
    onSumar: () -> Unit,
    onRestar: () -> Unit,
    onMarcarAgotado: () -> Unit,
    onEliminar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val paso = textoCantidad(item.unidad.pasoRapido, item.unidad)
    ListItem(
        modifier = modifier.clickable(onClick = onEditar),
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background),
        headlineContent = {
            Text(item.nombre, maxLines = 2, overflow = TextOverflow.Ellipsis)
        },
        supportingContent = {
            Column {
                if (item.agotado) {
                    Etiqueta(
                        icono = { Icon(Icons.Outlined.RemoveShoppingCart, null, Modifier.size(16.dp)) },
                        texto = stringResource(R.string.refri_agotado),
                        color = MaterialTheme.colorScheme.error,
                    )
                } else {
                    Text(textoCantidad(item.cantidad, item.unidad))
                }
                item.caducidad?.let { fecha ->
                    val caducado = fecha.isBefore(hoy)
                    Etiqueta(
                        icono = {
                            Icon(
                                if (caducado) Icons.Outlined.ReportProblem else Icons.Outlined.Event,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                        },
                        texto = stringResource(
                            if (caducado) R.string.refri_caducado else R.string.refri_caduca,
                            formatearFecha(fecha),
                        ),
                        color = if (caducado) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onRestar, enabled = !item.agotado) {
                    Icon(Icons.Default.Remove, stringResource(R.string.refri_restar, paso, item.nombre))
                }
                IconButton(onClick = onSumar) {
                    Icon(Icons.Default.Add, stringResource(R.string.refri_sumar, paso, item.nombre))
                }
                MenuFila(
                    nombre = item.nombre,
                    agotado = item.agotado,
                    onEditar = onEditar,
                    onMarcarAgotado = onMarcarAgotado,
                    onEliminar = onEliminar,
                )
            }
        },
    )
}

@Composable
private fun Etiqueta(icono: @Composable () -> Unit, texto: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        CompositionLocalProvider(LocalContentColor provides color) {
            icono()
            Spacer(Modifier.width(4.dp))
            Text(texto, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun MenuFila(
    nombre: String,
    agotado: Boolean,
    onEditar: () -> Unit,
    onMarcarAgotado: () -> Unit,
    onEliminar: () -> Unit,
) {
    var abierto by rememberSaveable { mutableStateOf(false) }
    Box {
        IconButton(onClick = { abierto = true }) {
            Icon(Icons.Default.MoreVert, stringResource(R.string.refri_mas_opciones, nombre))
        }
        DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.refri_editar)) },
                onClick = { abierto = false; onEditar() },
            )
            if (!agotado) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.refri_marcar_agotado)) },
                    onClick = { abierto = false; onMarcarAgotado() },
                )
            }
            DropdownMenuItem(
                text = { Text(stringResource(R.string.refri_eliminar)) },
                onClick = { abierto = false; onEliminar() },
            )
        }
    }
}
