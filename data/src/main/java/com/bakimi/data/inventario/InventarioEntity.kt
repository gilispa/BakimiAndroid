package com.bakimi.data.inventario

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bakimi.domain.inventario.ItemInventario
import com.bakimi.domain.inventario.Ubicacion
import com.bakimi.domain.inventario.Unidad
import java.time.LocalDate

/**
 * Fila del inventario. Unidad y ubicación se guardan por nombre del enum para que reordenar
 * los enums no altere datos ya guardados.
 */
@Entity(tableName = "inventario")
data class InventarioEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val cantidad: Double,
    val unidad: String,
    val ubicacion: String,
    @ColumnInfo(name = "caducidad_dia_epoch") val caducidadDiaEpoch: Long?,
    /** Id del ingrediente del catálogo; `null` para alimentos "otro" o registros de la versión 1. */
    @ColumnInfo(name = "ingrediente_id") val ingredienteId: String? = null,
)

fun ItemInventario.aEntidad(): InventarioEntity = InventarioEntity(
    id = id,
    nombre = nombre,
    cantidad = cantidad,
    unidad = unidad.name,
    ubicacion = ubicacion.name,
    caducidadDiaEpoch = caducidad?.toEpochDay(),
    ingredienteId = ingredienteId,
)

fun InventarioEntity.aDominio(): ItemInventario = ItemInventario(
    id = id,
    nombre = nombre,
    cantidad = cantidad,
    unidad = Unidad.valueOf(unidad),
    ubicacion = Ubicacion.valueOf(ubicacion),
    caducidad = caducidadDiaEpoch?.let(LocalDate::ofEpochDay),
    ingredienteId = ingredienteId,
)
