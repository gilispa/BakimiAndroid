package com.bakimi.domain.recetas

import com.bakimi.domain.ingredientes.Ingrediente
import com.bakimi.domain.inventario.Unidad

/** Cantidad de un ingrediente para las porciones base de la receta. */
data class IngredienteReceta(
    val ingrediente: Ingrediente,
    val cantidad: Double,
    val unidad: Unidad,
    /** Aclaración opcional, por ejemplo "rebanadas" o "picado". */
    val nota: String? = null,
)

data class Paso(
    val instruccion: String,
    /** Minutos del paso cuando el tiempo importa (hervir, dorar); `null` si no aplica. */
    val minutos: Int? = null,
    /** Imagen de referencia del paso (ruta dentro de assets); `null` si todavía no hay. */
    val imagen: String? = null,
    /** Video corto del paso; `null` si todavía no hay. La instrucción escrita basta sin él. */
    val video: String? = null,
)

/** Crédito de una foto de Unsplash. */
data class CreditoFoto(
    val autor: String,
    val usuario: String,
    val idFoto: String,
) {
    val urlFoto: String get() = "https://unsplash.com/photos/$idFoto"
    val urlAutor: String get() = "https://unsplash.com/@$usuario"
}

data class Receta(
    val id: String,
    val nombre: String,
    val descripcion: String,
    val minutos: Int,
    val porcionesBase: Int,
    val ingredientes: List<IngredienteReceta>,
    val pasos: List<Paso>,
    val creditoFoto: CreditoFoto,
    /** Proteína estimada por porción; `null` mientras no haya una fuente confiable. */
    val proteinaPorPorcionG: Double? = null,
)

/** Catálogo de recetas. La implementación vive en :data. */
interface RecetaRepository {
    fun todas(): List<Receta>

    fun obtener(id: String): Receta?
}
