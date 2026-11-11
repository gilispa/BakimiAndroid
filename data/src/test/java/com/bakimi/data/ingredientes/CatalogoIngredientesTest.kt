package com.bakimi.data.ingredientes

import com.bakimi.data.recetas.CatalogoRecetasLocal
import com.bakimi.domain.ingredientes.coincideCon
import com.bakimi.domain.ingredientes.convertir
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogoIngredientesTest {

    private val catalogo = CatalogoIngredientesLocal()
    private val ingredientes = catalogo.todos()

    @Test
    fun `cubre los ingredientes de uso diario`() {
        assertTrue("hay ${ingredientes.size}", ingredientes.size >= 100)
    }

    @Test
    fun `los ids son unicos y estables`() {
        assertEquals(ingredientes.size, ingredientes.map { it.id }.toSet().size)
        ingredientes.forEach { assertTrue(it.id, it.id.matches(Regex("[a-z0-9_]+"))) }
    }

    @Test
    fun `ningun nombre o sinonimo corresponde a dos ingredientes`() {
        ingredientes.forEach { ingrediente ->
            (listOf(ingrediente.nombre) + ingrediente.sinonimos).forEach { nombre ->
                val coincidencias = ingredientes.filter { it.coincideCon(nombre) }.map { it.id }
                assertEquals(nombre, listOf(ingrediente.id), coincidencias)
            }
        }
    }

    @Test
    fun `todo ingrediente de receta esta en el catalogo`() {
        CatalogoRecetasLocal().todas().flatMap { it.ingredientes }.forEach { usado ->
            assertEquals(usado.ingrediente.id, usado.ingrediente, catalogo.obtener(usado.ingrediente.id))
        }
    }

    @Test
    fun `los alimentos que se compran por peso o por pieza tienen equivalencia`() {
        listOf("tortilla_maiz", "pechuga_pollo", "jitomate", "huevo", "cebolla", "pan_caja").forEach { id ->
            assertTrue(id, (catalogo.obtener(id)?.gramosPorPieza ?: 0.0) > 0.0)
        }
    }

    @Test
    fun `el ajo no tiene equivalencia porque una pieza puede ser cabeza o diente`() {
        assertEquals(null, catalogo.obtener("ajo")?.gramosPorPieza)
    }

    @Test
    fun `las equivalencias son positivas y todas pertenecen a un ingrediente del catalogo`() {
        val ids = ingredientes.map { it.id }.toSet()
        GRAMOS_POR_PIEZA.keys.forEach { id -> assertTrue("$id no está en el catálogo", id in ids) }
        ingredientes.forEach { ingrediente ->
            ingrediente.gramosPorPieza?.let { assertTrue(ingrediente.id, it > 0.0) }
        }
    }

    @Test
    fun `todo ingrediente de receta tiene precio de referencia y presentacion valida`() {
        CatalogoRecetasLocal().todas().flatMap { it.ingredientes }.map { it.ingrediente }.distinct().forEach { ing ->
            val compra = ing.compra
            assertTrue("${ing.id} sin datos de compra", compra != null)
            assertTrue("${ing.id} sin precio", (compra!!.precio ?: 0.0) > 0.0)
            assertTrue("${ing.id} presentación", compra.presentacion > 0.0)
        }
    }

    @Test
    fun `la unidad de compra se puede convertir desde la unidad de las recetas`() {
        CatalogoRecetasLocal().todas().flatMap { it.ingredientes }.forEach { usado ->
            val compra = usado.ingrediente.compra ?: return@forEach
            assertTrue(
                "${usado.ingrediente.id}: ${usado.unidad} → ${compra.unidad}",
                usado.ingrediente.convertir(1.0, usado.unidad, compra.unidad) != null,
            )
        }
    }

    @Test
    fun `los datos de compra y los basicos pertenecen a ingredientes del catalogo`() {
        val ids = ingredientes.map { it.id }.toSet()
        (INFO_COMPRA.keys + BASICOS).forEach { id -> assertTrue("$id no está en el catálogo", id in ids) }
    }

    @Test
    fun `la unidad sugerida es compatible con la que piden las recetas`() {
        CatalogoRecetasLocal().todas().flatMap { it.ingredientes }.forEach { usado ->
            assertTrue(
                "${usado.ingrediente.id}: ${usado.ingrediente.unidadSugerida} vs ${usado.unidad}",
                usado.ingrediente.unidadSugerida.esCompatibleCon(usado.unidad),
            )
        }
    }
}
