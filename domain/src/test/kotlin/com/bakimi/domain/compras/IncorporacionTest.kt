package com.bakimi.domain.compras

import com.bakimi.domain.ingredientes.Ingrediente
import com.bakimi.domain.inventario.ItemInventario
import com.bakimi.domain.inventario.Ubicacion
import com.bakimi.domain.inventario.Unidad
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IncorporacionTest {

    private val arroz = Ingrediente(
        "arroz",
        "Arroz",
        unidadSugerida = Unidad.GRAMO,
        ubicacionSugerida = Ubicacion.DESPENSA,
    )
    private val pollo = Ingrediente(
        "pechuga_pollo",
        "Pechuga de pollo",
        unidadSugerida = Unidad.GRAMO,
        ubicacionSugerida = Ubicacion.REFRIGERADOR,
    )

    private fun item(
        id: Long,
        nombre: String,
        cantidad: Double,
        unidad: Unidad,
        ubicacion: Ubicacion = Ubicacion.DESPENSA,
        ingredienteId: String? = null,
    ) = ItemInventario(id, nombre, cantidad, unidad, ubicacion, ingredienteId = ingredienteId)

    @Test
    fun `una bolsa de 1 kg suma el kilo completo a lo que ya habia`() {
        val cambios = incorporarCompra(
            listOf(item(1, "Arroz", 200.0, Unidad.GRAMO, ingredienteId = "arroz")),
            listOf(Comprado(arroz, "Arroz", 1000.0, Unidad.GRAMO)),
        )

        assertEquals(listOf(item(1, "Arroz", 1200.0, Unidad.GRAMO, ingredienteId = "arroz")), cambios)
    }

    @Test
    fun `convierte entre unidades compatibles al sumar`() {
        val cambios = incorporarCompra(
            listOf(item(1, "Pechuga de pollo", 1.0, Unidad.KILOGRAMO, Ubicacion.REFRIGERADOR)),
            listOf(Comprado(pollo, "Pechuga de pollo", 300.0, Unidad.GRAMO)),
        )

        assertEquals(1.3, cambios.single().cantidad, 0.0)
        assertEquals(Unidad.KILOGRAMO, cambios.single().unidad)
    }

    @Test
    fun `un registro anterior al catalogo queda vinculado al sumar`() {
        val cambios = incorporarCompra(
            listOf(item(1, "Pechuga de pollo", 1.0, Unidad.KILOGRAMO, Ubicacion.REFRIGERADOR)),
            listOf(Comprado(pollo, "Pechuga de pollo", 300.0, Unidad.GRAMO)),
        )

        assertEquals("pechuga_pollo", cambios.single().ingredienteId)
    }

    @Test
    fun `sin registro previo crea uno nuevo en su ubicacion habitual`() {
        val cambios = incorporarCompra(emptyList(), listOf(Comprado(pollo, "Pechuga de pollo", 500.0, Unidad.GRAMO)))

        val nuevo = cambios.single()
        assertEquals(0L, nuevo.id)
        assertEquals("Pechuga de pollo", nuevo.nombre)
        assertEquals(Ubicacion.REFRIGERADOR, nuevo.ubicacion)
        assertEquals("pechuga_pollo", nuevo.ingredienteId)
    }

    @Test
    fun `reutiliza un registro agotado`() {
        val cambios = incorporarCompra(
            listOf(item(4, "Arroz", 0.0, Unidad.GRAMO, ingredienteId = "arroz")),
            listOf(Comprado(arroz, "Arroz", 1000.0, Unidad.GRAMO)),
        )

        assertEquals(4L, cambios.single().id)
        assertEquals(1000.0, cambios.single().cantidad, 0.0)
    }

    @Test
    fun `prefiere el registro en la ubicacion habitual`() {
        val cambios = incorporarCompra(
            listOf(
                item(1, "Pechuga de pollo", 200.0, Unidad.GRAMO, Ubicacion.CONGELADOR, "pechuga_pollo"),
                item(2, "Pechuga de pollo", 100.0, Unidad.GRAMO, Ubicacion.REFRIGERADOR, "pechuga_pollo"),
            ),
            listOf(Comprado(pollo, "Pechuga de pollo", 300.0, Unidad.GRAMO)),
        )

        assertEquals(2L, cambios.single().id)
        assertEquals(400.0, cambios.single().cantidad, 0.0)
    }

    @Test
    fun `con unidades incompatibles crea un registro aparte en lugar de adivinar`() {
        val cambios = incorporarCompra(
            listOf(item(1, "Pechuga de pollo", 2.0, Unidad.PIEZA, Ubicacion.REFRIGERADOR, "pechuga_pollo")),
            listOf(Comprado(pollo, "Pechuga de pollo", 500.0, Unidad.GRAMO)),
        )

        assertEquals(0L, cambios.single().id)
        assertEquals(500.0, cambios.single().cantidad, 0.0)
    }

    @Test
    fun `un producto fuera del catalogo se suma al registro con su mismo nombre`() {
        val cambios = incorporarCompra(
            listOf(item(9, "Salsa Valentina", 1.0, Unidad.PIEZA)),
            listOf(Comprado(null, "salsa valentina", 2.0, Unidad.PIEZA)),
        )

        assertEquals(9L, cambios.single().id)
        assertEquals(3.0, cambios.single().cantidad, 0.0)
    }

    @Test
    fun `dos compras del mismo alimento terminan en un solo registro`() {
        val cambios = incorporarCompra(
            emptyList(),
            listOf(
                Comprado(arroz, "Arroz", 1000.0, Unidad.GRAMO),
                Comprado(arroz, "Arroz", 500.0, Unidad.GRAMO),
            ),
        )

        assertEquals(1500.0, cambios.single().cantidad, 0.0)
    }

    @Test
    fun `una cantidad en cero no se incorpora`() {
        assertTrue(incorporarCompra(emptyList(), listOf(Comprado(arroz, "Arroz", 0.0, Unidad.GRAMO))).isEmpty())
    }
}
