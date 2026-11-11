package com.bakimi.domain.inventario

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GestorInventarioTest {

    /** Repositorio en memoria que cede el hilo entre leer y escribir, como lo haría una base de datos. */
    private class RepositorioEnMemoria : InventarioRepository {
        private val items = MutableStateFlow<Map<Long, ItemInventario>>(emptyMap())
        private var siguienteId = 1L

        override fun observarTodos(): Flow<List<ItemInventario>> = TODO("No se usa en estas pruebas")

        override suspend fun obtener(id: Long): ItemInventario? {
            yield()
            return items.value[id]
        }

        override suspend fun guardar(item: ItemInventario): Long {
            yield()
            val id = if (item.id == 0L) siguienteId++ else item.id
            items.value = items.value + (id to item.copy(id = id))
            return id
        }

        override suspend fun eliminar(id: Long) {
            items.value = items.value - id
        }

        fun actual(id: Long): ItemInventario? = items.value[id]
    }

    private val repositorio = RepositorioEnMemoria()
    private val gestor = GestorInventario(repositorio)

    private fun nuevo(
        nombre: String = "Huevo",
        cantidad: Double = 12.0,
        unidad: Unidad = Unidad.PIEZA,
    ) = ItemInventario(nombre = nombre, cantidad = cantidad, unidad = unidad, ubicacion = Ubicacion.REFRIGERADOR)

    private fun guardarValido(item: ItemInventario): Long = runBlocking {
        (gestor.guardar(item) as ResultadoGuardado.Guardado).id
    }

    @Test
    fun `guardar un alimento valido lo persiste con el nombre recortado`() = runBlocking {
        val resultado = gestor.guardar(nuevo(nombre = "  Pechuga de pollo  "))

        val id = (resultado as ResultadoGuardado.Guardado).id
        assertEquals("Pechuga de pollo", repositorio.actual(id)!!.nombre)
    }

    @Test
    fun `guardar un alimento invalido no persiste nada`() = runBlocking {
        val resultado = gestor.guardar(nuevo(nombre = ""))

        assertEquals(ResultadoGuardado.Invalido(setOf(ErrorItem.NOMBRE_VACIO)), resultado)
        assertNull(repositorio.actual(1))
    }

    @Test
    fun `sumar agrega el paso rapido de la unidad`() = runBlocking {
        val id = guardarValido(nuevo(cantidad = 1.0, unidad = Unidad.KILOGRAMO))

        gestor.ajustar(id, pasos = 1)

        assertEquals(1.5, repositorio.actual(id)!!.cantidad, 0.0)
    }

    @Test
    fun `restar de mas deja el alimento agotado sin cantidad negativa`() = runBlocking {
        val id = guardarValido(nuevo(cantidad = 50.0, unidad = Unidad.GRAMO))

        gestor.ajustar(id, pasos = -1)

        val item = repositorio.actual(id)!!
        assertEquals(0.0, item.cantidad, 0.0)
        assertTrue(item.agotado)
    }

    @Test
    fun `ajustes repetidos no acumulan errores de punto flotante`() = runBlocking {
        val id = guardarValido(nuevo(cantidad = 0.1, unidad = Unidad.LITRO))

        repeat(3) { gestor.ajustar(id, pasos = 1) }

        assertEquals(1.6, repositorio.actual(id)!!.cantidad, 0.0)
    }

    @Test
    fun `toques rapidos simultaneos no pierden ajustes`() = runBlocking {
        val id = guardarValido(nuevo(cantidad = 0.0))

        (1..10).map { async { gestor.ajustar(id, pasos = 1) } }.awaitAll()

        assertEquals(10.0, repositorio.actual(id)!!.cantidad, 0.0)
    }

    @Test
    fun `ajustar un alimento que no existe devuelve null`() = runBlocking {
        assertNull(gestor.ajustar(99, pasos = 1))
    }

    @Test
    fun `marcar agotado conserva el registro con cantidad cero`() = runBlocking {
        val id = guardarValido(nuevo(cantidad = 6.0))

        gestor.marcarAgotado(id)

        val item = repositorio.actual(id)!!
        assertEquals("Huevo", item.nombre)
        assertTrue(item.agotado)
    }

    @Test
    fun `eliminar quita el registro`() = runBlocking {
        val id = guardarValido(nuevo())

        gestor.eliminar(id)

        assertNull(repositorio.actual(id))
    }
}
