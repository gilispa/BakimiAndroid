package com.bakimi.data.compras

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bakimi.domain.compras.ArticuloManual
import com.bakimi.domain.compras.ConfigPlan
import com.bakimi.domain.compras.EntradaPlan
import com.bakimi.domain.compras.MarcaCompra
import com.bakimi.domain.inventario.Unidad

/** Configuración del plan: una sola fila con id 1. */
@Entity(tableName = "plan_config")
data class PlanConfigEntity(
    @PrimaryKey val id: Int = ID_UNICO,
    val personas: Int,
    val presupuesto: Double?,
) {
    companion object {
        const val ID_UNICO = 1
    }
}

@Entity(tableName = "plan_receta")
data class PlanRecetaEntity(
    @PrimaryKey @ColumnInfo(name = "receta_id") val recetaId: String,
    val porciones: Int,
    /** Orden en que se agregaron, para mostrarlas igual. */
    @ColumnInfo(name = "agregada_en") val agregadaEn: Long,
)

@Entity(tableName = "compra_marca")
data class MarcaCompraEntity(
    @PrimaryKey @ColumnInfo(name = "ingrediente_id") val ingredienteId: String,
    val comprado: Boolean,
    @ColumnInfo(name = "cantidad_real") val cantidadReal: Double?,
    @ColumnInfo(name = "precio_real") val precioReal: Double?,
)

@Entity(tableName = "compra_manual")
data class ArticuloManualEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "ingrediente_id") val ingredienteId: String?,
    val nombre: String,
    val cantidad: Double,
    val unidad: String,
    val precio: Double?,
    val comprado: Boolean,
)

fun PlanConfigEntity.aDominio() = ConfigPlan(personas = personas, presupuesto = presupuesto)
fun ConfigPlan.aEntidad() = PlanConfigEntity(personas = personas, presupuesto = presupuesto)

fun PlanRecetaEntity.aDominio() = EntradaPlan(recetaId, porciones)

fun MarcaCompraEntity.aDominio() = MarcaCompra(ingredienteId, comprado, cantidadReal, precioReal)
fun MarcaCompra.aEntidad() = MarcaCompraEntity(ingredienteId, comprado, cantidadReal, precioReal)

fun ArticuloManualEntity.aDominio() =
    ArticuloManual(id, ingredienteId, nombre, cantidad, Unidad.valueOf(unidad), precio, comprado)

fun ArticuloManual.aEntidad() =
    ArticuloManualEntity(id, ingredienteId, nombre, cantidad, unidad.name, precio, comprado)
