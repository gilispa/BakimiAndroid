package com.bakimi.data.recetas

import com.bakimi.domain.ingredientes.coincideCon
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class CatalogoRecetasTest {

    private val catalogo = CatalogoRecetasLocal()
    private val recetas = catalogo.todas()

    @Test
    fun `el catalogo tiene recetas`() {
        assertTrue(recetas.size >= 10)
    }

    @Test
    fun `los ids de receta son unicos y validos como nombre de recurso`() {
        assertEquals(recetas.size, recetas.map { it.id }.toSet().size)
        recetas.forEach { assertTrue(it.id, it.id.matches(Regex("[a-z0-9_]+"))) }
    }

    @Test
    fun `cada receta tiene datos completos y cantidades positivas`() {
        recetas.forEach { receta ->
            assertTrue(receta.id, receta.minutos > 0)
            assertTrue(receta.id, receta.porcionesBase > 0)
            assertTrue(receta.id, receta.pasos.isNotEmpty())
            assertTrue(receta.id, receta.ingredientes.isNotEmpty())
            assertTrue(receta.id, receta.creditoFoto.autor.isNotBlank())
            receta.ingredientes.forEach { assertTrue("${receta.id}/${it.ingrediente.id}", it.cantidad > 0) }
        }
    }

    @Test
    fun `una receta no repite ingredientes`() {
        recetas.forEach { receta ->
            val ids = receta.ingredientes.map { it.ingrediente.id }
            assertEquals(receta.id, ids.size, ids.toSet().size)
        }
    }

    @Test
    fun `ningun nombre o sinonimo coincide con dos ingredientes distintos`() {
        val ingredientes = recetas.flatMap { r -> r.ingredientes.map { it.ingrediente } }.distinctBy { it.id }
        ingredientes.forEach { ingrediente ->
            (listOf(ingrediente.nombre) + ingrediente.sinonimos).forEach { nombre ->
                val coincidencias = ingredientes.filter { it.coincideCon(nombre) }.map { it.id }
                assertEquals(nombre, listOf(ingrediente.id), coincidencias)
            }
        }
    }

    @Test
    fun `las imagenes de los pasos existen en los assets de la app`() {
        // Las pruebas de :data corren desde la carpeta del módulo.
        val assets = File("../app/src/main/assets")
        recetas.flatMap { it.pasos }.mapNotNull { it.imagen }.forEach { ruta ->
            assertTrue("falta $ruta", File(assets, ruta).isFile)
        }
    }

    @Test
    fun `huevos a la mexicana tiene una imagen por paso`() {
        val receta = catalogo.obtener("huevos_mexicana")!!
        assertTrue(receta.pasos.all { it.imagen != null })
    }

    @Test
    fun `obtener busca por id`() {
        assertNotNull(catalogo.obtener(recetas.first().id))
    }
}
