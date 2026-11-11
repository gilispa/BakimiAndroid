package com.bakimi.domain.compras

import com.bakimi.domain.ingredientes.Categoria
import com.bakimi.domain.ingredientes.InfoCompra
import com.bakimi.domain.ingredientes.Ingrediente
import com.bakimi.domain.inventario.Unidad
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ListaTest {

    private val arroz = Ingrediente(
        "arroz",
        "Arroz",
        categoria = Categoria.DESPENSA,
        compra = InfoCompra(Unidad.GRAMO, 1000.0, "bolsa de 1 kg", 34.0),
    )
    private val leche = Ingrediente("leche", "Leche", categoria = Categoria.LACTEOS_HUEVO)
    private val catalogo = listOf(arroz, leche).associateBy { it.id }

    private val compraArroz = ArticuloCompra(
        necesidad = Necesidad(arroz, Unidad.GRAMO, 200.0, 0.0, 200.0, false, listOf("arroz_con_pollo")),
        unidad = Unidad.GRAMO,
        cantidad = 1000.0,
        presentaciones = 1,
        empaque = "bolsa de 1 kg",
        precio = 34.0,
        aproximado = false,
    )

    private fun lista(marcas: List<MarcaCompra> = emptyList(), manuales: List<ArticuloManual> = emptyList()) =
        armarLista(listOf(compraArroz), marcas, manuales, catalogo::get)

    @Test
    fun `los articulos del plan aparecen con su presentacion, precio y recetas`() {
        val renglon = lista().renglones.single()

        assertEquals("plan:arroz", renglon.clave)
        assertEquals(1000.0, renglon.cantidad, 0.0)
        assertEquals("bolsa de 1 kg", renglon.empaque)
        assertEquals(34.0, renglon.precio!!, 0.0)
        assertEquals(Categoria.DESPENSA, renglon.categoria)
        assertEquals(listOf("arroz_con_pollo"), renglon.recetas)
        assertFalse(renglon.comprado)
    }

    @Test
    fun `una marca registra la compra, la cantidad real y el precio real`() {
        val renglon = lista(listOf(MarcaCompra("arroz", comprado = true, cantidadReal = 2000.0, precioReal = 60.0)))
            .renglones.single()

        assertTrue(renglon.comprado)
        assertEquals(2000.0, renglon.cantidad, 0.0)
        assertEquals(60.0, renglon.precio!!, 0.0)
        assertTrue(renglon.precioReal)
    }

    @Test
    fun `cambiar la cantidad sin precio real ajusta el estimado en proporcion`() {
        val renglon = lista(listOf(MarcaCompra("arroz", cantidadReal = 2000.0))).renglones.single()

        assertEquals(68.0, renglon.precio!!, 0.0)
        assertFalse(renglon.precioReal)
    }

    @Test
    fun `los productos manuales entran a la lista y al total`() {
        val lista = lista(
            manuales = listOf(
                ArticuloManual(7, "leche", "Leche", 1000.0, Unidad.MILILITRO, precio = 28.0),
                ArticuloManual(8, null, "Salsa Valentina", 1.0, Unidad.PIEZA, precio = null),
            ),
        )

        val leche = lista.renglones.single { it.clave == "manual:7" }
        assertTrue(leche.manual)
        assertEquals(Categoria.LACTEOS_HUEVO, leche.categoria)
        assertNull(lista.renglones.single { it.clave == "manual:8" }.categoria)
        assertEquals(62.0, lista.total.total, 0.0)
        assertTrue(lista.total.parcial)
    }

    @Test
    fun `un producto manual del catalogo sin precio anotado usa el de referencia como estimado`() {
        val lista = lista(
            manuales = listOf(ArticuloManual(3, "arroz", "Arroz", 2000.0, Unidad.GRAMO, precio = null)),
        )

        val manual = lista.renglones.single { it.clave == "manual:3" }
        assertEquals(68.0, manual.precio!!, 0.0)
        assertFalse(manual.precioReal)
    }

    @Test
    fun `las marcas de articulos que ya no estan en el plan se ignoran`() {
        val lista = lista(listOf(MarcaCompra("pechuga_pollo", comprado = true)))

        assertEquals(listOf("plan:arroz"), lista.renglones.map { it.clave })
    }

    @Test
    fun `solo los marcados se incorporan, con su cantidad real`() {
        val lista = lista(
            marcas = listOf(MarcaCompra("arroz", comprado = true, cantidadReal = 2000.0)),
            manuales = listOf(ArticuloManual(7, "leche", "Leche", 1000.0, Unidad.MILILITRO, precio = 28.0)),
        )

        assertEquals(
            listOf(Comprado(arroz, "Arroz", 2000.0, Unidad.GRAMO)),
            lista.comprados(),
        )
    }
}
