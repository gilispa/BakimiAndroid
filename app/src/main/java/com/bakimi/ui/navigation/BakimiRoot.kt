package com.bakimi.ui.navigation

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bakimi.ui.cocina.ModoCocinaViewModel
import com.bakimi.ui.cocina.PantallaModoCocina
import com.bakimi.ui.compras.PantallaAgregarProducto
import com.bakimi.ui.compras.PantallaCompras
import com.bakimi.ui.inicio.PantallaInicio
import com.bakimi.ui.recetas.DetalleRecetaViewModel
import com.bakimi.ui.recetas.PantallaDetalleReceta
import com.bakimi.ui.refri.FormularioItemViewModel
import com.bakimi.ui.refri.PantallaFormularioItem
import com.bakimi.ui.refri.PantallaRefri
import com.bakimi.ui.semana.PantallaSemana

/** Formulario para agregar (sin id) o editar (con id) un alimento de Mi refri. */
private const val RUTA_FORMULARIO_ITEM = "refri/item?${FormularioItemViewModel.ARG_ID}={${FormularioItemViewModel.ARG_ID}}"

private fun rutaFormularioItem(id: Long? = null): String =
    if (id == null) "refri/item" else "refri/item?${FormularioItemViewModel.ARG_ID}=$id"

private const val RUTA_DETALLE_RECETA = "receta/{${DetalleRecetaViewModel.ARG_ID}}"

private fun rutaDetalleReceta(id: String): String = "receta/$id"

private const val RUTA_AGREGAR_PRODUCTO = "compras/agregar"

private const val RUTA_MODO_COCINA =
    "cocina/{${ModoCocinaViewModel.ARG_RECETA}}/{${ModoCocinaViewModel.ARG_PORCIONES}}"

private fun rutaModoCocina(recetaId: String, porciones: Int): String = "cocina/$recetaId/$porciones"

@Composable
fun BakimiRoot() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destinoActual = backStackEntry?.destination
    // La barra inferior solo aparece en las cuatro secciones principales.
    val mostrarBarra = destinoActual == null || Destino.entries.any { it.ruta == destinoActual.route }

    Scaffold(
        bottomBar = {
            if (mostrarBarra) {
                NavigationBar {
                    Destino.entries.forEach { destino ->
                        NavigationBarItem(
                            selected = destinoActual?.hierarchy?.any { it.route == destino.ruta } == true,
                            onClick = { navController.irASeccion(destino) },
                            // La etiqueta visible ya nombra el destino; el ícono es decorativo.
                            icon = { Icon(destino.icono, contentDescription = null) },
                            label = { Text(stringResource(destino.etiqueta)) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Destino.INICIO.ruta,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
        ) {
            Destino.entries.forEach { destino ->
                composable(destino.ruta) {
                    when (destino) {
                        Destino.INICIO -> PantallaInicio(
                            onAbrirReceta = { id -> navController.navigate(rutaDetalleReceta(id)) },
                            onIrARefri = { navController.irASeccion(Destino.REFRI) },
                            onPlanear = { navController.irASeccion(Destino.SEMANA) },
                        )

                        Destino.REFRI -> PantallaRefri(
                            onAgregar = { navController.navigate(rutaFormularioItem()) },
                            onEditar = { id -> navController.navigate(rutaFormularioItem(id)) },
                        )

                        Destino.SEMANA -> PantallaSemana(
                            onAbrirReceta = { id -> navController.navigate(rutaDetalleReceta(id)) },
                            onVerLista = { navController.irASeccion(Destino.COMPRAS) },
                        )

                        Destino.COMPRAS -> PantallaCompras(
                            onAgregarProducto = { navController.navigate(RUTA_AGREGAR_PRODUCTO) },
                            onPlanear = { navController.irASeccion(Destino.SEMANA) },
                        )
                    }
                }
            }
            composable(
                route = RUTA_FORMULARIO_ITEM,
                arguments = listOf(
                    navArgument(FormularioItemViewModel.ARG_ID) {
                        type = NavType.LongType
                        defaultValue = 0L
                    },
                ),
            ) { entrada ->
                PantallaFormularioItem(onTerminar = { navController.volverDesde(entrada) })
            }
            composable(
                route = RUTA_DETALLE_RECETA,
                arguments = listOf(navArgument(DetalleRecetaViewModel.ARG_ID) { type = NavType.StringType }),
            ) { entrada ->
                PantallaDetalleReceta(
                    onVolver = { navController.volverDesde(entrada) },
                    onIniciarCocina = { id, porciones -> navController.navigate(rutaModoCocina(id, porciones)) },
                )
            }
            composable(
                route = RUTA_MODO_COCINA,
                arguments = listOf(
                    navArgument(ModoCocinaViewModel.ARG_RECETA) { type = NavType.StringType },
                    navArgument(ModoCocinaViewModel.ARG_PORCIONES) { type = NavType.IntType },
                ),
            ) { entrada ->
                PantallaModoCocina(onSalir = { navController.volverDesde(entrada) })
            }
            composable(RUTA_AGREGAR_PRODUCTO) { entrada ->
                PantallaAgregarProducto(onTerminar = { navController.volverDesde(entrada) })
            }
        }
    }
}

/** Cambia de sección conservando el estado de cada una, igual que la barra inferior. */
private fun NavController.irASeccion(destino: Destino) {
    navigate(destino.ruta) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

/** Regresa solo si la pantalla sigue activa, para que un doble toque no saque al usuario de Mi refri. */
private fun NavController.volverDesde(entrada: NavBackStackEntry) {
    if (entrada.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
        popBackStack()
    }
}
