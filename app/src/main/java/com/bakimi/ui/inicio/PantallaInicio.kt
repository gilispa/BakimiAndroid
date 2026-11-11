package com.bakimi.ui.inicio

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bakimi.R
import com.bakimi.domain.recetas.EvaluacionReceta
import com.bakimi.domain.recetas.SeccionesInicio
import com.bakimi.ui.recetas.EtiquetaDisponibilidad
import com.bakimi.ui.recetas.ImagenReceta
import com.bakimi.ui.recetas.textoDisponibilidad

@Composable
fun PantallaInicio(
    onAbrirReceta: (String) -> Unit,
    onIrARefri: () -> Unit,
    onPlanear: () -> Unit,
    viewModel: InicioViewModel = hiltViewModel(),
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    when (val actual = estado) {
        InicioUiState.Cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }

        is InicioUiState.Listo -> ContenidoInicio(
            secciones = actual.secciones,
            refriVacio = actual.refriVacio,
            onAbrirReceta = onAbrirReceta,
            onIrARefri = onIrARefri,
            onPlanear = onPlanear,
        )
    }
}

@Composable
private fun ContenidoInicio(
    secciones: SeccionesInicio,
    refriVacio: Boolean,
    onAbrirReceta: (String) -> Unit,
    onIrARefri: () -> Unit,
    onPlanear: () -> Unit,
) {
    // El carrusel ocupa cerca de la mitad de la pantalla y deja ver el contenido de abajo.
    val altoCarrusel = (LocalConfiguration.current.screenHeightDp * 0.42f).dp

    LazyColumn(
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        item {
            Column(
                Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.logo_bakimi_horizontal),
                    contentDescription = stringResource(R.string.app_name),
                    modifier = Modifier.height(40.dp).semantics { heading() },
                )
                Text(
                    stringResource(R.string.inicio_lema),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item {
            Carrusel(secciones.destacadas, alto = altoCarrusel, onAbrirReceta = onAbrirReceta)
        }
        item {
            Button(onClick = onPlanear, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Icon(Icons.Default.DateRange, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.inicio_planear))
            }
        }
        if (refriVacio) {
            item { InvitacionRefri(onIrARefri) }
        }
        seccion(R.string.inicio_puedes_cocinar, secciones.puedesCocinar, onAbrirReceta)
        seccion(R.string.inicio_faltan_pocos, secciones.faltanPocos, onAbrirReceta)
        seccion(R.string.inicio_rapidas, secciones.rapidas, onAbrirReceta)
        seccion(R.string.inicio_mas_ideas, secciones.masIdeas, onAbrirReceta)
    }
}

private fun LazyListScope.seccion(
    @StringRes titulo: Int,
    recetas: List<EvaluacionReceta>,
    onAbrirReceta: (String) -> Unit,
) {
    if (recetas.isEmpty()) return
    item(key = titulo) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                stringResource(titulo),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp).semantics { heading() },
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(recetas, key = { it.receta.id }) { evaluacion ->
                    TarjetaReceta(evaluacion, onClick = { onAbrirReceta(evaluacion.receta.id) })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Carrusel(
    recetas: List<EvaluacionReceta>,
    alto: Dp,
    onAbrirReceta: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            stringResource(R.string.inicio_destacadas),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp).semantics { heading() },
        )
        HorizontalMultiBrowseCarousel(
            state = rememberCarouselState { recetas.size },
            preferredItemWidth = 300.dp,
            itemSpacing = 8.dp,
            contentPadding = PaddingValues(horizontal = 16.dp),
            modifier = Modifier.fillMaxWidth().height(alto),
        ) { indice ->
            val evaluacion = recetas[indice]
            val descripcion = "${evaluacion.receta.nombre}. ${textoDisponibilidad(evaluacion)}. " +
                stringResource(R.string.receta_minutos_accesible, evaluacion.receta.minutos)
            Box(
                Modifier
                    .fillMaxSize()
                    .maskClip(MaterialTheme.shapes.extraLarge)
                    .clickable(role = Role.Button) { onAbrirReceta(evaluacion.receta.id) }
                    .semantics(mergeDescendants = true) { contentDescription = descripcion },
            ) {
                ImagenReceta(evaluacion.receta.id, Modifier.fillMaxSize())
                // Degradado para que el texto blanco se lea sobre cualquier foto.
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                0.45f to Color.Transparent,
                                1f to Color.Black.copy(alpha = 0.75f),
                            ),
                        ),
                )
                Column(
                    Modifier.align(Alignment.BottomStart).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    EtiquetaDisponibilidad(evaluacion)
                    Text(
                        evaluacion.receta.nombre,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Tiempo(evaluacion.receta.minutos, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun TarjetaReceta(evaluacion: EvaluacionReceta, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(200.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        ImagenReceta(evaluacion.receta.id, Modifier.fillMaxWidth().aspectRatio(4f / 3f))
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                evaluacion.receta.nombre,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                minLines = 2,
            )
            Tiempo(evaluacion.receta.minutos, color = MaterialTheme.colorScheme.onSurfaceVariant)
            EtiquetaDisponibilidad(evaluacion)
        }
    }
}

@Composable
private fun Tiempo(minutos: Int, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Outlined.Schedule, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(4.dp))
        Text(
            stringResource(R.string.receta_minutos, minutos),
            style = MaterialTheme.typography.labelLarge,
            color = color,
        )
    }
}

@Composable
private fun InvitacionRefri(onIrARefri: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Kitchen, contentDescription = null, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.inicio_refri_vacio_titulo), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.inicio_refri_vacio_mensaje), style = MaterialTheme.typography.bodyMedium)
                Button(onClick = onIrARefri) { Text(stringResource(R.string.inicio_refri_vacio_accion)) }
            }
        }
    }
}
