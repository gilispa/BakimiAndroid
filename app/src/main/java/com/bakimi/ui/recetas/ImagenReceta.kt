package com.bakimi.ui.recetas

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.bakimi.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Foto de cada receta, incluida en drawable-nodpi. Fuente: Unsplash. */
@DrawableRes
fun fotoDeReceta(id: String): Int? = when (id) {
    "huevos_mexicana" -> R.drawable.receta_huevos_mexicana
    "huevos_estrellados" -> R.drawable.receta_huevos_estrellados
    "ensalada_pollo" -> R.drawable.receta_ensalada_pollo
    "sandwich_pollo" -> R.drawable.receta_sandwich_pollo
    "arroz_con_pollo" -> R.drawable.receta_arroz_con_pollo
    "tacos_pollo" -> R.drawable.receta_tacos_pollo
    "quesadillas" -> R.drawable.receta_quesadillas
    "omelette" -> R.drawable.receta_omelette
    "pasta_jitomate" -> R.drawable.receta_pasta_jitomate
    "avena_platano" -> R.drawable.receta_avena_platano
    "arroz_frito" -> R.drawable.receta_arroz_frito
    else -> null
}

/** Mapas de bits ya decodificados; ~30 MB como máximo. */
private val cache = object : LruCache<Int, Bitmap>(30 * 1024 * 1024) {
    override fun sizeOf(key: Int, value: Bitmap) = value.byteCount
}

private suspend fun cargar(resources: Resources, @DrawableRes res: Int): ImageBitmap =
    (cache.get(res) ?: withContext(Dispatchers.IO) {
        BitmapFactory.decodeResource(resources, res).also { cache.put(res, it) }
    }).asImageBitmap()

/**
 * Foto de la receta. Se decodifica fuera del hilo principal para que el carrusel no se trabe;
 * mientras carga se muestra un fondo del color de superficie.
 */
@Composable
fun ImagenReceta(recetaId: String, modifier: Modifier = Modifier) {
    val res = fotoDeReceta(recetaId)
    val resources = LocalContext.current.resources
    val imagen by produceState<ImageBitmap?>(initialValue = res?.let { cache.get(it)?.asImageBitmap() }, res) {
        if (value == null && res != null) value = cargar(resources, res)
    }
    val actual = imagen
    if (actual == null) {
        Box(modifier.background(MaterialTheme.colorScheme.surfaceVariant))
    } else {
        // Decorativa: el nombre de la receta siempre acompaña a la foto.
        Image(bitmap = actual, contentDescription = null, contentScale = ContentScale.Crop, modifier = modifier)
    }
}
