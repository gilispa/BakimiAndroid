package com.bakimi.ui.compras

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bakimi.R
import com.bakimi.data.ingredientes.FECHA_PRECIOS_REFERENCIA
import com.bakimi.domain.compras.EstadoPresupuesto
import com.bakimi.domain.compras.RenglonCompra
import com.bakimi.domain.compras.Semana
import com.bakimi.domain.ingredientes.Categoria
import com.bakimi.domain.inventario.parsearCantidad
import com.bakimi.ui.refri.etiqueta
import com.bakimi.ui.refri.textoCantidad
import com.bakimi.ui.refri.textoEditable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCompras(
    onAgregarProducto: () -> Unit,
    onPlanear: () -> Unit,
    viewModel: ComprasViewModel = hiltViewModel(),
) {
    val semana by viewModel.estado.collectAsStateWithLifecycle()
    val terminando by viewModel.terminando.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val contexto = LocalContext.current
    var confirmar by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.eventos.collect { evento ->
            val mensaje = when (evento) {
                is EventoCompras.Incorporados ->
                    contexto.resources.getQuantityString(R.plurals.compras_incorporados, evento.cantidad, evento.cantidad)
                EventoCompras.Error -> contexto.getString(R.string.compras_error)
            }
            snackbar.showSnackbar(mensaje)
        }
    }

    val marcados = semana?.lista?.marcados.orEmpty()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_compras)) },
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        floatingActionButton = {
            if (marcados.isEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = onAgregarProducto,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text(stringResource(R.string.compras_agregar_producto)) },
                )
            }
        },
        bottomBar = {
            if (marcados.isNotEmpty()) {
                Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp) {
                    Button(
                        onClick = { confirmar = true },
                        enabled = !terminando,
                        modifier = Modifier.fillMaxWidth().padding(16.dp).height(52.dp),
                    ) {
                        Text(stringResource(R.string.compras_terminar, marcados.size), textAlign = TextAlign.Center)
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        val actual = semana
        when {
            actual == null -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            actual.lista.renglones.isEmpty() -> ListaVacia(
                planVacio = actual.recetas.isEmpty(),
                onPlanear = onPlanear,
                modifier = Modifier.padding(padding),
            )
            else -> Lista(actual, padding, viewModel)
        }
    }

    if (confirmar && marcados.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { confirmar = false },
            title = { Text(stringResource(R.string.compras_confirmar_titulo)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(stringResource(R.string.compras_confirmar_mensaje))
                    marcados.forEach {
                        Text(stringResource(R.string.compras_confirmar_renglon, it.nombre, textoCantidad(it.cantidad, it.unidad)))
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.compras_confirmar_pendientes),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmar = false
                        viewModel.terminarCompra()
                    },
                    enabled = !terminando,
                ) { Text(stringResource(R.string.compras_confirmar_accion)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmar = false }) { Text(stringResource(R.string.form_cancelar)) }
            },
        )
    }
}

@Composable
private fun ListaVacia(planVacio: Boolean, onPlanear: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            Icons.Outlined.ShoppingCart,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            stringResource(if (planVacio) R.string.compras_vacia_titulo else R.string.compras_cubierta_titulo),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(if (planVacio) R.string.compras_vacia_mensaje else R.string.compras_cubierta_mensaje),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (planVacio) {
            Spacer(Modifier.height(24.dp))
            Button(onClick = onPlanear) { Text(stringResource(R.string.compras_planear)) }
        }
    }
}

/** Orden de los grupos de la lista (documento de producto, sección 13); "Otros" al final. */
private val ORDEN_GRUPOS: List<Categoria?> = Categoria.entries + null

@Composable
private fun Lista(semana: Semana, padding: PaddingValues, viewModel: ComprasViewModel) {
    var corrigiendo by remember { mutableStateOf<RenglonCompra?>(null) }
    val nombresRecetas = semana.recetas.associate { it.receta.id to it.receta.nombre }
    // Dentro de cada grupo, primero lo pendiente y después lo ya comprado.
    val grupos = semana.lista.renglones
        .groupBy { it.categoria }
        .mapValues { (_, renglones) -> renglones.sortedBy { it.comprado } }

    LazyColumn(
        contentPadding = PaddingValues(
            top = padding.calculateTopPadding(),
            bottom = padding.calculateBottomPadding() + 96.dp,
        ),
    ) {
        item { Resumen(semana) }
        ORDEN_GRUPOS.forEach { categoria ->
            val renglones = grupos[categoria] ?: return@forEach
            item(key = "grupo_${categoria?.name ?: "otros"}") {
                Text(
                    stringResource(categoria?.etiqueta ?: R.string.compras_otros),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 4.dp)
                        .semantics { heading() },
                )
            }
            items(renglones, key = { it.clave }) { renglon ->
                FilaCompra(
                    renglon,
                    recetas = renglon.recetas.mapNotNull(nombresRecetas::get),
                    onMarcar = { viewModel.marcar(renglon, it) },
                    onCorregir = { corrigiendo = renglon },
                )
            }
        }
    }

    corrigiendo?.let { renglon ->
        DialogoCorregir(
            renglon = renglon,
            onCerrar = { corrigiendo = null },
            onGuardar = { cantidad, precio ->
                viewModel.corregir(renglon, cantidad, precio)
                corrigiendo = null
            },
            onEliminar = {
                viewModel.eliminar(renglon)
                corrigiendo = null
            },
        )
    }
}

