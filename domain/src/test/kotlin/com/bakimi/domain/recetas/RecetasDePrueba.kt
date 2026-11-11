package com.bakimi.domain.recetas

import com.bakimi.domain.ingredientes.Ingrediente
import com.bakimi.domain.inventario.ItemInventario
import com.bakimi.domain.inventario.Ubicacion
import com.bakimi.domain.inventario.Unidad

internal val POLLO = Ingrediente("pechuga_pollo", "Pechuga de pollo", sinonimos = listOf("Pollo"))
internal val HUEVO = Ingrediente("huevo", "Huevo")
internal val LECHUGA = Ingrediente("lechuga", "Lechuga")
internal val ARROZ = Ingrediente("arroz", "Arroz")

private val CREDITO = CreditoFoto(autor = "Prueba", usuario = "prueba", idFoto = "x")

internal fun receta(
    id: String = "r",
    minutos: Int = 30,
    porcionesBase: Int = 2,
    vararg ingredientes: IngredienteReceta,
) = Receta(
    id = id,
    nombre = id,
    descripcion = "",
    minutos = minutos,
    porcionesBase = porcionesBase,
    ingredientes = ingredientes.toList(),
    pasos = emptyList(),
    creditoFoto = CREDITO,
)

internal fun necesita(ingrediente: Ingrediente, cantidad: Double, unidad: Unidad) =
    IngredienteReceta(ingrediente, cantidad, unidad)

internal fun hay(
    nombre: String,
    cantidad: Double,
    unidad: Unidad,
    ubicacion: Ubicacion = Ubicacion.REFRIGERADOR,
    ingredienteId: String? = null,
) = ItemInventario(
    nombre = nombre,
    cantidad = cantidad,
    unidad = unidad,
    ubicacion = ubicacion,
    ingredienteId = ingredienteId,
)
