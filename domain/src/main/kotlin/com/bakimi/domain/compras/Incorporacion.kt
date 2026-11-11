package com.bakimi.domain.compras

import com.bakimi.domain.ingredientes.Ingrediente
import com.bakimi.domain.ingredientes.corresponde
import com.bakimi.domain.ingredientes.normalizarNombre
import com.bakimi.domain.inventario.ItemInventario
import com.bakimi.domain.inventario.Ubicacion
import com.bakimi.domain.inventario.Unidad
import com.bakimi.domain.inventario.convertir
import com.bakimi.domain.inventario.redondear

/**
 * Producto comprado, con la cantidad real, listo para entrar a Mi refri.
 *
 * @property ingrediente ingrediente del catálogo; `null` para productos "otro".
 */
data class Comprado(
    val ingrediente: Ingrediente?,
    val nombre: String,
    val cantidad: Double,
    val unidad: Unidad,
)

/**
 * Calcula los registros de inventario que hay que guardar para incorporar [comprados].
 *
 * Suma a un registro existente del mismo alimento en una unidad compatible (prefiriendo su
 * ubicación habitual, y reutilizando registros agotados); si no hay, crea uno nuevo en la
 * ubicación habitual. Se incorpora la cantidad comprada completa: una bolsa de 1 kg suma 1 kg
 * aunque solo faltaran 200 g. Nunca usa equivalencias aproximadas para sumar.
 *
 * @return registros actualizados o nuevos (id 0), cada uno una sola vez.
 */
fun incorporarCompra(inventario: List<ItemInventario>, comprados: List<Comprado>): List<ItemInventario> {
    // Registros de trabajo; los nuevos se agregan aquí para que dos compras del mismo alimento se junten.
    val registros = inventario.toMutableList()
    val modificados = mutableSetOf<Int>()

    comprados.filter { it.cantidad > 0.0 }.forEach { comprado ->
        val indice = destino(registros, comprado)
        if (indice == null) {
            registros += ItemInventario(
                nombre = comprado.ingrediente?.nombre ?: comprado.nombre.trim(),
                cantidad = comprado.cantidad,
                unidad = comprado.unidad,
                ubicacion = comprado.ingrediente?.ubicacionSugerida ?: Ubicacion.DESPENSA,
                ingredienteId = comprado.ingrediente?.id,
            )
            modificados += registros.lastIndex
        } else {
            val actual = registros[indice]
            val sumar = convertir(comprado.cantidad, comprado.unidad, actual.unidad) ?: return@forEach
            registros[indice] = actual.copy(
                cantidad = redondear(actual.cantidad + sumar, decimales = 3),
                ingredienteId = actual.ingredienteId ?: comprado.ingrediente?.id,
            )
            modificados += indice
        }
    }
    return modificados.sorted().map { registros[it] }
}

/** Registro al que se suma la compra: mismo alimento, unidad compatible, de preferencia en su ubicación habitual. */
private fun destino(registros: List<ItemInventario>, comprado: Comprado): Int? {
    val candidatos = registros.indices.filter { i ->
        val registro = registros[i]
        val mismoAlimento = comprado.ingrediente?.let { registro.corresponde(it) }
            ?: (registro.ingredienteId == null && normalizarNombre(registro.nombre) == normalizarNombre(comprado.nombre))
        mismoAlimento && registro.unidad.esCompatibleCon(comprado.unidad)
    }
    val habitual = comprado.ingrediente?.ubicacionSugerida
    return candidatos.firstOrNull { registros[it].ubicacion == habitual } ?: candidatos.firstOrNull()
}