@Composable
private fun Resumen(semana: Semana) {
    val total = semana.lista.total
    val presupuesto = semana.config.presupuesto
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                if (presupuesto == null) {
                    stringResource(R.string.semana_compra_estimada, textoTotal(total))
                } else {
                    stringResource(
                        R.string.semana_compra_vs_presupuesto,
                        textoTotal(total),
                        stringResource(R.string.precio, formatearPrecio(presupuesto)),
                    )
                },
                style = MaterialTheme.typography.titleMedium,
            )
            when (semana.presupuesto) {
                EstadoPresupuesto.EXCEDIDO -> Text(
                    stringResource(R.string.semana_presupuesto_excedido),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
                EstadoPresupuesto.INCIERTO -> Text(
                    stringResource(R.string.semana_presupuesto_incierto),
                    style = MaterialTheme.typography.bodyMedium,
                )
                EstadoPresupuesto.DENTRO -> Text(
                    stringResource(R.string.semana_presupuesto_dentro),
                    style = MaterialTheme.typography.bodyMedium,
                )
                EstadoPresupuesto.SIN_PRESUPUESTO -> if (total.parcial) {
                    Text(stringResource(R.string.semana_faltan_precios), style = MaterialTheme.typography.bodyMedium)
                }
            }
            Text(
                stringResource(R.string.precios_referencia, FECHA_PRECIOS_REFERENCIA),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun FilaCompra(
    renglon: RenglonCompra,
    recetas: List<String>,
    onMarcar: (Boolean) -> Unit,
    onCorregir: () -> Unit,
) {
    val atenuado = if (renglon.comprado) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
    Row(
        Modifier
            .fillMaxWidth()
            .toggleable(value = renglon.comprado, role = Role.Checkbox, onValueChange = onMarcar)
            .padding(start = 4.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // El renglón completo es el control; la casilla solo lo refleja.
        Checkbox(checked = renglon.comprado, onCheckedChange = null, modifier = Modifier.padding(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                renglon.nombre,
                style = MaterialTheme.typography.bodyLarge,
                color = atenuado,
                textDecoration = if (renglon.comprado) TextDecoration.LineThrough else null,
            )
            Text(textoCantidadCompra(renglon), style = MaterialTheme.typography.bodyMedium, color = atenuado)
            val detalle = when {
                renglon.comprado -> stringResource(R.string.compras_comprado)
                renglon.manual -> stringResource(R.string.compras_agregado_a_mano)
                recetas.isNotEmpty() -> stringResource(R.string.compras_para, recetas.joinToString(", "))
                else -> null
            }
            detalle?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Text(
            textoPrecio(renglon.precio, renglon.precioReal),
            style = MaterialTheme.typography.bodyMedium,
            color = atenuado,
        )
        IconButton(onClick = onCorregir) {
            Icon(Icons.Outlined.Edit, stringResource(R.string.compras_corregir, renglon.nombre))
        }
    }
}

@Composable
private fun DialogoCorregir(
    renglon: RenglonCompra,
    onCerrar: () -> Unit,
    onGuardar: (cantidad: Double?, precio: Double?) -> Unit,
    onEliminar: () -> Unit,
) {
    var cantidad by rememberSaveable { mutableStateOf(textoEditable(renglon.cantidad)) }
    var precio by rememberSaveable {
        mutableStateOf(if (renglon.precioReal) renglon.precio?.let(::textoEditable).orEmpty() else "")
    }
    val cantidadValida = parsearCantidad(cantidad)?.takeIf { it > 0.0 }
    val precioValido = precio.isBlank() || parsearCantidad(precio) != null

    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text(stringResource(R.string.compras_corregir_titulo, renglon.nombre)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = cantidad,
                    onValueChange = { cantidad = it },
                    label = { Text(stringResource(R.string.compras_cantidad_real, stringResource(renglon.unidad.etiqueta))) },
                    isError = cantidadValida == null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
                OutlinedTextField(
                    value = precio,
                    onValueChange = { precio = it },
                    label = { Text(stringResource(R.string.compras_precio_real)) },
                    prefix = { Text("$") },
                    supportingText = { Text(stringResource(R.string.compras_precio_real_ayuda)) },
                    isError = !precioValido,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
                if (renglon.manual) {
                    TextButton(onClick = onEliminar) { Text(stringResource(R.string.compras_eliminar)) }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    // Sin cambio de cantidad se conserva la presentación ("1 × bolsa de 1 kg").
                    val sinCambio = renglon.presentaciones != null && cantidadValida == renglon.cantidad
                    onGuardar(
                        if (sinCambio) null else cantidadValida,
                        precio.takeIf { it.isNotBlank() }?.let(::parsearCantidad),
                    )
                },
                enabled = cantidadValida != null && precioValido,
            ) { Text(stringResource(R.string.compras_guardar)) }
        },
        dismissButton = { TextButton(onClick = onCerrar) { Text(stringResource(R.string.form_cancelar)) } },
    )
}
