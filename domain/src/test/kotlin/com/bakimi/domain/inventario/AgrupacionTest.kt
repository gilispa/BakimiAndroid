package com.bakimi.domain.inventario

import org.junit.Assert.assertEquals
import org.junit.Test

class AgrupacionTest {

    private fun item(nombre: String, ubicacion: Ubicacion, cantidad: Double = 1.0) =
        ItemInventario(nombre = nombre, cantidad = cantidad, unidad = Unidad.PIEZA, ubicacion = ubicacion)

    @Test
    fun `agrupa en el orden refrigerador, congelador, despensa y omite grupos vacios`() {
        val grupos = agruparPorUbicacion(
            listOf(
                item("Arroz", Ubicacion.DESPENSA),
                item("Huevo", Ubicacion.REFRIGERADOR),
            ),
        )

        assertEquals(listOf(Ubicacion.REFRIGERADOR, Ubicacion.DESPENSA), grupos.keys.toList())
    }

    @Test
    fun `ordena por nombre sin distinguir mayusculas ni acentos`() {
        val grupos = agruparPorUbicacion(
            listOf(
                item("lechuga", Ubicacion.REFRIGERADOR),
                item("Ajo", Ubicacion.REFRIGERADOR),
                item("Ácido cítrico", Ubicacion.REFRIGERADOR),
                item("Brócoli", Ubicacion.REFRIGERADOR),
            ),
        )

        assertEquals(
            listOf("Ácido cítrico", "Ajo", "Brócoli", "lechuga"),
            grupos.getValue(Ubicacion.REFRIGERADOR).map { it.nombre },
        )
    }

    @Test
    fun `los agotados van al final de su grupo`() {
        val grupos = agruparPorUbicacion(
            listOf(
                item("Aceite", Ubicacion.DESPENSA, cantidad = 0.0),
                item("Sal", Ubicacion.DESPENSA),
                item("Arroz", Ubicacion.DESPENSA),
            ),
        )

        assertEquals(
            listOf("Arroz", "Sal", "Aceite"),
            grupos.getValue(Ubicacion.DESPENSA).map { it.nombre },
        )
    }

    @Test
    fun `inventario vacio no produce grupos`() {
        assertEquals(emptyMap<Ubicacion, List<ItemInventario>>(), agruparPorUbicacion(emptyList()))
    }
}
