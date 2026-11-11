package com.bakimi.domain.compras

import com.bakimi.domain.ingredientes.Categoria
import com.bakimi.domain.ingredientes.InfoCompra
import com.bakimi.domain.ingredientes.Ingrediente
import com.bakimi.domain.ingredientes.IngredienteRepository
import com.bakimi.domain.inventario.InventarioRepository
import com.bakimi.domain.inventario.ItemInventario
import com.bakimi.domain.inventario.Unidad
import com.bakimi.domain.recetas.Receta
import com.bakimi.domain.recetas.RecetaRepository
import com.bakimi.domain.recetas.necesita
import com.bakimi.domain.recetas.receta
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GestorComprasTest {

    private val arroz = Ingrediente(
        "arroz",
        "Arroz",
        categoria = Categoria.DESPENSA,
        unidadSugerida = Unidad.GRAMO,
        compra = InfoCompra(Unidad.GRAMO, 1000.0, "bolsa de 1 kg", 34.0),
    )
    private val cebolla = Ingrediente("cebolla", "Cebolla", categoria = Categoria.FRUTAS_VERDURAS)
    private val arrozRojo = receta(
        "arroz_rojo",
        porcionesBase = 2,
        ingredientes = arrayOf(necesita(arroz, 200.0, Unidad.GRAMO), necesita(cebolla, 0.5, Unidad.PIEZA)),
    )

    private val inventario = InventarioEnMemoria()
    private val planRepo = PlanEnMemoria()
    private val comprasRepo = ComprasEnMemoria(inventario)
    private val gestor = GestorCompras(
        planRepo,
        comprasRepo,
        object : RecetaRepository {
            override fun todas() = listOf(arrozRojo)
            override fun obtener(id: String): Receta? = todas().find { it.id == id }
        },
        object : IngredienteRepository {
            override fun todos() = listOf(arroz, cebolla)
            override fun obtener(id: String) = todos().find { it.id == id }
        },
        inventario,
    )

    private fun semana() = runBlocking { gestor.observarSemana().first() }

    @Test
    fun `agregar al plan usa el numero de personas como porciones`() = runBlocking {
        gestor.guardarConfig(ConfigPlan(personas = 3))
        gestor.agregarAlPlan("arroz_rojo")

        assertEquals(3, semana().recetas.single().porciones)
    }

    @Test
    fun `la lista se genera a partir del plan`() = runBlocking {
        gestor.agregarAlPlan("arroz_rojo", porciones = 2)

        assertEquals(listOf("plan:arroz", "plan:cebolla"), semana().lista.renglones.map { it.clave })
    }

    @Test
    fun `marcar una compra no modifica el inventario`() = runBlocking {
        gestor.agregarAlPlan("arroz_rojo", porciones = 2)
        gestor.marcarComprado("plan:arroz", true)

        assertTrue(inventario.items.value.isEmpty())
        assertTrue(semana().lista.renglones.single { it.clave == "plan:arroz" }.comprado)
    }

    @Test
    fun `una compra parcial incorpora solo lo marcado y deja lo demas pendiente`() = runBlocking {
        gestor.agregarAlPlan("arroz_rojo", porciones = 2)
        gestor.marcarComprado("plan:arroz", true)

        assertEquals(1, gestor.terminarCompra())

        val arrozEnRefri = inventario.items.value.single()
        assertEquals("arroz", arrozEnRefri.ingredienteId)
        assertEquals(1000.0, arrozEnRefri.cantidad, 0.0)
        assertEquals(listOf("plan:cebolla"), semana().lista.renglones.map { it.clave })
    }

    @Test
    fun `repetir la confirmacion no duplica la compra`() = runBlocking {
        gestor.agregarAlPlan("arroz_rojo", porciones = 2)
        gestor.marcarComprado("plan:arroz", true)
        gestor.terminarCompra()

        assertEquals(0, gestor.terminarCompra())
        assertEquals(1000.0, inventario.items.value.single().cantidad, 0.0)
    }

    @Test
    fun `se incorpora la cantidad real corregida`() = runBlocking {
        gestor.agregarAlPlan("arroz_rojo", porciones = 2)
        gestor.corregir("plan:arroz", cantidad = 2000.0, precio = 60.0)
        gestor.marcarComprado("plan:arroz", true)
        gestor.terminarCompra()

        assertEquals(2000.0, inventario.items.value.single().cantidad, 0.0)
    }

    @Test
    fun `un producto manual se incorpora y sale de la lista`() = runBlocking {
        gestor.agregarManual(ArticuloManual(ingredienteId = null, nombre = "Café", cantidad = 1.0, unidad = Unidad.PIEZA, precio = 80.0))
        val clave = semana().lista.renglones.single().clave
        gestor.marcarComprado(clave, true)

        assertEquals(1, gestor.terminarCompra())
        assertEquals("Café", inventario.items.value.single().nombre)
        assertTrue(semana().lista.renglones.isEmpty())
    }

    @Test
    fun `las porciones se mantienen entre 1 y 12`() = runBlocking {
        gestor.agregarAlPlan("arroz_rojo", porciones = 2)
        gestor.cambiarPorciones("arroz_rojo", 40)

        assertEquals(12, semana().recetas.single().porciones)
    }

    @Test
    fun `el presupuesto se compara con el total de la lista`() = runBlocking {
        gestor.guardarConfig(ConfigPlan(personas = 2, presupuesto = 20.0))
        gestor.agregarAlPlan("arroz_rojo", porciones = 2)

        assertEquals(EstadoPresupuesto.EXCEDIDO, semana().presupuesto)
    }

    private class InventarioEnMemoria : InventarioRepository {
        val items = MutableStateFlow<List<ItemInventario>>(emptyList())
        private var siguiente = 1L

        override fun observarTodos(): Flow<List<ItemInventario>> = items
        override suspend fun obtener(id: Long) = items.value.find { it.id == id }
        override suspend fun guardar(item: ItemInventario): Long {
            val id = if (item.id == 0L) siguiente++ else item.id
            items.value = items.value.filter { it.id != id } + item.copy(id = id)
            return id
        }
        override suspend fun eliminar(id: Long) {
            items.value = items.value.filter { it.id != id }
        }
    }

    private class PlanEnMemoria : PlanRepository {
        val config = MutableStateFlow(ConfigPlan())
        val recetas = MutableStateFlow<List<EntradaPlan>>(emptyList())

        override fun observarConfig(): Flow<ConfigPlan> = config
        override fun observarRecetas(): Flow<List<EntradaPlan>> = recetas
        override suspend fun guardarConfig(config: ConfigPlan) {
            this.config.value = config
        }
        override suspend fun guardarReceta(entrada: EntradaPlan) {
            val actual = recetas.value
            recetas.value = if (actual.any { it.recetaId == entrada.recetaId }) {
                actual.map { if (it.recetaId == entrada.recetaId) entrada else it }
            } else {
                actual + entrada
            }
        }
        override suspend fun quitarReceta(recetaId: String) {
            recetas.value = recetas.value.filter { it.recetaId != recetaId }
        }
    }

    private class ComprasEnMemoria(private val inventario: InventarioEnMemoria) : ComprasRepository {
        val marcas = MutableStateFlow<Map<String, MarcaCompra>>(emptyMap())
        val manuales = MutableStateFlow<Map<Long, ArticuloManual>>(emptyMap())
        private var siguiente = 1L

        override fun observarMarcas(): Flow<List<MarcaCompra>> = marcas.map { it.values.toList() }
        override fun observarManuales(): Flow<List<ArticuloManual>> = manuales.map { it.values.toList() }
        override suspend fun obtenerMarca(ingredienteId: String) = marcas.value[ingredienteId]
        override suspend fun obtenerManual(id: Long) = manuales.value[id]
        override suspend fun guardarMarca(marca: MarcaCompra) {
            marcas.value = marcas.value + (marca.ingredienteId to marca)
        }
        override suspend fun guardarManual(articulo: ArticuloManual): Long {
            val id = if (articulo.id == 0L) siguiente++ else articulo.id
            manuales.value = manuales.value + (id to articulo.copy(id = id))
            return id
        }
        override suspend fun eliminarManual(id: Long) {
            manuales.value = manuales.value - id
        }
        override suspend fun registrarCompra(
            inventario: List<ItemInventario>,
            marcasProcesadas: List<String>,
            manualesProcesados: List<Long>,
        ) {
            inventario.forEach { this.inventario.guardar(it) }
            marcas.value = marcas.value - marcasProcesadas.toSet()
            manuales.value = manuales.value - manualesProcesados.toSet()
        }
    }
}
