package com.bakimi.ui.recipes

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

/** Photo for each recipe, bundled in drawable-nodpi. Source: Unsplash. */
@DrawableRes
fun recipePhoto(id: String): Int? = when (id) {
    "huevos_mexicana" -> R.drawable.recipe_huevos_mexicana
    "huevos_estrellados" -> R.drawable.recipe_huevos_estrellados
    "ensalada_pollo" -> R.drawable.recipe_ensalada_pollo
    "sandwich_pollo" -> R.drawable.recipe_sandwich_pollo
    "arroz_con_pollo" -> R.drawable.recipe_arroz_con_pollo
    "tacos_pollo" -> R.drawable.recipe_tacos_pollo
    "quesadillas" -> R.drawable.recipe_quesadillas
    "omelette" -> R.drawable.recipe_omelette
    "pasta_jitomate" -> R.drawable.recipe_pasta_jitomate
    "avena_platano" -> R.drawable.recipe_avena_platano
    "arroz_frito" -> R.drawable.recipe_arroz_frito
    else -> null
}

/** Already decoded bitmaps; ~30 MB at most. */
private val cache = object : LruCache<Int, Bitmap>(30 * 1024 * 1024) {
    override fun sizeOf(key: Int, value: Bitmap) = value.byteCount
}

private suspend fun load(resources: Resources, @DrawableRes res: Int): ImageBitmap =
    (cache.get(res) ?: withContext(Dispatchers.IO) {
        BitmapFactory.decodeResource(resources, res).also { cache.put(res, it) }
    }).asImageBitmap()

/**
 * Recipe photo. Decoded off the main thread so the carousel does not stutter; while it loads, a
 * surface-colored background is shown.
 */
@Composable
fun RecipeImage(recipeId: String, modifier: Modifier = Modifier) {
    val res = recipePhoto(recipeId)
    val resources = LocalContext.current.resources
    val image by produceState<ImageBitmap?>(initialValue = res?.let { cache.get(it)?.asImageBitmap() }, res) {
        if (value == null && res != null) value = load(resources, res)
    }
    val current = image
    if (current == null) {
        Box(modifier.background(MaterialTheme.colorScheme.surfaceVariant))
    } else {
        // Decorative: the recipe name always accompanies the photo.
        Image(bitmap = current, contentDescription = null, contentScale = ContentScale.Crop, modifier = modifier)
    }
}
