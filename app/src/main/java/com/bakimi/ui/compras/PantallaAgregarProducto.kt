package com.bakimi.ui.compras

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bakimi.R
import com.bakimi.domain.inventario.Unidad
import com.bakimi.ui.refri.BuscadorIngredientes
import com.bakimi.ui.refri.Opcion
import com.bakimi.ui.refri.Seccion
import com.bakimi.ui.refri.SeleccionElegida
import com.bakimi.ui.refri.etiqueta

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PantallaAgregarProducto(
    onTerminar: () -> Unit,
    viewModel: AgregarProductoViewModel = hiltViewModel(),
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    LaunchedEffect(estado.guardado) { if (estado.guardado) onTerminar() }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.compras_agregar_titulo)) },
                navigationIcon = {
                    IconButton(onClick = onTerminar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.form_volver))
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
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
                    error = estado.errorNombre,
                    onBuscar = viewModel::cambiarBusqueda,
                    onElegir = viewModel::elegir,
                    onOtro = viewModel::elegirOtro,
                )
                return@Column
            }
            SeleccionElegida(seleccion, onCambiar = viewModel::cambiarSeleccion)

            OutlinedTextField(
                value = estado.cantidadTexto,
                onValueChange = viewModel::cambiarCantidad,
                label = { Text(stringResource(R.string.form_cantidad)) },
                isError = estado.errorCantidad,
                supportingText = if (estado.errorCantidad) {
                    { Text(stringResource(R.string.form_cantidad_error)) }
                } else {
                    null
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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

            OutlinedTextField(
                value = estado.precioTexto,
                onValueChange = viewModel::cambiarPrecio,
                label = { Text(stringResource(R.string.compras_precio_opcional)) },
                prefix = { Text("$") },
                isError = estado.errorPrecio,
                supportingText = { Text(stringResource(R.string.compras_precio_opcional_ayuda)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )

            if (estado.errorAlGuardar) {
                Text(
                    stringResource(R.string.compras_error),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Button(
                onClick = viewModel::guardar,
                enabled = !estado.guardando,
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) { Text(stringResource(R.string.form_guardar)) }
        }
    }
}
