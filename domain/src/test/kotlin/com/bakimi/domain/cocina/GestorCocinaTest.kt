package com.bakimi.domain.cocina

import com.bakimi.domain.ingredientes.Ingrediente
import com.bakimi.domain.inventario.InventarioRepository
import com.bakimi.domain.inventario.ItemInventario
import com.bakimi.domain.inventario.Ubicacion
import com.bakimi.domain.inventario.Unidad
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GestorCocinaTest {

    private val huevo = Ingrediente("huevo", "Huevo")
    private val inventario = InventarioEnMemoria(
        ItemInventario(1, "Huevo", 12.0, Unidad.PIEZA, Ubicacion.REFRIGERADOR, ingredienteId = "huevo"),
    )
    private val preparaciones = PreparacionesEnMemoria(inventario)
    private val gestor = GestorCocina(inventario, preparaciones, ahora = { 1_000L })
    private val usados = listOf(IngredienteUsado(huevo, 4.0, Unidad.PIEZA))

    @Test
    fun `calcular el descuento no modifica el inventario`() = runBlocking {
        val consumo = gestor.calcular(usados)

        assertEquals(8.0, consumo.cambios.single().cantidad, 0.0)
        assertEquals(12.0, inventario.items.value.single().cantidad, 0.0)
    }

    @Test
    fun `confirmar descuenta y registra la preparacion`() = runBlocking {
        val resultado = gestor.confirmar("sesion-1", "omelette", porciones = 2, usados = usados)

        assertTrue(resultado is ResultadoPreparacion.Registrada)
        assertEquals(8.0, inventario.items.value.single().cantidad, 0.0)
        assertEquals(listOf(Preparacion("sesion-1", "omelette", 2, 1_000L)), preparaciones.registradas)
    }

    @Test
    fun `una confirmacion repetida no duplica el descuento`() = runBlocking {
        gestor.confirmar("sesion-1", "omelette", porciones = 2, usados = usados)

        val segunda = gestor.confirmar("sesion-1", "omelette", porciones = 2, usados = usados)

        assertEquals(ResultadoPreparacion.YaRegistrada, segunda)
        assertEquals(8.0, inventario.items.value.single().cantidad, 0.0)
    }

    @Test
    fun `otra sesion de la misma receta si descuenta`() = runBlocking {
        gestor.confirmar("sesion-1", "omelette", porciones = 2, usados = usados)
        gestor.confirmar("sesion-2", "omelette", porciones = 2, usados = usados)

        assertEquals(4.0, inventario.items.value.single().cantidad, 0.0)
    }

    private class InventarioEnMemoria(vararg iniciales: ItemInventario) : InventarioRepository {
        val items = MutableStateFlow(iniciales.toList())

        override fun observarTodos(): Flow<List<ItemInventario>> = items
        override suspend fun obtener(id: Long) = items.value.find { it.id == id }
        override suspend fun guardar(item: ItemInventario): Long {
            items.value = items.value.filter { it.id != item.id } + item
            return item.id
        }
        override suspend fun eliminar(id: Long) {
            items.value = items.value.filter { it.id != id }
        }
    }

    private class PreparacionesEnMemoria(private val inventario: InventarioEnMemoria) : PreparacionRepository {
        val registradas = mutableListOf<Preparacion>()

        override suspend fun registrar(preparacion: Preparacion, cambios: List<ItemInventario>): Boolean {
            if (registradas.any { it.id == preparacion.id }) return false
            registradas += preparacion
            cambios.forEach { inventario.guardar(it) }
            return true
        }
    }
}
