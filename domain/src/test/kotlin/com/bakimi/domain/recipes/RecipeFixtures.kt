package com.bakimi.domain.recipes

import com.bakimi.domain.ingredients.Ingredient
import com.bakimi.domain.inventory.InventoryItem
import com.bakimi.domain.inventory.StorageLocation
import com.bakimi.domain.inventory.MeasureUnit

internal val CHICKEN = Ingredient("pechuga_pollo", "Pechuga de pollo", synonyms = listOf("Pollo"))
internal val EGG = Ingredient("huevo", "Huevo")
internal val LETTUCE = Ingredient("lechuga", "Lechuga")
internal val RICE = Ingredient("arroz", "Arroz")

private val CREDIT = PhotoCredit(author = "Prueba", username = "prueba", photoId = "x")

internal fun recipe(
    id: String = "r",
    minutes: Int = 30,
    baseServings: Int = 2,
    vararg ingredients: RecipeIngredient,
) = Recipe(
    id = id,
    name = id,
    description = "",
    minutes = minutes,
    baseServings = baseServings,
    ingredients = ingredients.toList(),
    steps = emptyList(),
    photoCredit = CREDIT,
)

internal fun needs(ingredient: Ingredient, quantity: Double, unit: MeasureUnit) =
    RecipeIngredient(ingredient, quantity, unit)

internal fun inStock(
    name: String,
    quantity: Double,
    unit: MeasureUnit,
    location: StorageLocation = StorageLocation.FRIDGE,
    ingredientId: String? = null,
) = InventoryItem(
    name = name,
    quantity = quantity,
    unit = unit,
    location = location,
    ingredientId = ingredientId,
)
