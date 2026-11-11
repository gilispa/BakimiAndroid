package com.bakimi.domain.inventory

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InputTest {

    private fun item(name: String = "Huevo", quantity: Double = 12.0) =
        InventoryItem(name = name, quantity = quantity, unit = MeasureUnit.PIECE, location = StorageLocation.FRIDGE)

    @Test
    fun `accepts decimal point`() {
        assertEquals(1.5, parseQuantity("1.5")!!, 0.0)
    }

    @Test
    fun `accepts decimal comma`() {
        assertEquals(0.5, parseQuantity("0,5")!!, 0.0)
    }

    @Test
    fun `ignores surrounding whitespace`() {
        assertEquals(12.0, parseQuantity("  12 ")!!, 0.0)
    }

    @Test
    fun `rejects text that is not a number`() {
        assertNull(parseQuantity("medio kilo"))
        assertNull(parseQuantity(""))
    }

    @Test
    fun `rejects negative quantities`() {
        assertNull(parseQuantity("-2"))
    }

    @Test
    fun `rejects non finite values`() {
        assertNull(parseQuantity("NaN"))
        assertNull(parseQuantity("Infinity"))
    }

    @Test
    fun `a food with name and quantity is valid`() {
        assertTrue(validate(item()).isEmpty())
    }

    @Test
    fun `zero quantity is valid because it represents a depleted food`() {
        assertTrue(validate(item(quantity = 0.0)).isEmpty())
    }

    @Test
    fun `blank name is invalid`() {
        assertEquals(setOf(ItemError.EMPTY_NAME), validate(item(name = "   ")))
    }

    @Test
    fun `negative quantity is invalid`() {
        assertEquals(setOf(ItemError.INVALID_QUANTITY), validate(item(quantity = -1.0)))
    }

    @Test
    fun `reports all errors together`() {
        assertEquals(
            setOf(ItemError.EMPTY_NAME, ItemError.INVALID_QUANTITY),
            validate(item(name = "", quantity = Double.NaN)),
        )
    }
}
