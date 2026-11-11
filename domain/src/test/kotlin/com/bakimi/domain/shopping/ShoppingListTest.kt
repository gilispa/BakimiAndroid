package com.bakimi.domain.shopping

import com.bakimi.domain.ingredients.Category
import com.bakimi.domain.ingredients.PurchaseInfo
import com.bakimi.domain.ingredients.Ingredient
import com.bakimi.domain.inventory.MeasureUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ShoppingListTest {

    private val rice = Ingredient(
        "arroz",
        "Arroz",
        category = Category.PANTRY,
        purchase = PurchaseInfo(MeasureUnit.GRAM, 1000.0, "bolsa de 1 kg", 34.0),
    )
    private val milk = Ingredient("leche", "Leche", category = Category.DAIRY_EGGS)
    private val catalog = listOf(rice, milk).associateBy { it.id }

    private val ricePurchase = PurchaseItem(
        requirement = Requirement(rice, MeasureUnit.GRAM, 200.0, 0.0, 200.0, false, listOf("arroz_con_pollo")),
        unit = MeasureUnit.GRAM,
        quantity = 1000.0,
        packages = 1,
        packageName = "bolsa de 1 kg",
        price = 34.0,
        approximate = false,
    )

    private fun shoppingList(marks: List<PurchaseMark> = emptyList(), manualItems: List<ManualItem> = emptyList()) =
        buildShoppingList(listOf(ricePurchase), marks, manualItems, catalog::get)

    @Test
    fun `plan items appear with their package, price and recipes`() {
        val line = shoppingList().lines.single()

        assertEquals("plan:arroz", line.key)
        assertEquals(1000.0, line.quantity, 0.0)
        assertEquals("bolsa de 1 kg", line.packageName)
        assertEquals(34.0, line.price!!, 0.0)
        assertEquals(Category.PANTRY, line.category)
        assertEquals(listOf("arroz_con_pollo"), line.recipes)
        assertFalse(line.purchased)
    }

    @Test
    fun `a mark records the purchase, the actual quantity and the actual price`() {
        val line = shoppingList(listOf(PurchaseMark("arroz", purchased = true, actualQuantity = 2000.0, actualPrice = 60.0)))
            .lines.single()

        assertTrue(line.purchased)
        assertEquals(2000.0, line.quantity, 0.0)
        assertEquals(60.0, line.price!!, 0.0)
        assertTrue(line.actualPrice)
    }

    @Test
    fun `changing the quantity without an actual price scales the estimate`() {
        val line = shoppingList(listOf(PurchaseMark("arroz", actualQuantity = 2000.0))).lines.single()

        assertEquals(68.0, line.price!!, 0.0)
        assertFalse(line.actualPrice)
    }

    @Test
    fun `manual products go into the list and the total`() {
        val shoppingList = shoppingList(
            manualItems = listOf(
                ManualItem(7, "leche", "Leche", 1000.0, MeasureUnit.MILLILITER, price = 28.0),
                ManualItem(8, null, "Salsa Valentina", 1.0, MeasureUnit.PIECE, price = null),
            ),
        )

        val milk = shoppingList.lines.single { it.key == "manual:7" }
        assertTrue(milk.manual)
        assertEquals(Category.DAIRY_EGGS, milk.category)
        assertNull(shoppingList.lines.single { it.key == "manual:8" }.category)
        assertEquals(62.0, shoppingList.total.total, 0.0)
        assertTrue(shoppingList.total.partial)
    }

    @Test
    fun `a catalog manual product without a written price uses the reference price as estimate`() {
        val shoppingList = shoppingList(
            manualItems = listOf(ManualItem(3, "arroz", "Arroz", 2000.0, MeasureUnit.GRAM, price = null)),
        )

        val manual = shoppingList.lines.single { it.key == "manual:3" }
        assertEquals(68.0, manual.price!!, 0.0)
        assertFalse(manual.actualPrice)
    }

    @Test
    fun `marks for items no longer in the plan are ignored`() {
        val shoppingList = shoppingList(listOf(PurchaseMark("pechuga_pollo", purchased = true)))

        assertEquals(listOf("plan:arroz"), shoppingList.lines.map { it.key })
    }

    @Test
    fun `only checked items are added, with their actual quantity`() {
        val shoppingList = shoppingList(
            marks = listOf(PurchaseMark("arroz", purchased = true, actualQuantity = 2000.0)),
            manualItems = listOf(ManualItem(7, "leche", "Leche", 1000.0, MeasureUnit.MILLILITER, price = 28.0)),
        )

        assertEquals(
            listOf(Purchased(rice, "Arroz", 2000.0, MeasureUnit.GRAM)),
            shoppingList.purchasedItems(),
        )
    }
}
